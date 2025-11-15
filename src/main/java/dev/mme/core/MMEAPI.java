package dev.mme.core;

import com.google.common.reflect.TypeToken;
import dev.mme.MMEClient;
import dev.mme.util.FS;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public abstract class MMEAPI {
    private static final String rawGHContent = "https://raw.githubusercontent.com/TrueConstitution/MMEAPI/main/";
    private static final String dirAPIContents = "https://api.github.com/repos/TrueConstitution/MMEAPI/contents/";
    private static final Map<String, List<String>> files_cache = new HashMap<>();

    public static <T> T fetchGHContent(String path, TypeToken<T> type) throws IOException {
        return FS.GSON.fromJson(fetchData(rawGHContent, path), type.getType());
    }
    public static <T> T fetchGHContent(String path, Class<T> clazz) throws IOException {
        return FS.GSON.fromJson(fetchData(rawGHContent, path), clazz);
    }
    public static List<String> getFilesInPath(String path) {
        cacheAsync(path);
        return files_cache.getOrDefault(path, List.of());
    }

    public static List<String> getJsonFilesInPath(String path) {
        return getFilesInPath(path).stream().map(str -> str.substring(0, str.length() - 5)).toList();
    }

    @SuppressWarnings("unchecked")
    private static void cacheAsync(String path) {
        if (files_cache.containsKey(path)) return;
        CompletableFuture.runAsync(() -> {
            try {
                files_cache.put(path, ((List<GHDir>) FS.GSON.fromJson(fetchData(dirAPIContents, path), new TypeToken<List<GHDir>>(){}.getType())).stream().map(ghDir -> ghDir.name).toList());
            } catch (IOException e) {
                MMEClient.LOGGER.error(e.getMessage(), e);
            }
        });
    }

    public static String fetchData(String base, String path) throws IOException {
        URL url = new URL(base + path.replace(" ", "%20"));
        URLConnection conn = url.openConnection();
        Scanner scanner = new Scanner(conn.getInputStream());
        scanner.useDelimiter("\\Z");
        String data = scanner.next();
        scanner.close();
        return data;
    }
    private static class GHDir {
        String name;
        String path;
        String sha;
        long size;
        String url;
        String html_url;
        String git_url;
        String download_url;
        String type;
        Links _links;
    }
    private static class Links {
        String self;
        String git;
        String html;
    }
}