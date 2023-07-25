/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Services;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.PlayerJoinedWorldEvent;
import com.wynntils.mc.event.PlayerTeamEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
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
    private final Map<UUID, String> nameMap = new ConcurrentHashMap<>();

    public PlayerModel() {
        super(List.of());

        errorCount = 0;
    }

    // Returns true if the player is on the same server and is not a npc
    public boolean isLocalPlayer(Player player) {
        return !isNpc(player) && !(isPlayerGhost(player));
    }

    public boolean isNpc(Player player) {
        StyledText scoreboardName = StyledText.fromString(player.getScoreboardName());
        return isNpc(scoreboardName);
    }

    public boolean isPlayerGhost(Player player) {
        return ghosts.containsKey(player.getUUID());
    }

    public WynntilsUser getUser(UUID uuid) {
        return users.getOrDefault(uuid, null);
    }

    public Stream<String> getAllPlayerNames() {
        return nameMap.values().stream();
    }

    public void reset() {
        errorCount = 0;
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.NOT_CONNECTED) {
            clearUserCache();
        }
        if (event.getNewState() == WorldState.WORLD) {
            clearGhostCache();
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerJoinedWorldEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.getUUID() == null) return;
        StyledText name = StyledText.fromString(player.getGameProfile().getName());
        if (isNpc(name)) return; // avoid player npcs

        loadUser(player.getUUID(), name.getString());
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

    private void loadUser(UUID uuid, String userName) {
        if (fetching.contains(uuid)) return;
        if (errorCount >= MAX_ERRORS) {
            // Athena is having problems, skip this
            return;
        }

        fetching.add(uuid); // temporary, avoid extra loads
        nameMap.put(uuid, userName);

        ApiResponse apiResponse = Managers.Net.callApi(UrlId.API_ATHENA_USER_INFO, Map.of("uuid", uuid.toString()));
        apiResponse.handleJsonObject(
                json -> {
                    if (!json.has("user")) return;

                    WynntilsUser user = WynntilsMod.GSON.fromJson(json.getAsJsonObject("user"), WynntilsUser.class);

                    users.put(uuid, user);
                    fetching.remove(uuid);

                    // Schedule cape loading for next render tick
                    RenderSystem.recordRenderCall(() -> Services.Cosmetics.loadCosmeticTextures(uuid, user));
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
        nameMap.clear();
    }

    private void clearGhostCache() {
        ghosts.clear();
    }

    private boolean isNpc(StyledText name) {
        // FIXME: Maybe make a better check using more native StyledText operations?
        return name.contains("\u0001") || name.contains("§");
    }
}
