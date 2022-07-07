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
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.objects.CustomColor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;

public class ConfigManager {
    private static final File CONFIGS = WynntilsMod.getModStorageDir("config");
    private static final String FILE_SUFFIX = ".conf.json";
    private static final List<ConfigHolder> CONFIG_HOLDERS = new ArrayList<>();
    private static File userConfig;
    private static JsonObject configObject;
    private static Gson gson;

    public static void registerFeature(Feature feature) {
        List<ConfigHolder> featureConfigOptions = collectConfigOptions(feature);
        if (featureConfigOptions == null) return; // invalid feature

        feature.addConfigOptions(featureConfigOptions);
        loadConfigOptions(featureConfigOptions);
        CONFIG_HOLDERS.addAll(featureConfigOptions);
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
            WynntilsMod.error("Failed to load user config file!");
            e.printStackTrace();
        }
    }

    private static void loadConfigOptions(List<ConfigHolder> holders) {
        if (configObject == null) return; // nothing to load from

        for (ConfigHolder holder : holders) {
            // option hasn't been saved to config
            if (!configObject.has(holder.getJsonName())) continue;

            // read value and update option
            JsonElement holderJson = configObject.get(holder.getJsonName());
            Object value = gson.fromJson(holderJson, holder.getType());
            holder.setValue(value);
        }
    }

    public static void saveConfig() {
        try {
            // create file if necessary
            if (!userConfig.exists()) userConfig.createNewFile();

            // create json object, with entry for each option of each container
            JsonObject holderJson = new JsonObject();
            for (ConfigHolder holder : CONFIG_HOLDERS) {
                if (!holder.isUserEdited()) continue; // only save options that have been set by the user

                Object value = holder.getValue();

                JsonElement holderElement = gson.toJsonTree(value);
                holderJson.add(holder.getJsonName(), holderElement);
            }

            // write json to file
            OutputStreamWriter fileWriter =
                    new OutputStreamWriter(new FileOutputStream(userConfig), StandardCharsets.UTF_8);
            gson.toJson(holderJson, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            WynntilsMod.error("Failed to save user config file!");
            e.printStackTrace();
        }
    }

    private static List<ConfigHolder> collectConfigOptions(Feature feature) {
        FeatureInfo featureInfo = feature.getClass().getAnnotation(FeatureInfo.class);
        // feature has no category defined, can't create options
        if (featureInfo == null || featureInfo.category().isBlank()) return null;

        String category = featureInfo.category();

        loadFeatureOverlayConfigOptions(feature, category);

        return getConfigOptions(category, feature);
    }

    private static void loadFeatureOverlayConfigOptions(Feature feature, String category) {
        // collect feature's overlays' config options
        for (Overlay overlay : feature.getOverlays()) {
            List<ConfigHolder> options = getConfigOptions(category, overlay);

            CONFIG_HOLDERS.addAll(options);

            overlay.addConfigOptions(options);

            loadConfigOptions(options);
        }
    }

    @NotNull
    private static List<ConfigHolder> getConfigOptions(String category, Object parent) {
        List<ConfigHolder> options = new ArrayList<>();

        for (Field overlayConfigFields : FieldUtils.getFieldsWithAnnotation(parent.getClass(), Config.class)) {
            Config metadata = overlayConfigFields.getAnnotation(Config.class);
            options.add(new ConfigHolder(parent, overlayConfigFields, category, metadata));
        }
        return options;
    }
}
