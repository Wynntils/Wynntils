/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.mc.event.PlayerJoinedWorldEvent;
import com.wynntils.mc.event.PlayerTeamEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.models.players.type.AccountType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class PlayerModel extends Model {
    private static final Pattern GHOST_WORLD_PATTERN = Pattern.compile("^_(\\d+)$");
    private static final int MAX_ERRORS = 5;

    private final Map<UUID, WynntilsUser> users = new ConcurrentHashMap<>();
    private final Set<UUID> fetching = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> ghosts = new ConcurrentHashMap<>();
    private int errorCount;

    @Override
    public void init() {
        errorCount = 0;
    }

    // Returns true if the player is on the same server and is not a npc
    public boolean isLocalPlayer(Player player) {
        return !isNpc(player) && !(isPlayerGhost(player));
    }

    public boolean isNpc(Player player) {
        String scoreboardName = player.getScoreboardName();
        return isNpc(scoreboardName);
    }

    public boolean isPlayerGhost(Player player) {
        return ghosts.containsKey(player.getUUID());
    }

    public WynntilsUser getUser(UUID uuid) {
        return users.getOrDefault(uuid, null);
    }

    public void reset() {
        errorCount = 0;
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        switch (event.getNewState()) {
            case NOT_CONNECTED, CONNECTING -> clearUserCache();
        }
        if (event.getNewState() == WorldState.WORLD) {
            clearGhostCache();
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerJoinedWorldEvent event) {
        UUID uuid = event.getPlayerId();
        if (uuid == null || event.getPlayerInfo() == null) return;
        String name = event.getPlayerInfo().getProfile().getName();
        if (isNpc(name)) return; // avoid player npcs

        loadUser(uuid);
    }

    @SubscribeEvent
    public void onAddPlayerToTeam(PlayerTeamEvent.Added event) {
        PlayerInfo playerInfo = McUtils.mc().getConnection().getPlayerInfo(event.getUsername());
        if (playerInfo == null) return;

        UUID uuid = playerInfo.getProfile().getId();
        if (uuid == null) return;

        PlayerTeam playerTeam = event.getPlayerTeam();

        Matcher matcher = GHOST_WORLD_PATTERN.matcher(playerTeam.getName());
        if (!matcher.matches()) {
            // Maybe it should not be a ghost anymore
            ghosts.remove(uuid);
            return;
        }

        int world = Integer.parseInt(matcher.group(1));
        ghosts.put(uuid, world);
    }

    private void loadUser(UUID uuid) {
        if (fetching.contains(uuid)) return;
        if (errorCount >= MAX_ERRORS) {
            // Athena is having problems, skip this
            return;
        }

        fetching.add(uuid); // temporary, avoid extra loads

        ApiResponse apiResponse = Managers.Net.callApi(UrlId.API_ATHENA_USER_INFO, Map.of("uuid", uuid.toString()));
        apiResponse.handleJsonObject(
                json -> {
                    if (!json.has("user")) return;

                    JsonObject user = json.getAsJsonObject("user");
                    users.put(
                            uuid,
                            new WynntilsUser(
                                    AccountType.valueOf(user.get("accountType").getAsString())));
                    fetching.remove(uuid);
                },
                onError -> {
                    errorCount++;
                    if (errorCount >= MAX_ERRORS) {
                        WynntilsMod.error("Athena user lookup has repeating failures. Disabling future lookups.");
                    }
                });
    }

    private void clearUserCache() {
        users.clear();
    }

    private void clearGhostCache() {
        ghosts.clear();
    }

    private boolean isNpc(String name) {
        return name.contains("\u0001") || name.contains("§");
    }
}
