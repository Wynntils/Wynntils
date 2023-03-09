/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.utils.FileUtils;
import com.wynntils.utils.colors.CustomColor;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

public final class JsonManager extends Manager {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(CustomColor.class, new CustomColor.CustomColorSerializer())
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    public JsonManager() {
        super(List.of());
    }

    public Type findFieldTypeOverride(Object parent, Field field) {
        Optional<Field> typeField = Arrays.stream(
                        FieldUtils.getFieldsWithAnnotation(parent.getClass(), TypeOverride.class))
                .filter(f ->
                        f.getType() == Type.class && f.getName().equals(field.getName() + "Type"))
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

    public Object deepCopy(Object value, Type fieldType) {
        return GSON.fromJson(GSON.toJson(value), fieldType);
    }

    public void savePreciousJson(File jsonFile, JsonObject jsonObject) {
        // create file if necessary
        if (!jsonFile.exists()) {
            FileUtils.createNewFile(jsonFile);
        }

        // FIXME: Make backup first!
        try {
            // write json to file
            OutputStreamWriter fileWriter =
                    new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8);

            GSON.toJson(jsonObject, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            WynntilsMod.error("Failed to save json file " + jsonFile, e);
        }
    }

    public JsonObject loadPreciousJson(File jsonFile) {
        JsonObject storageJson;

        // create directory if necessary
        FileUtils.mkdir(jsonFile.getParentFile());

        if (!jsonFile.exists()) {
            FileUtils.createNewFile(jsonFile);
            return new JsonObject();
        }

        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8);
            JsonElement fileElement = JsonParser.parseReader(new JsonReader(reader));
            reader.close();
            if (!fileElement.isJsonObject()) {
                // invalid json file
                WynntilsMod.error("Error in json file " + jsonFile.getPath());

                return handleInvalidFile(jsonFile);
            }

            return fileElement.getAsJsonObject();
        } catch (JsonParseException | IOException e) {
            WynntilsMod.error("Failed to load or parse json file " + jsonFile.getPath(), e);

            return handleInvalidFile(jsonFile);
        }
    }

    private JsonObject handleInvalidFile(File jsonFile) {
        File dir = jsonFile.getParentFile();

        // Copy old  file to a backup, with a random part in the name to make sure we do not overwrite it
        FileUtils.tryCopyFile(
                jsonFile,
                new File(
                        dir,
                        "invalid_" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "_"
                                + RandomStringUtils.randomAlphanumeric(5) + "_" + jsonFile.getName()));
        FileUtils.deleteFile(jsonFile);
        FileUtils.createNewFile(jsonFile);

        return new JsonObject();
    }
}
