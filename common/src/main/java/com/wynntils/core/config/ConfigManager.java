/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.upfixers.ConfigUpfixerManager;
import com.wynntils.core.features.Configurable;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.json.JsonManager;
import com.wynntils.utils.FileUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.apache.commons.lang3.reflect.FieldUtils;

public final class ConfigManager extends Manager {
    private static final File CONFIGS = WynntilsMod.getModStorageDir("config");
    private static final String FILE_SUFFIX = ".conf.json";
    private static final File DEFAULT_CONFIG = new File(CONFIGS, "default" + FILE_SUFFIX);
    private static final Gson CONFIG_GSON = new GsonBuilder()
            .registerTypeAdapter(CustomColor.class, new CustomColor.CustomColorSerializer())
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    private static final Set<ConfigHolder> CONFIG_HOLDERS = new TreeSet<>();

    private final File userConfig;
    private JsonObject configObject;

    public ConfigManager(ConfigUpfixerManager configUpfixerManager, JsonManager jsonManager) {
        super(List.of(configUpfixerManager, jsonManager));

        userConfig = new File(CONFIGS, McUtils.mc().getUser().getUuid() + FILE_SUFFIX);

        // First, we load the config file
        configObject = Managers.Json.loadPreciousJson(userConfig);

        // Now, we have to apply upfixers, before any config loading happens
        if (configUpfixerManager.runUpfixers(configObject)) {
            Managers.Json.savePreciousJson(userConfig, configObject);
        }
    }

    public void registerFeature(Feature feature) {
        for (Overlay overlay : feature.getOverlays()) {
            registerConfigOptions(overlay);
        }

        registerConfigOptions(feature);
    }

    private void registerConfigOptions(Configurable configurable) {
        List<ConfigHolder> configOptions = getConfigOptions(configurable);

        configurable.addConfigOptions(configOptions);
        loadConfigOptions(configOptions, false);
        CONFIG_HOLDERS.addAll(configOptions);
    }

    public void reloadConfiguration() {
        configObject = Managers.Json.loadPreciousJson(userConfig);
        loadConfigOptions(CONFIG_HOLDERS.stream().toList(), true);
    }

    private void loadConfigOptions(List<ConfigHolder> holders, boolean resetIfNotFound) {
        if (configObject == null) {
            WynntilsMod.error("Tried to load configs when configObject is null.");
            return; // nothing to load from
        }

        for (ConfigHolder holder : holders) {
            // option hasn't been saved to config
            if (!configObject.has(holder.getJsonName())) {
                if (resetIfNotFound) {
                    holder.reset();
                }
                continue;
            }

            // read value and update option
            JsonElement holderJson = configObject.get(holder.getJsonName());
            Object value = CONFIG_GSON.fromJson(holderJson, holder.getType());
            holder.setValue(value);
        }
    }

    public void saveConfig() {
        // create file if necessary
        if (!userConfig.exists()) {
            FileUtils.createNewFile(userConfig);
        }

        // create json object, with entry for each option of each container
        JsonObject holderJson = new JsonObject();
        for (ConfigHolder holder : CONFIG_HOLDERS) {
            if (!holder.valueChanged()) continue; // only save options that have been set by the user
            Object value = holder.getValue();

            JsonElement holderElement = CONFIG_GSON.toJsonTree(value);
            holderJson.add(holder.getJsonName(), holderElement);
        }

        // Also save upfixer data
        holderJson.add(
                Managers.ConfigUpfixer.UPFIXER_JSON_MEMBER_NAME,
                configObject.get(Managers.ConfigUpfixer.UPFIXER_JSON_MEMBER_NAME));

        Managers.Json.savePreciousJson(userConfig, holderJson);
    }

    public void saveDefaultConfig() {
        try {
            // create file if necessary
            if (!DEFAULT_CONFIG.exists()) {
                FileUtils.createNewFile(DEFAULT_CONFIG);
            }

            // create json object, with entry for each option of each container
            JsonObject holderJson = new JsonObject();
            for (ConfigHolder holder : CONFIG_HOLDERS) {
                Object value = holder.getDefaultValue();

                JsonElement holderElement = CONFIG_GSON.toJsonTree(value);
                holderJson.add(holder.getJsonName(), holderElement);
            }

            // write json to file
            OutputStreamWriter fileWriter =
                    new OutputStreamWriter(new FileOutputStream(DEFAULT_CONFIG), StandardCharsets.UTF_8);
            CONFIG_GSON.toJson(holderJson, fileWriter);
            fileWriter.close();
            WynntilsMod.info("Default config file created with " + holderJson.size() + " config values.");
        } catch (IOException e) {
            WynntilsMod.error("Failed to save user config file!", e);
        }
    }

    private Type findFieldTypeOverride(Configurable parent, Field configField) {
        Optional<Field> typeField = Arrays.stream(
                        FieldUtils.getFieldsWithAnnotation(parent.getClass(), TypeOverride.class))
                .filter(field ->
                        field.getType() == Type.class && field.getName().equals(configField.getName() + "Type"))
                .findFirst();

        if (typeField.isPresent()) {
            try {
                return (Type) FieldUtils.readField(typeField.get(), parent, true);
            } catch (IllegalAccessException e) {
                WynntilsMod.error("Unable to get field " + typeField.get().getName(), e);
            }
        }

        return null;
    }

    private List<ConfigHolder> getConfigOptions(Configurable parent) {
        List<ConfigHolder> options = new ArrayList<>();

        for (Field configField : FieldUtils.getFieldsWithAnnotation(parent.getClass(), Config.class)) {
            Config metadata = configField.getAnnotation(Config.class);

            Type typeOverride = findFieldTypeOverride(parent, configField);

            ConfigHolder configHolder = new ConfigHolder(parent, configField, metadata, typeOverride);
            if (WynntilsMod.isDevelopmentEnvironment()) {
                if (metadata.visible()) {
                    if (configHolder.getDisplayName().startsWith("feature.wynntils.")) {
                        WynntilsMod.error("Config displayName i18n is missing for " + configHolder.getDisplayName());
                        throw new AssertionError("Missing i18n for " + configHolder.getDisplayName());
                    }
                    if (configHolder.getDescription().startsWith("feature.wynntils.")) {
                        WynntilsMod.error("Config description i18n is missing for " + configHolder.getDescription());
                        throw new AssertionError("Missing i18n for " + configHolder.getDescription());
                    }
                    if (configHolder.getDescription().isEmpty()) {
                        WynntilsMod.error("Config description is empty for " + configHolder.getDisplayName());
                        throw new AssertionError("Missing i18n for " + configHolder.getDisplayName());
                    }
                }
            }
            options.add(configHolder);
        }
        return options;
    }

    public Stream<ConfigHolder> getConfigHolders() {
        return CONFIG_HOLDERS.stream();
    }

    public Object deepCopy(Object value, Type fieldType) {
        return CONFIG_GSON.fromJson(CONFIG_GSON.toJson(value), fieldType);
    }
}
