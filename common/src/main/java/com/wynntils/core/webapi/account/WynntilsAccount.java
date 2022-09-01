/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.account;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.WebReader;
import com.wynntils.core.webapi.request.PostRequestBuilder;
import com.wynntils.core.webapi.request.Request;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.MD5Verification;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.crypto.SecretKey;
import net.minecraft.util.Crypt;
import org.apache.commons.codec.binary.Hex;

public class WynntilsAccount {
    private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("wynntils-accounts-%d").build());

    private String token;
    private boolean ready = false;

    private final HashMap<String, String> encodedConfigs = new HashMap<>();
    private final HashMap<String, String> md5Verifications = new HashMap<>();

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

    public boolean login() {
        if (WebManager.getApiUrls().isEmpty() || !WebManager.getApiUrls().get().hasKey("Athena")) return false;

        WebReader webReader = WebManager.getApiUrls().get();
        RequestHandler handler = WebManager.getHandler();

        String baseUrl = webReader.get("Athena");
        String[] secretKey = new String[1]; // it's an array for the lambda below be able to set its value

        // generating secret key

        Request getPublicKey = new RequestBuilder(baseUrl + "/auth/getPublicKey", "getPublicKey")
                .handleJsonObject(json -> {
                    if (!json.has("publicKeyIn")) return false;
                    secretKey[0] = parseAndJoinPublicKey(json.get("publicKeyIn").getAsString());
                    return true;
                })
                .build();

        handler.addAndDispatch(getPublicKey);

        // response

        JsonObject authParams = new JsonObject();
        authParams.addProperty("username", McUtils.mc().getUser().getName());
        authParams.addProperty("key", secretKey[0]);
        authParams.addProperty("version", "A" + WynntilsMod.getVersion() + "_" + WynntilsMod.getBuildNumber());

        Request responseEncryption = new PostRequestBuilder(baseUrl + "/auth/responseEncryption", "responseEncryption")
                .postJsonElement(authParams)
                .handleJsonObject(json -> {
                    if (!json.has("authToken")) return false;
                    token = json.get("authToken").getAsString(); /* md5 hashes*/
                    JsonObject hashes = json.getAsJsonObject("hashes");
                    hashes.entrySet()
                            .forEach((k) -> md5Verifications.put(
                                    k.getKey(), k.getValue().getAsString())); /* configurations*/
                    JsonObject configFiles = json.getAsJsonObject("configFiles");
                    configFiles
                            .entrySet()
                            .forEach((k) ->
                                    encodedConfigs.put(k.getKey(), k.getValue().getAsString()));
                    ready = true;
                    WynntilsMod.info("Successfully connected to Athena!");
                    return true;
                })
                .build();

        handler.addAndDispatch(responseEncryption);

        return true;
    }

    private String parseAndJoinPublicKey(String key) {
        try {
            byte[] publicKeyBy = Hex.decodeHex(key.toCharArray());

            SecretKey secretkey = Crypt.generateSecretKey();

            PublicKey publicKey = Crypt.byteToPublicKey(publicKeyBy);

            String s1 = (new BigInteger(Crypt.digestData("", publicKey, secretkey))).toString(16);

            McUtils.mc()
                    .getMinecraftSessionService()
                    .joinServer(
                            McUtils.mc().getUser().getGameProfile(),
                            McUtils.mc().getUser().getAccessToken(),
                            s1.toLowerCase());

            byte[] secretKeyEncrypted = Crypt.encryptUsingKey(publicKey, secretkey.getEncoded());

            return Hex.encodeHexString(secretKeyEncrypted);
        } catch (Exception ex) {
            WynntilsMod.error("Failed to parse public key.", ex);
            return "";
        }
    }

    public String getMD5Verification(String key) {
        String digest = md5Verifications.getOrDefault(key, null);
        return MD5Verification.isMd5Digest(digest) ? digest : null;
    }
}
