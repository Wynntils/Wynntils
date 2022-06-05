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
import com.wynntils.core.config.objects.ConfigOptionHolder;
import com.wynntils.core.config.objects.ConfigurableHolder;
import com.wynntils.core.config.properties.ConfigOption;
import com.wynntils.core.config.properties.ConfigurableInfo;
import com.wynntils.mc.utils.McUtils;
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

public class ConfigManager {
    private static final File CONFIGS = new File(WynntilsMod.MOD_STORAGE_ROOT, "configs");
    private static final String FILE_SUFFIX = ".config";

    private static final List<ConfigurableHolder> configContainers = new ArrayList<>();
    private static File userConfig;
    private static JsonObject configObject;

    private static Gson gson;

    public static void registerConfigurable(Configurable configurableObject) {
        ConfigurableHolder configurable = configurableFromObject(configurableObject);
        if (configurable == null) return; // not a valid configurable

        // add configurable to its corresponding category, then try to load it from file
        configContainers.add(configurable);
        loadConfigOptions(configurable);
    }

    public static void init() {
        gson = new GsonBuilder().setPrettyPrinting().create();

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

    private static void loadConfigOptions(ConfigurableHolder configurable) {
        if (configObject == null) return; // nothing to load from

        String prefix = configurable.getJsonName() + ".";
        for (ConfigOptionHolder option : configurable.getOptions()) {
            String name = prefix + option.getJsonName();

            // option hasn't been saved to config
            if (!configObject.has(name)) continue;

            // read value and update option
            JsonElement optionJson = configObject.get(name);
            Object value = gson.fromJson(optionJson, option.getType());
            option.setValue(value);
        }
    }

    public static void saveConfig() {
        try {
            // create file if necessary
            if (!userConfig.exists()) userConfig.createNewFile();

            // create json object, with entry for each option of each container
            JsonObject configJson = new JsonObject();
            for (ConfigurableHolder configurable : configContainers) {
                String prefix = configurable.getJsonName() + ".";
                for (ConfigOptionHolder option : configurable.getOptions()) {
                    if (option.isDefault()) continue; // only save options that are non-default

                    String name = prefix + option.getJsonName();
                    JsonElement optionElement = gson.toJsonTree(option.getValue());
                    configJson.add(name, optionElement);
                }
            }

            // write json to file
            OutputStreamWriter fileWriter =
                    new OutputStreamWriter(new FileOutputStream(userConfig), StandardCharsets.UTF_8);
            gson.toJson(configJson, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            Reference.LOGGER.error("Failed to save user config file!");
            e.printStackTrace();
        }
    }

    private static ConfigurableHolder configurableFromObject(Configurable configurableObject) {
        Class<?> configurableClass = configurableObject.getClass();

        ConfigurableInfo metadata = configurableClass.getAnnotation(ConfigurableInfo.class);
        if (metadata == null || metadata.category().isEmpty()) return null; // not a valid configurable

        List<ConfigOptionHolder> options = new ArrayList<>();

        // collect options
        for (Field f : FieldUtils.getFieldsWithAnnotation(configurableClass, ConfigOption.class)) {
            ConfigOptionHolder option = optionFromField(configurableObject, f);
            if (option == null) continue;

            options.add(option);
        }

        if (options.isEmpty()) return null; // configurable contains no options, so not valid

        return new ConfigurableHolder(configurableClass, options, metadata);
    }

    private static ConfigOptionHolder optionFromField(Configurable parent, Field field) {
        ConfigOption metadata = field.getAnnotation(ConfigOption.class);
        if (metadata == null || metadata.displayName().isEmpty()) return null; // not a valid config variable

        return new ConfigOptionHolder(parent, field, metadata);
    }
}
