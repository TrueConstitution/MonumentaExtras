package dev.mme.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
        import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class TextBuilder {
    private MutableText head;
    private MutableText self;
    private boolean keepStyle;

    public static final Identifier SPACING = Identifier.of("mmev2", "spacing");

    public TextBuilder() {
        this(Text.literal(""), false);
    }

    public TextBuilder(Text text, boolean append) {
        this.self = text.copy();
        if (append) {
            this.head = Text.literal("");
            append(self);
            return;
        }
        this.head = this.self;
        if (self.getStyle() == Style.EMPTY)  {
            withFormat(Formatting.GRAY);
            resetMarkdowns();
        }
    }
    public TextBuilder(String text, boolean append) {
        this.self = Text.literal(text);
        if (append) {
            this.head = Text.literal("");
            append(self);
            return;
        }
        this.head = this.self;
        withFormat(Formatting.GRAY);
        resetMarkdowns();
    }

    public TextBuilder(@Nullable Text text) {
        this(text != null ? text : Text.literal(""), false);
    }
    public TextBuilder(@Nullable String text) {
        this(text != null ? text : "", false);
    }
    public TextBuilder setKeepStyle(boolean keepStyle) {
        this.keepStyle = keepStyle;
        return this;
    }

    public TextBuilder withStyle(Style style) {
        self.setStyle(style);
        return this;
    }

    public TextBuilder append(String text) {
        Style prevStyle = self.getStyle();
        head.append(self = Text.literal(text));
        if (this.keepStyle) self.setStyle(prevStyle);
        return this;
    }
    public TextBuilder append(@Nullable Text text) {
        if (text == null) return this;
        Style prevStyle = self.getStyle();
        head.append(self = text.copy());
        if (this.keepStyle && self.getStyle().isEmpty()) self.setStyle(prevStyle);
        return this;
    }
    public TextBuilder append(TextBuilder builder) {
        return append(builder.build());
    }
    public TextBuilder withFormat(Formatting format) {
        self.styled(style -> style.withFormatting(format));
        return this;
    }
    public TextBuilder withShowTextHover(Text text) {
        self.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text)));
        return this;
    }
    public TextBuilder resetMarkdowns() {
        self.styled(style -> {
            style = style.withItalic(false);
            style = style.withObfuscated(false);
            style = style.withUnderline(false);
            style = style.withBold(false);
            return style;
        });
        return this;
    }
    public TextBuilder withShowTextHover(String text) {
        self.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(text))));
        return this;
    }

    public TextBuilder withClickEvent(String action, String value) {
        ClickEvent.Action clickAction = ClickEvent.Action.valueOf(action);
        assert action != null;
        self.styled(style -> style.withClickEvent(new ClickEvent(clickAction, value)));
        return this;
    }
    public TextBuilder withClickEvent(ClickEvent.Action action, String value) {
        assert action != null;
        self.styled(style -> style.withClickEvent(new ClickEvent(action, value)));
        return this;
    }

    public TextBuilder withColor(int r, int g, int b) {
        self.styled(style -> style.withColor((r & 255) << 16 | (g & 255) << 8 | (b & 255)));
        return this;
    }

    public TextBuilder withColor(int color) {
        self.styled(style -> style.withColor(color));
        return this;
    }

    public TextBuilder withColor(TextColor color) {
        self.styled(style -> style.withColor(color));
        return this;
    }

    public TextBuilder withFont(Identifier font) {
        self.styled(style -> style.withFont(font));
        return this;
    }

    public static Text getPadding(int pixels) {
        TextBuilder builder = new TextBuilder();
        while (pixels > 0) {
            for (int i = 9; i >= 0; i--) {
                int s = 1 << i;
                if (s > pixels) continue;
                pixels -= s;
                builder.append(String.valueOf(i)).withFont(SPACING);
                break;
            }
        }
        return builder.build();
    }

    public enum Alignment {
        LEFT,
        RIGHT,
        CENTER
    }

    public TextBuilder align(Alignment alignment, int width) {
        var TR = MinecraftClient.getInstance().textRenderer;
        if (TR == null) return this;
        int padding = width - TR.getWidth(head);
        if (padding <= 0) return this; // no support for negative padding
        switch (alignment) {
            case LEFT -> head.getSiblings().add(getPadding(padding));
            case RIGHT -> head.getSiblings().add(0, getPadding(padding));
            case CENTER -> {
                head.getSiblings().add(0, getPadding(padding / 2));
                head.getSiblings().add(getPadding(padding / 2));
            }
        }
        return this;
    }


    public Text build() {
        return head;
    }
}