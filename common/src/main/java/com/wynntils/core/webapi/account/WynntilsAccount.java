/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */

package com.wynntils.core.webapi.account;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;
import com.wynntils.core.Reference;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.request.*;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.objects.MD5Verification;
import net.minecraft.util.Crypt;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class WynntilsAccount {

    private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("wynntils-accounts-%d").build());

    String token;
    boolean ready = false;

    HashMap<String, String> encodedConfigs = new HashMap<>();
    HashMap<String, String> md5Verifications = new HashMap<>();

    public WynntilsAccount() { }

    public String getToken() {
        return token;
    }

    public boolean isConnected() {
        return ready;
    }

    public HashMap<String, String> getEncodedConfigs() {
        return encodedConfigs;
    }

    public void dumpEncodedConfig(String name) {
        encodedConfigs.remove(name);
    }

    int connectionAttempts = 0;

    public boolean login() {
        if (WebManager.getApiUrls() == null || connectionAttempts >= 4) return false;
        connectionAttempts++;

        RequestHandler handler = WebManager.getHandler();

        String baseUrl = WebManager.getApiUrls().get("Athena");
        String[] secretKey = new String[1]; // it's an array for the lambda below be able to set it's value

        // generating secret key

        Request getPublicKey = new RequestBuilder(baseUrl + "/auth/getPublicKey", "getPublicKey")
                .handleJsonObject(json -> {
                    if (!json.has("publicKeyIn")) return false;

                    secretKey[0] = parseAndJoinPublicKey(json.get("publicKeyIn").getAsString());
                    return true;
                }).onError(this::login).build();

        handler.addAndDispatch(getPublicKey);

        // response

        JsonObject authParams = new JsonObject();
        authParams.addProperty("username", McUtils.mc().getUser().getName());
        authParams.addProperty("key", secretKey[0]);
        authParams.addProperty("version", Reference.VERSION + "_" + Reference.BUILD_NUMBER);

        Request responseEncryption = new PostRequestBuilder(baseUrl + "/auth/responseEncryption", "responseEncryption")
                .postJsonElement(authParams)
                .handleJsonObject(json -> {
                    if (!json.has("authToken")) return false;

                    token = json.get("authToken").getAsString();

                    // md5 hashes
                    JsonObject hashes = json.getAsJsonObject("hashes");
                    hashes.entrySet().forEach((k) -> md5Verifications.put(k.getKey(), k.getValue().getAsString()));

                    // configurations
                    JsonObject configFiles = json.getAsJsonObject("configFiles");
                    configFiles.entrySet().forEach((k) -> encodedConfigs.put(k.getKey(), k.getValue().getAsString()));

                    ready = true;

                    Reference.LOGGER.info("Successfully connected to Athena!");
                    return true;
                }).onError(this::login).build();

        handler.addAndDispatch(responseEncryption);

        return true;
    }

    private String parseAndJoinPublicKey(String key) {
        try {
            byte[] publicKeyBy = Hex.decodeHex(key.toCharArray());

            SecretKey secretkey = Crypt.generateSecretKey();

            PublicKey publicKey = Crypt.byteToPublicKey(publicKeyBy);

            String s1 = (new BigInteger(Crypt.digestData("", publicKey, secretkey))).toString(16);

            McUtils.mc().getMinecraftSessionService().joinServer(McUtils.mc().getUser().getGameProfile(), McUtils.mc().getUser().getAccessToken(), s1.toLowerCase());

            byte[] secretKeyEncrypted = Crypt.encryptUsingKey(publicKey, secretkey.getEncoded());

            return Hex.encodeHexString(secretKeyEncrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public String getMD5Verification(String key) {
        String digest = md5Verifications.getOrDefault(key, null);
        return MD5Verification.isMd5Digest(digest) ? digest : null;
    }

}
