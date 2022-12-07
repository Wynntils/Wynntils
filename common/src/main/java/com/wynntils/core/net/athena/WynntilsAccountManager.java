/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.athena;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.WebReader;
import com.wynntils.core.webapi.request.PostRequestBuilder;
import com.wynntils.core.webapi.request.Request;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.MD5Verification;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.crypto.SecretKey;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Crypt;
import org.apache.commons.codec.binary.Hex;

public class WynntilsAccountManager extends CoreManager {
    private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("wynntils-accounts-%d").build());
    public static WynntilsAccountManager account = null;

    private String token;
    private boolean ready = false;

    private final HashMap<String, String> encodedConfigs = new HashMap<>();
    private final HashMap<String, String> md5Verifications = new HashMap<>();

    public static void init() {
        setupUserAccount();
    }

    private static boolean isLoggedIn() {
        return (account != null && account.isConnected());
    }

    private static void setupUserAccount() {
        if (isLoggedIn()) return;

        account = new WynntilsAccountManager();
        boolean accountSetup = account.login();

        if (!accountSetup) {
            MutableComponent failed = new TextComponent(
                            "Welps! Trying to connect and set up the Wynntils Account with your data has failed. "
                                    + "Most notably, cloud config syncing will not work. To try this action again, run ")
                    .withStyle(ChatFormatting.GREEN);
            failed.append(new TextComponent("/wynntils reload")
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils reload"))));

            if (McUtils.player() == null) {
                WynntilsMod.error(ComponentUtils.getUnformatted(failed));
                return;
            }

            McUtils.sendMessageToClient(failed);
        }
    }

    public static Optional<WynntilsAccountManager> getAccount() {
        return Optional.ofNullable(account);
    }

    public static boolean isAthenaOnline() {
        return (account != null && account.isConnected());
    }

    public String getToken() {
        return token;
    }

    private boolean isConnected() {
        return ready;
    }

    public HashMap<String, String> getEncodedConfigs() {
        return encodedConfigs;
    }

    public void dumpEncodedConfig(String name) {
        encodedConfigs.remove(name);
    }

    private boolean login() {
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
        authParams.addProperty(
                "version", String.format("A%s %s", WynntilsMod.getVersion(), WynntilsMod.getModLoader()));

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
                            s1.toLowerCase(Locale.ROOT));

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
