/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.upfixers.ConfigUpfixerManager;
import com.wynntils.core.features.Configurable;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.DynamicOverlay;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.annotations.OverlayGroup;
import com.wynntils.utils.FileUtils;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
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
    private static final List<ConfigHolder> CONFIG_HOLDERS = new ArrayList<>();

    private static final List<ConfigHolder> OVERLAY_GROUP_CONFIG_HOLDERS = new ArrayList<>();
    private static final List<OverlayGroupHolder> OVERLAY_GROUP_FIELDS = new ArrayList<>();

    private File userConfig;
    private JsonObject configObject;

    public ConfigManager(ConfigUpfixerManager upfixer) {
        super(List.of(upfixer));

        // First, we load the config file
        loadConfigFile();

        // Now, we have to apply upfixers, before any config loading happens
        if (upfixer.runUpfixers(configObject)) {
            saveConfigToDisk(configObject);
        }
    }

    public void registerFeature(Feature feature) {
        registerOverlayGroups(feature);

        for (Overlay overlay : feature.getOverlays()) {
            registerConfigOptions(overlay);
        }

        registerConfigOptions(feature);
    }

    private void registerOverlayGroups(Feature feature) {
        List<OverlayGroupHolder> holders = Stream.of(feature.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(OverlayGroup.class))
                .map(field -> new OverlayGroupHolder(
                        field, feature, field.getAnnotation(OverlayGroup.class).instances()))
                .toList();

        if (holders.isEmpty()) return;

        OVERLAY_GROUP_FIELDS.addAll(holders);
        feature.addOverlayGroups(holders);

        for (OverlayGroupHolder holder : holders) {
            holder.initGroup(
                    IntStream.rangeClosed(1, holder.getDefaultCount()).boxed().toList());

            List<ConfigHolder> overlayHolders = holder.getOverlays().stream()
                    .map(this::getConfigOptions)
                    .flatMap(List::stream)
                    .toList();

            holder.getOverlays().forEach(overlay -> overlay.addConfigOptions(this.getConfigOptions(overlay)));

            OVERLAY_GROUP_CONFIG_HOLDERS.addAll(overlayHolders);
        }
    }

    private void registerConfigOptions(Configurable configurable) {
        List<ConfigHolder> configOptions = getConfigOptions(configurable);

        configurable.addConfigOptions(configOptions);
        CONFIG_HOLDERS.addAll(configOptions);
    }

    public void loadConfigFile() {
        // create config directory if necessary
        FileUtils.mkdir(CONFIGS);

        // set up config file based on uuid, load it if it exists
        userConfig = new File(CONFIGS, McUtils.mc().getUser().getUuid() + FILE_SUFFIX);
        if (!userConfig.exists()) {
            FileUtils.createNewFile(userConfig);
            configObject = new JsonObject();
            return;
        }

        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(userConfig), StandardCharsets.UTF_8);
            JsonElement fileElement = JsonParser.parseReader(new JsonReader(reader));
            reader.close();
            if (!fileElement.isJsonObject()) {
                // invalid config file

                // Copy old config file to a backup, with a random part in the name to make sure we do not overwrite it
                FileUtils.copyFile(
                        userConfig,
                        new File(
                                CONFIGS,
                                "invalid_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "_"
                                        + RandomStringUtils.random(5) + "_" + userConfig.getName()));
                FileUtils.deleteFile(userConfig);
                FileUtils.createNewFile(userConfig);
                configObject = new JsonObject();

                return;
            }

            configObject = fileElement.getAsJsonObject();
        } catch (IOException e) {
            WynntilsMod.error("Failed to load user config file!", e);

            configObject = new JsonObject();
        }
    }

    public void loadAllConfigOptions() {
        loadConfigOptions(true, true);
    }

    // Info: The purpose of initOverlayGroups is to use the config system in a way that is really "hacky".
    //       Overlay group initialization needs:
    //          1, Overlay instances to be loaded (at init, the default number of instances, then the number defined in
    //             configObject)
    //          2, We need to handle dynamic overlays' configs as regular configs, so that they can be loaded from the
    //             config file
    //       The problem is that the config system "save" is used to remove unused configs, and that "load" is used to
    //       init dynamic overlay instances.
    //
    //       This really becomes a problem when modifying overlay group sizes at runtime.
    //       We want to do 4 things: Save new overlay group size, init overlay instances, load configs, and remove
    //       unused configs.
    //       This means we need to save - load - save, which we should not do. initOverlayGroups is the solution to
    //       this, for now.
    public void loadConfigOptions(boolean resetIfNotFound, boolean initOverlayGroups) {
        if (configObject == null) {
            WynntilsMod.error("Tried to load configs when configObject is null.");
            return; // nothing to load from
        }

        // We have to set up the overlay groups first, so that the overlays' configs can be loaded
        List<ConfigHolder> oldOverlayHolders = new ArrayList<>(OVERLAY_GROUP_CONFIG_HOLDERS);
        OVERLAY_GROUP_CONFIG_HOLDERS.clear();

        JsonObject overlayGroups = JsonUtils.getNullableJsonObject(configObject, "overlayGroups");

        for (OverlayGroupHolder holder : OVERLAY_GROUP_FIELDS) {
            if (initOverlayGroups) {
                JsonArray ids = JsonUtils.getNullableJsonArray(overlayGroups, holder.getConfigKey());

                List<Integer> idList = overlayGroups.has(holder.getConfigKey())
                        ? ids.asList().stream().map(JsonElement::getAsInt).toList()
                        : IntStream.rangeClosed(1, holder.getDefaultCount())
                                .boxed()
                                .toList();

                holder.initGroup(idList);
            }

            List<ConfigHolder> overlayHolders = holder.getOverlays().stream()
                    .map(this::getConfigOptions)
                    .flatMap(List::stream)
                    .toList();

            holder.getOverlays().forEach(overlay -> overlay.removeConfigOptions(oldOverlayHolders));
            holder.getOverlays().forEach(overlay -> overlay.addConfigOptions(this.getConfigOptions(overlay)));

            holder.getParent().initOverlayGroups();

            OVERLAY_GROUP_CONFIG_HOLDERS.addAll(overlayHolders);
        }

        for (ConfigHolder holder : getConfigHolderList()) {
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

        // Newly created group overlays need to be enabled
        for (OverlayGroupHolder holder : OVERLAY_GROUP_FIELDS) {
            holder.getParent().enableOverlays();
        }
    }

    private static List<ConfigHolder> getConfigHolderList() {
        return Stream.concat(CONFIG_HOLDERS.stream(), OVERLAY_GROUP_CONFIG_HOLDERS.stream())
                .toList();
    }

    public void saveConfig() {
        // create file if necessary
        if (!userConfig.exists()) {
            FileUtils.createNewFile(userConfig);
        }

        // create json object, with entry for each option of each container
        JsonObject holderJson = new JsonObject();
        for (ConfigHolder holder : getConfigHolderList()) {
            if (!holder.valueChanged()) continue; // only save options that have been set by the user
            Object value = holder.getValue();

            JsonElement holderElement = CONFIG_GSON.toJsonTree(value);
            holderJson.add(holder.getJsonName(), holderElement);
        }

        // Also save upfixer data
        holderJson.add(
                Managers.ConfigUpfixer.UPFIXER_JSON_MEMBER_NAME,
                configObject.get(Managers.ConfigUpfixer.UPFIXER_JSON_MEMBER_NAME));

        // Save overlay groups
        JsonObject overlayGroups = new JsonObject();
        for (OverlayGroupHolder holder : OVERLAY_GROUP_FIELDS) {
            JsonArray ids = new JsonArray();

            holder.getOverlays().stream()
                    .map(overlay -> ((DynamicOverlay) overlay).getId())
                    .forEach(ids::add);

            overlayGroups.add(holder.getConfigKey(), ids);
        }

        holderJson.add("overlayGroups", overlayGroups);

        saveConfigToDisk(holderJson);
    }

    private void saveConfigToDisk(JsonObject configObject) {
        try {
            // write json to file
            OutputStreamWriter fileWriter =
                    new OutputStreamWriter(new FileOutputStream(userConfig), StandardCharsets.UTF_8);

            CONFIG_GSON.toJson(configObject, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            WynntilsMod.error("Failed to save user config file!", e);
        }
    }

    public void saveDefaultConfig() {
        try {
            // create file if necessary
            if (!DEFAULT_CONFIG.exists()) {
                FileUtils.createNewFile(DEFAULT_CONFIG);
            }

            // create json object, with entry for each option of each container
            JsonObject holderJson = new JsonObject();
            for (ConfigHolder holder : getConfigHolderList()) {
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
        return getConfigHolderList().stream();
    }

    public Object deepCopy(Object value, Type fieldType) {
        return CONFIG_GSON.fromJson(CONFIG_GSON.toJson(value), fieldType);
    }
}
