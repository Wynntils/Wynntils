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
import com.wynntils.core.config.objects.ConfigCategoryHolder;
import com.wynntils.core.config.objects.ConfigOptionHolder;
import com.wynntils.core.config.objects.ConfigurableHolder;
import com.wynntils.core.config.properties.ConfigOption;
import com.wynntils.core.config.properties.Configurable;
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
import java.util.Locale;
import org.apache.commons.lang3.reflect.FieldUtils;

public class ConfigManager {
    private static final File CONFIGS = new File(WynntilsMod.MOD_STORAGE_ROOT, "configs");
    private static final String FILE_SUFFIX = ".config";

    private static final List<ConfigCategoryHolder> configCategories = new ArrayList<>();
    private static File userConfigs;

    private static Gson gson;

    public static void registerConfigurable(Object configurableObject) {
        ConfigurableHolder configurable = configurableFromObject(configurableObject);
        if (configurable == null) return; // not a valid configurable

        // add configurable to its corresponding category, then try to load it from file
        getOrCreateCategory(configurable.getCategory()).addConfigurable(configurable);
        loadConfig(configurable);
    }

    public static void init() {
        gson = new GsonBuilder().setPrettyPrinting().create();

        // set up config directories, create them if necessary
        CONFIGS.mkdirs();
        userConfigs = new File(CONFIGS, McUtils.mc().getUser().getUuid());
        userConfigs.mkdirs();
    }

    private static void loadConfig(ConfigurableHolder configurable) {
        String fileName = categoryToFileName(configurable.getCategory());
        try {
            File configFile = new File(userConfigs, fileName);
            if (!configFile.exists()) return; // nothing to load

            InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8);
            JsonElement fileElement = JsonParser.parseReader(new JsonReader(reader));
            reader.close();
            if (!fileElement.isJsonObject()) return; // invalid config file

            // find configurable's section in file
            JsonObject fileObject = fileElement.getAsJsonObject();
            if (!fileObject.has(configurable.getJsonName())) return; // section doesn't exist, nothing to load
            JsonObject configObject = fileObject.getAsJsonObject(configurable.getJsonName());

            // attempt to load each value from the object
            for (String optionName : configObject.keySet()) {
                tryUpdateOption(configurable, optionName, configObject.get(optionName));
            }
        } catch (IOException e) {
            Reference.LOGGER.error("Failed to load config: " + fileName);
            e.printStackTrace();
        }
    }

    private static void saveConfig(ConfigCategoryHolder category) {
        String fileName = categoryToFileName(category.getName());
        try {
            // get file, create if necessary
            File configFile = new File(userConfigs, fileName);
            if (!configFile.exists()) configFile.createNewFile();

            // create json object, with entry for each configurable
            JsonObject configJson = new JsonObject();
            for (ConfigurableHolder configurable : category.getConfigurables()) {
                JsonObject configurableJson = configurableAsJson(configurable);
                configJson.add(configurable.getJsonName(), configurableJson);
            }

            // write json to file
            OutputStreamWriter fileWriter =
                    new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8);
            gson.toJson(configJson, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            Reference.LOGGER.error("Failed to save config: " + fileName);
            e.printStackTrace();
        }
    }

    public static void saveConfigs() {
        configCategories.forEach(ConfigManager::saveConfig);
    }

    public static ConfigCategoryHolder getOrCreateCategory(String name) {
        for (ConfigCategoryHolder category : configCategories) {
            if (category.getName().equals(name)) return category;
        }

        // category doesn't exist yet, has to be created
        ConfigCategoryHolder newCategory = new ConfigCategoryHolder(name);
        configCategories.add(newCategory);
        return newCategory;
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

    private static String categoryToFileName(String categoryName) {
        return categoryName.toLowerCase(Locale.ROOT).replace(" ", "_").replace("/", "-") + FILE_SUFFIX;
    }

    private static ConfigurableHolder configurableFromObject(Object configurableObject) {
        Class<?> configurableClass = configurableObject.getClass();

        Configurable metadata = configurableClass.getAnnotation(Configurable.class);
        if (metadata == null || metadata.category().isEmpty()) return null; // not a valid config container

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

    private static ConfigOptionHolder optionFromField(Object parent, Field field) {
        ConfigOption metadata = field.getAnnotation(ConfigOption.class);
        if (metadata == null || metadata.displayName().isEmpty()) return null; // not a valid config variable

        return new ConfigOptionHolder(parent, field, metadata);
    }
}
