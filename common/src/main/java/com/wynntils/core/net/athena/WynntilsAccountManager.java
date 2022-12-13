/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.athena;

import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlId;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.crypto.SecretKey;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Crypt;
import org.apache.commons.codec.binary.Hex;

public class WynntilsAccountManager extends CoreManager {
    private static final String NO_TOKEN = "<no token>";

    private static String token = NO_TOKEN;
    private static boolean loggedIn = false;

    private static final HashMap<String, String> encodedConfigs = new HashMap<>();
    private static final HashMap<String, String> md5Verifications = new HashMap<>();

    public static void init() {
        login();
    }

    private static void login() {
        if (loggedIn) return;

        doLogin();

        if (!loggedIn) {
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

    public static String getToken() {
        return token;
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public HashMap<String, String> getEncodedConfigs() {
        return encodedConfigs;
    }

    public void dumpEncodedConfig(String name) {
        encodedConfigs.remove(name);
    }

    private static void doLogin() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        // generating secret key
        ApiResponse apiResponse = NetManager.callApi(UrlId.API_ATHENA_AUTH_PUBLIC_KEY);
        apiResponse.handleJsonObject(json -> {
            String secretKey = parseAndJoinPublicKey(json.get("publicKeyIn").getAsString());

            Map<String, String> arguments = new HashMap<>();
            arguments.put("key", secretKey);
            arguments.put("username", McUtils.mc().getUser().getName());
            arguments.put("version", String.format("A%s %s", WynntilsMod.getVersion(), WynntilsMod.getModLoader()));

            ApiResponse apiResponse2 = NetManager.callApi(UrlId.API_ATHENA_AUTH_RESPONSE, arguments);
            apiResponse2.handleJsonObject(json2 -> {
                token = json2.get("authToken").getAsString(); /* md5 hashes*/
                JsonObject hashes = json2.getAsJsonObject("hashes");
                hashes.entrySet()
                        .forEach((k) ->
                                md5Verifications.put(k.getKey(), k.getValue().getAsString())); /* configurations*/
                JsonObject configFiles = json2.getAsJsonObject("configFiles");
                configFiles
                        .entrySet()
                        .forEach((k) ->
                                encodedConfigs.put(k.getKey(), k.getValue().getAsString()));
                loggedIn = true;
                future.complete(true);
                WynntilsMod.info("Successfully connected to Athena!");
            });
        });
        try {
            // FIXME: Wait up to 5 seconds for login to complete
            // This is not ideal, but the rest of the code assumes that login is done
            // when further down the manager init chain.
            future.get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // if timeout is reached, return false
        }
    }

    private static String parseAndJoinPublicKey(String key) {
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
}
