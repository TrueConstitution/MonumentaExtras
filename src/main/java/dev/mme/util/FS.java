package dev.mme.util;

import com.google.gson.*;
import dev.mme.core.StyleSerializer;
import dev.mme.core.TextColorSerializer;
import dev.mme.features.tooltip.czcharms.CZCharmEffect;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.io.*;
import java.lang.reflect.Type;

public class FS {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Text.class, new Text.Serializer())
            .registerTypeAdapter(TextColor.class, new TextColorSerializer())
            .registerTypeAdapter(Style.class, new StyleSerializer())
            .registerTypeAdapter(CZCharmEffect.class, new CZCharmEffect.Serializer())
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .disableHtmlEscaping()
            .setPrettyPrinting().create();

    public static File locate(String filePath) {
        return FabricLoader.getInstance().getConfigDir().resolve("mme\\" + filePath).toFile();
    }
    public static <T> T readJsonFile(Type t, String filePath) throws IOException, JsonParseException {
        return readJsonFile(GSON, t, filePath);
    }
    public static <T> T readJsonFile(Gson gson, Type t, String filePath) throws IOException, JsonParseException {
        try (FileReader reader = new FileReader(locate(filePath))) {
            return gson.fromJson(reader, t);
        }
    }

    public static <T> T readJsonFile(Class<T> c, String filePath) throws IOException, JsonParseException {
        return readJsonFile(GSON, c, filePath);
    }
    public static <T> T readJsonFile(Gson gson, Class<T> c, String filePath) throws IOException, JsonParseException {
        try (FileReader reader = new FileReader(locate(filePath))) {
            return gson.fromJson(reader, c);
        }
    }

    public static void writeJsonFile(Object o, String filePath) throws IOException {
        writeJsonFile(GSON, o, filePath);
    }

    public static void writeJsonFile(Gson gson, Object o, String filePath) throws IOException {
        mkParents(filePath);
        try (FileWriter writer = new FileWriter(locate(filePath))) {
            writer.write(gson.toJson(o));
        }
    }
    public static void write(String filePath, String s) throws IOException {
        mkParents(filePath);
        try (FileWriter out = new FileWriter(locate(filePath))) {
            out.write(s);
        }
    }
    public static void append(String filePath, String s) throws IOException {
        mkParents(filePath);
        try (FileWriter out = new FileWriter(locate(filePath))) {
            out.append(s);
        }
    }

    public static String read(String filePath) throws IOException {
        mkParents(filePath);
        StringBuilder ret = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new FileReader(locate(filePath)))) {
            String line = in.readLine();
            while (line != null) {
                ret.append(line).append("\n");
                line = in.readLine();
            }
        }
        return ret.toString();
    }
    public static String readRaw(String filePath) throws IOException {
        mkParents(filePath);
        StringBuilder ret = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new FileReader(locate(filePath)))) {
            String line = in.readLine();
            while (line != null) {
                ret.append(line);
                line = in.readLine();
            }
        }
        return ret.toString();
    }
    public static boolean exists(String path) {
        return locate(path).exists();
    }
    public static void mkParents(String file) {
        locate(file).getParentFile().mkdirs();
    }
    public static void mkdirs(String path) {
        locate(path).mkdirs();
    }
}