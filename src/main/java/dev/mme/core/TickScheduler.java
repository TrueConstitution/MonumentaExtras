package dev.mme.core;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;

import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

public class TickScheduler implements ClientTickEvents.StartTick {
    public static final TickScheduler INSTANCE = new TickScheduler();
    private long tc = 0;
    private final PriorityQueue<Task> minHeap = new PriorityQueue<>();

    public TickScheduler() {
        ClientTickEvents.START_CLIENT_TICK.register(this);
    }

    @Override
    public void onStartTick(MinecraftClient client) {
        this.tc++;
        while (!minHeap.isEmpty() && minHeap.peek().tick <= tc) {
            Task task = minHeap.poll();
            if (task.runnable != null) {
                task.runnable.onStartTick(client);
            }
        }
    }

    public record Task(long tick, @Nullable ClientTickEvents.StartTick runnable) implements Comparable<Task> {
        @Override
        public int compareTo(Task o) {
            return Long.compare(tick, o.tick);
        }
    }

    public CompletableFuture<Void> schedule(int delay, ClientTickEvents.StartTick runnable) {
        final Task[] task = new Task[1];
        CompletableFuture<Void> future = new CompletableFuture<>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                minHeap.remove(task[0]);
                return super.cancel(mayInterruptIfRunning);
            }
        };

        task[0] = new Task(tc + delay, client -> {
            if (future.isCancelled()) return;
            future.complete(null);
            runnable.onStartTick(client);
        });

        minHeap.add(task[0]);
        return future;
    }

    public void waitTicks(int ticks) throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        schedule(ticks, client -> semaphore.release());
        semaphore.acquire();
    }
}
