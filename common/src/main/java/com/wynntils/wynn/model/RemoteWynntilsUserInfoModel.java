/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.google.gson.JsonObject;
import com.wynntils.core.managers.Model;
import com.wynntils.core.net.athena.WynntilsAccountManager;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.request.PostRequestBuilder;
import com.wynntils.core.webapi.request.Request;
import com.wynntils.mc.event.PlayerJoinedWorldEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.objects.account.AccountType;
import com.wynntils.wynn.objects.account.WynntilsUser;
import com.wynntils.wynn.utils.WynnPlayerUtils;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RemoteWynntilsUserInfoModel extends Model {
    public static void init() {}

    private static final Map<UUID, WynntilsUser> users = new ConcurrentHashMap<>();
    private static final Set<UUID> fetching = ConcurrentHashMap.newKeySet();

    public static void loadUser(UUID uuid) {
        if (!WynntilsAccountManager.isLoggedIn() || WebManager.getApiUrls().isEmpty()) return;
        if (fetching.contains(uuid)) return;

        fetching.add(uuid); // temporary, avoid extra loads

        JsonObject body = new JsonObject();
        body.addProperty("uuid", uuid.toString());

        Request req = new PostRequestBuilder(
                        WebManager.getApiUrls().get().get("Athena") + "/user/getInfo", "getInfo(" + uuid + ")")
                .postJsonElement(body)
                .handleJsonObject(json -> {
                    if (!json.has("user")) return false;

                    JsonObject user = json.getAsJsonObject("user");
                    users.put(
                            uuid,
                            new WynntilsUser(
                                    AccountType.valueOf(user.get("accountType").getAsString())));
                    fetching.remove(uuid);

                    return true;
                })
                .onError(() -> {})
                .build();

        WebManager.getHandler().addAndDispatch(req, true);
    }

    public static WynntilsUser getUser(UUID uuid) {
        return users.getOrDefault(uuid, null);
    }

    private static void clearUserCache() {
        users.clear();
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        switch (event.getNewState()) {
            case NOT_CONNECTED, CONNECTING -> clearUserCache();
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerJoinedWorldEvent event) {
        if (event.getPlayerId() == null || event.getPlayerInfo() == null) return;
        String name = event.getPlayerInfo().getProfile().getName();
        if (WynnPlayerUtils.isNpc(name)) return; // avoid player npcs

        loadUser(event.getPlayerId());
    }
}
