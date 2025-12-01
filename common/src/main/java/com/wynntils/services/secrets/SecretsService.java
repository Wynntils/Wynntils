/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.secrets;

import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.services.secrets.type.WynntilsSecret;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.io.FileUtils;

public class SecretsService extends Service {
    private static final File SECRETS_DIR = WynntilsMod.getModStorageDir("secrets");
    private static final String SECRETS_FILE_NAME = "secrets.json";
    private static final String MASTER_KEY_FILE_NAME = "master_key";

    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_NONCE_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private SecureRandom secureRandom = new SecureRandom();
    private SecretKey masterKey;
    private Map<WynntilsSecret, String> secrets = new HashMap<>();

    public SecretsService() {
        super(List.of());

        loadOrCreateMasterKey();
        loadSecrets();
    }

    public void setSecret(WynntilsSecret key, String value) {
        try {
            String encrypted = encryptSecret(value);
            secrets.put(key, encrypted);
            saveSecrets();
        } catch (Exception e) {
            WynntilsMod.error("Failed to encrypt and save secret for " + key, e);
        }
    }

    public String getSecret(WynntilsSecret key) {
        String encrypted = secrets.getOrDefault(key, "");
        if (encrypted.isEmpty()) return "";

        try {
            return decryptSecret(encrypted);
        } catch (Exception e) {
            WynntilsMod.warn("Secret for " + key + " is invalid and will be removed: " + e.getMessage());
            secrets.remove(key);
            saveSecrets();
            return "";
        }
    }

    private void loadOrCreateMasterKey() {
        File keyFile = new File(SECRETS_DIR, MASTER_KEY_FILE_NAME);

        try {
            if (keyFile.exists()) {
                String base64 = FileUtils.readFileToString(keyFile, StandardCharsets.UTF_8)
                        .trim();
                byte[] keyBytes = Base64.getDecoder().decode(base64);

                masterKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
                return;
            }

            // No master key found, generate a new one
            createMasterKey();
            return;
        } catch (Exception e) {
            WynntilsMod.warn("Master key load failed: " + e.getMessage());
        }

        // If we reach here, all attempts failed
        WynntilsMod.error("Master key could not be recovered; clearing secrets and regenerating.");

        // Cleanup invalidated files
        File secretsFile = new File(SECRETS_DIR, SECRETS_FILE_NAME);
        keyFile.delete();
        secretsFile.delete();
        secrets.clear();

        try {
            createMasterKey();
        } catch (Exception e) {
            WynntilsMod.error("Failed to regenerate master key", e);
        }
    }

    private void createMasterKey() throws Exception {
        File keyFile = new File(SECRETS_DIR, MASTER_KEY_FILE_NAME);
        KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGen.init(AES_KEY_SIZE);
        masterKey = keyGen.generateKey();

        String base64 = Base64.getEncoder().encodeToString(masterKey.getEncoded());
        FileUtils.writeStringToFile(keyFile, base64, StandardCharsets.UTF_8);
    }

    private void loadSecrets() {
        File file = new File(SECRETS_DIR, SECRETS_FILE_NAME);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<WynntilsSecret, String>>() {}.getType();
            secrets = WynntilsMod.GSON.fromJson(reader, type);
            if (secrets == null) {
                secrets = new HashMap<>();
            }
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

    private String encryptSecret(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);

        byte[] nonce = new byte[GCM_NONCE_LENGTH];
        secureRandom.nextBytes(nonce);

        AlgorithmParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, spec);

        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[nonce.length + cipherText.length];
        System.arraycopy(nonce, 0, combined, 0, nonce.length);
        System.arraycopy(cipherText, 0, combined, nonce.length, cipherText.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    private String decryptSecret(String base64CipherText) throws Exception {
        byte[] combined = Base64.getDecoder().decode(base64CipherText);

        byte[] nonce = new byte[GCM_NONCE_LENGTH];
        System.arraycopy(combined, 0, nonce, 0, nonce.length);

        byte[] cipherText = new byte[combined.length - nonce.length];
        System.arraycopy(combined, nonce.length, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        AlgorithmParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
        cipher.init(Cipher.DECRYPT_MODE, masterKey, spec);

        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }
}
