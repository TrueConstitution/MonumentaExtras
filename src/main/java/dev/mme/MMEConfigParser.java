package dev.mme;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MMEConfigParser {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
}
