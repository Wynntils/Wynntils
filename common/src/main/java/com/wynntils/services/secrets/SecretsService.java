/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.secrets;

import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.services.secrets.type.SecretKey;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

public class SecretsService extends Service {
    private static final File SECRETS_DIR = WynntilsMod.getModStorageDir("secrets");
    private static final String SECRETS_FILE_NAME = "secrets.json";

    private static Map<SecretKey, String> secrets = new HashMap<>();

    public SecretsService() {
        super(List.of());

        loadSecrets();
    }

    public void setSecret(SecretKey key, String value) {
        String base64Secret = Base64.getEncoder().encodeToString(value.getBytes(Charset.defaultCharset()));

        secrets.put(key, base64Secret);

        saveSecrets();
    }

    public String getSecret(SecretKey key) {
        String secret = secrets.getOrDefault(key, "");

        if (secret.isEmpty()) return "";

        try {
            byte[] decoded = Base64.getDecoder().decode(secret);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            WynntilsMod.warn("Failed to decode secret for key " + key + ": " + e.getMessage());
            return "";
        }
    }

    private static void loadSecrets() {
        File file = new File(SECRETS_DIR, SECRETS_FILE_NAME);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<SecretKey, String>>() {}.getType();
            secrets = WynntilsMod.GSON.fromJson(reader, type);
        } catch (IOException e) {
            WynntilsMod.warn("Could not parse secrets file.", e);
        }
    }

    private void saveSecrets() {
        try {
            File f = new File(SECRETS_DIR, SECRETS_FILE_NAME);
            String json = WynntilsMod.GSON.toJson(secrets);
            FileUtils.writeStringToFile(f, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            WynntilsMod.error("Error when trying to save secrets.", e);
        }
    }
}
