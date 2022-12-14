/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.google.gson.JsonObject;
import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
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
        if (fetching.contains(uuid)) return;

        fetching.add(uuid); // temporary, avoid extra loads

        ApiResponse apiResponse = Managers.NET.callApi(UrlId.API_ATHENA_USER_INFO, Map.of("uuid", uuid.toString()));
        apiResponse.handleJsonObject(json -> {
            if (!json.has("user")) return;

            JsonObject user = json.getAsJsonObject("user");
            users.put(
                    uuid,
                    new WynntilsUser(AccountType.valueOf(user.get("accountType").getAsString())));
            fetching.remove(uuid);
        });
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
