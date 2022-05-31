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
import com.wynntils.core.config.properties.Configurable;
import com.wynntils.core.features.properties.StartDisabled;
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
    public static final File CONFIGS = new File(WynntilsMod.MOD_STORAGE_ROOT, "configs");

    private static File userConfigs;

    /**
     * Issue: multiple containers under same parent category
     * Use map of category names to list of containers? Could make category holder object as well
     *
     */
    private static List<ConfigurableHolder> configContainers = new ArrayList<>();

    private static Gson gson;

    public static void registerConfigurable(Class<?> configurableClass) {
        ConfigurableHolder configurable = configurableFromClass(configurableClass);
        if (configurable == null) return; // not a valid configurable

        configContainers.add(configurable);
        loadConfig(configurable);
        // saveConfig(configurable);
    }

    public static void init() {
        gson = new GsonBuilder().setPrettyPrinting().create();

        // set up config directories, create them if necessary
        CONFIGS.mkdirs();
        userConfigs = new File(CONFIGS, McUtils.mc().getUser().getUuid());
        userConfigs.mkdirs();
    }

    private static void loadConfig(ConfigurableHolder configurable) {
        try {
            File configFile = new File(userConfigs, configurable.getCategoryFileName() + ".config");
            if (!configFile.exists()) return; // nothing to load

            InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8);
            JsonElement element = JsonParser.parseReader(new JsonReader(reader));
            reader.close();
            if (!element.isJsonObject()) return; // invalid config file

            // attempt to load each value from the file
            JsonObject configJson = element.getAsJsonObject();
            for (String optionName : configJson.keySet()) {
                tryUpdateOption(configurable, optionName, configJson.get(optionName));
            }
        } catch (IOException e) {
            Reference.LOGGER.error("Failed to load config: " + configurable.getCategoryFileName());
            e.printStackTrace();
        }
    }

    private static void saveConfig(ConfigurableHolder configurable) {
        try {
            // get file, create if necessary
            File configFile = new File(userConfigs, configurable.getCategoryFileName() + ".config");
            if (!configFile.exists()) configFile.createNewFile();

            // write json to file
            JsonObject configJson = configurableAsJson(configurable);
            OutputStreamWriter fileWriter =
                    new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8);
            gson.toJson(configJson, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            Reference.LOGGER.error("Failed to save config: " + configurable.getCategoryFileName());
            e.printStackTrace();
        }
    }

    public static void saveConfigs() {
        configContainers.forEach(ConfigManager::saveConfig);
    }

    private static void tryUpdateOption(ConfigurableHolder configurable, String optionName, JsonElement valueJson) {
        for (ConfigOptionHolder option : configurable.getOptions()) {
            if (!optionName.equals(option.getOptionJsonName())) continue;

            Object value = gson.fromJson(valueJson, option.getType());
            option.setValue(value);
            return;
        }
        // no matching option
    }

    private static JsonObject configurableAsJson(ConfigurableHolder configurable) {
        JsonObject json = new JsonObject();
        for (ConfigOptionHolder option : configurable.getOptions()) {
            JsonElement optionElement = gson.toJsonTree(option.getValue());
            json.add(option.getOptionJsonName(), optionElement);
        }
        return json;
    }

    private static ConfigurableHolder configurableFromClass(Class<?> configurableClass) {
        Configurable metadata = configurableClass.getAnnotation(Configurable.class);
        if (metadata == null || metadata.category().isEmpty()) return null; // not a valid config container

        List<ConfigOptionHolder> options = new ArrayList<>();

        // collect options
        for (Field f : FieldUtils.getFieldsWithAnnotation(configurableClass, ConfigOption.class)) {
            ConfigOptionHolder option = optionFromField(f);
            if (option == null) continue;

            // special handling for user features that are disabled by default
            if (f.getName().equals("userEnabled") && configurableClass.isAnnotationPresent(StartDisabled.class)) {
                option.setValue(false);
            }

            options.add(option);
        }

        return new ConfigurableHolder(configurableClass, options, metadata);
    }

    private static ConfigOptionHolder optionFromField(Field field) {
        ConfigOption metadata = field.getAnnotation(ConfigOption.class);
        if (metadata == null || metadata.displayName().isEmpty()) return null; // not a valid config variable

        return new ConfigOptionHolder(field, field.getType(), metadata);
    }
}
