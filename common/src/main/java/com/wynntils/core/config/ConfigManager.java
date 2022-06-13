/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.wynntils.core.Reference;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.objects.StorageHolder;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.objects.CustomColor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final File CONFIGS = new File(WynntilsMod.MOD_STORAGE_ROOT, "config");
    private static final String FILE_SUFFIX = ".conf.json";

    private static final List<StorageHolder> STORAGE_HOLDERS = new ArrayList<>();
    private static File userConfig;
    private static JsonObject configObject;

    private static Gson gson;

    public static void registerHolder(StorageHolder holder) {
        STORAGE_HOLDERS.add(holder);
        loadConfigOptions(holder);
    }

    public static void init() {
        gson = new GsonBuilder()
                .registerTypeAdapter(CustomColor.class, new CustomColor.CustomColorSerializer())
                .setPrettyPrinting()
                .create();

        loadConfigFile();
    }

    private static void loadConfigFile() {
        // create config directory if necessary
        CONFIGS.mkdirs();

        // set up config file based on uuid, load it if it exists
        userConfig = new File(CONFIGS, McUtils.mc().getUser().getUuid() + FILE_SUFFIX);
        if (!userConfig.exists()) return;

        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(userConfig), StandardCharsets.UTF_8);
            JsonElement fileElement = JsonParser.parseReader(new JsonReader(reader));
            reader.close();
            if (!fileElement.isJsonObject()) return; // invalid config file

            configObject = fileElement.getAsJsonObject();
        } catch (IOException e) {
            Reference.LOGGER.error("Failed to load user config file!");
            e.printStackTrace();
        }
    }

    private static void loadConfigOptions(StorageHolder holder) {
        if (configObject == null) return; // nothing to load from

        String prefix = holder.getCategory() + ".";
        String name = prefix + holder.getJsonName();

        // option hasn't been saved to config
        if (!configObject.has(name)) return;

        // read value and update option
        JsonElement holderJson = configObject.get(name);
        Object value = gson.fromJson(holderJson, holder.getType());
        holder.setValue(value);
    }

    public static void saveConfig() {
        try {
            // create file if necessary
            if (!userConfig.exists()) userConfig.createNewFile();

            // create json object, with entry for each option of each container
            JsonObject holderJson = new JsonObject();
            for (StorageHolder holder : STORAGE_HOLDERS) {
                String prefix = holder.getCategory() + ".";
                if (holder.isDefault()) continue; // only save options that are non-default

                String name = prefix + holder.getJsonName();
                JsonElement holderElement = gson.toJsonTree(holder.getValue());
                holderJson.add(name, holderElement);
            }

            // write json to file
            OutputStreamWriter fileWriter =
                    new OutputStreamWriter(new FileOutputStream(userConfig), StandardCharsets.UTF_8);
            gson.toJson(holderJson, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            Reference.LOGGER.error("Failed to save user config file!");
            e.printStackTrace();
        }
    }
}
