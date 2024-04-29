/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.google.gson.JsonObject;
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
import com.wynntils.models.players.type.GuildRank;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.SimpleDateFormatter;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.TimedSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class PlayerModel extends Model {
    private static final SimpleDateFormatter DATE_FORMATTER = new SimpleDateFormatter();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
    private static final String ATHENA_USER_NOT_FOUND = "User not found";
    private static final Pattern GHOST_WORLD_PATTERN = Pattern.compile("^_(\\d+)$");

    // If there is a failure with the API, give it time to recover
    private static final int ERROR_TIMEOUT_MINUTE = 5;

    // Max amount of overall lookup errors
    private static final int MAX_ERRORS = 5;

    // Max amount of errors for a single user,
    // before disabling lookups for them
    private static final int MAX_USER_ERRORS = 3;

    private final Map<UUID, WynntilsUser> users = new ConcurrentHashMap<>();
    private final Set<UUID> usersWithoutWynntilsAccount = ConcurrentHashMap.newKeySet();
    private final Set<UUID> fetching = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> ghosts = new ConcurrentHashMap<>();
    private final Map<UUID, String> nameMap = new ConcurrentHashMap<>();

    // The size of this set is the count of errors in the last ERROR_TIMEOUT_MINUTE minutes.
    // This is used to avoid spamming the API.
    private final TimedSet<Object> errors =
            new TimedSet<>(ERROR_TIMEOUT_MINUTE, TimeUnit.MINUTES, true, ConcurrentHashMap::newKeySet);
    private final Map<UUID, Integer> userFailures = new ConcurrentHashMap<>();

    public PlayerModel() {
        super(List.of());
        errors.clear();
        userFailures.clear();
    }

    // Returns true if the player is on the same server and is not a npc
    public boolean isLocalPlayer(Player player) {
        return !isNpc(player) && !isPlayerGhost(player);
    }

    public boolean isLocalPlayer(String name) {
        // Wynn uses TeamNames for player names that are online
        return !isNpcName(StyledText.fromString(name))
                && McUtils.mc().level.getScoreboard().getTeamNames().contains(name);
    }

    public boolean isNpc(Player player) {
        StyledText scoreboardName = StyledText.fromString(player.getScoreboardName());
        return isNpcName(scoreboardName) || isNpcUuid(player.getUUID());
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
        fetching.clear();
        errors.clear();
        userFailures.clear();
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.NOT_CONNECTED) {
            clearNameMap();
            reset();
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
        if (!isLocalPlayer(player)) return; // avoid player npcs

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
        // Avoid fetching the same user multiple times
        if (fetching.contains(uuid)) return;
        if (users.containsKey(uuid) || usersWithoutWynntilsAccount.contains(uuid)) return;

        // Call getEntries to clear old entries
        if (errors.getEntries().size() >= MAX_ERRORS) {
            // Athena is having problems, skip this
            return;
        }

        if (userFailures.getOrDefault(uuid, 0) >= MAX_USER_ERRORS) {
            // User has had too many failures, skip this
            return;
        }

        fetching.add(uuid); // temporary, avoid extra loads
        nameMap.put(uuid, userName);

        ApiResponse apiResponse = Managers.Net.callApi(UrlId.API_ATHENA_USER_INFO, Map.of("uuid", uuid.toString()));
        apiResponse.handleJsonObject(
                json -> {
                    if (json.has("message") && json.get("message").getAsString().equals(ATHENA_USER_NOT_FOUND)) {
                        // This user does not exist in our database, stop requesting it
                        usersWithoutWynntilsAccount.add(uuid);
                        fetching.remove(uuid);
                        return;
                    }

                    if (!json.has("user")) {
                        fetching.remove(uuid);
                        saveUserFailures(uuid, userName);
                        return;
                    }

                    WynntilsUser user = WynntilsMod.GSON.fromJson(json.getAsJsonObject("user"), WynntilsUser.class);

                    users.put(uuid, user);
                    fetching.remove(uuid);

                    // Schedule cape loading for next render tick
                    RenderSystem.recordRenderCall(() -> Services.Cosmetics.loadCosmeticTextures(uuid, user));
                },
                onError -> {
                    errors.put(System.currentTimeMillis());

                    saveUserFailures(uuid, userName);
                });
    }

    public CompletableFuture<MutableComponent> getPlayerGuild(String username) {
        CompletableFuture<MutableComponent> future = new CompletableFuture<>();

        ApiResponse playerApiResponse = Managers.Net.callApi(UrlId.DATA_WYNNCRAFT_PLAYER, Map.of("username", username));
        playerApiResponse.handleJsonObject(
                playerJson -> {
                    if (playerJson.has("Error")) {
                        future.complete(
                                Component.literal("Unknown player " + username).withStyle(ChatFormatting.RED));
                    } else if (!playerJson.has("username")) { // Handles multi selector
                        // Display all UUID's for known players with this username
                        // with click events to run the command with the UUID instead.
                        // Multi selector doesn't give any other identifiable
                        // information besides rank which doesn't really help
                        MutableComponent response = Component.literal("Multiple players found with the username ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(username).withStyle(ChatFormatting.RED))
                                .append(Component.literal(":").withStyle(ChatFormatting.GRAY));

                        for (String uuid : playerJson.keySet()) {
                            MutableComponent current = Component.literal("\n")
                                    .append(Component.literal(uuid)
                                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.UNDERLINE));

                            current.withStyle(style -> style.withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/player guild " + uuid)));

                            response.append(current);
                        }

                        future.complete(response);
                    } else {
                        MutableComponent response = Component.literal(
                                        playerJson.get("username").getAsString())
                                .withStyle(ChatFormatting.DARK_AQUA);

                        if (!playerJson.get("guild").isJsonNull()) {
                            JsonObject guildInfo = playerJson.getAsJsonObject("guild");
                            String name = guildInfo.get("name").getAsString();
                            String prefix = guildInfo.get("prefix").getAsString();

                            GuildRank guildRank =
                                    GuildRank.fromName(guildInfo.get("rank").getAsString());

                            response.append(Component.literal(" is a ")
                                    .withStyle(ChatFormatting.GRAY)
                                    .append(Component.literal(guildRank.getGuildDescription())
                                            .withStyle(ChatFormatting.AQUA)
                                            .append(Component.literal(" of ")
                                                    .withStyle(ChatFormatting.GRAY)
                                                    .append(Component.literal(name + " [" + prefix + "]")
                                                            .withStyle(ChatFormatting.AQUA)))));

                            ApiResponse guildApiResponse =
                                    Managers.Net.callApi(UrlId.DATA_WYNNCRAFT_GUILD, Map.of("name", name));
                            guildApiResponse.handleJsonObject(
                                    guildJson -> {
                                        if (!guildJson.has("name")) {
                                            future.complete(response);
                                            return;
                                        }

                                        String joined = guildJson
                                                .getAsJsonObject("members")
                                                .getAsJsonObject(
                                                        guildRank.getName().toLowerCase(Locale.ROOT))
                                                .getAsJsonObject(playerJson
                                                        .get("username")
                                                        .getAsString())
                                                .get("joined")
                                                .getAsString();

                                        try {
                                            Date joinedDate = DATE_FORMAT.parse(joined);
                                            long differenceInMillis = System.currentTimeMillis() - joinedDate.getTime();

                                            response.append(Component.literal("\nThey have been in the guild for ")
                                                    .withStyle(ChatFormatting.GRAY)
                                                    .append(Component.literal(DATE_FORMATTER.format(differenceInMillis))
                                                            .withStyle(ChatFormatting.AQUA)));
                                        } catch (ParseException e) {
                                            WynntilsMod.error(
                                                    "Error when trying to parse player joined guild date.", e);
                                        }

                                        future.complete(response);
                                    },
                                    onError -> future.complete(response));
                        } else {
                            response.append(
                                    Component.literal(" is not in a guild").withStyle(ChatFormatting.GRAY));

                            future.complete(response);
                        }
                    }
                },
                onError -> future.complete(Component.literal("Unable to get player guild for " + username)
                        .withStyle(ChatFormatting.RED)));

        return future;
    }

    public CompletableFuture<MutableComponent> getPlayerLastSeen(String username) {
        CompletableFuture<MutableComponent> future = new CompletableFuture<>();

        ApiResponse playerApiResponse = Managers.Net.callApi(UrlId.DATA_WYNNCRAFT_PLAYER, Map.of("username", username));
        playerApiResponse.handleJsonObject(
                playerJson -> {
                    if (playerJson.has("Error")) {
                        future.complete(
                                Component.literal("Unknown player " + username).withStyle(ChatFormatting.RED));
                    } else if (!playerJson.has("username")) { // Handles multi selector
                        // Display all UUID's for known players with this username
                        // with click events to run the command with the UUID instead.
                        // Multi selector doesn't give any other identifiable
                        // information besides rank which doesn't really help
                        MutableComponent response = Component.literal("Multiple players found with the username ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(username).withStyle(ChatFormatting.RED))
                                .append(Component.literal(":").withStyle(ChatFormatting.GRAY));

                        for (String uuid : playerJson.keySet()) {
                            MutableComponent current = Component.literal("\n")
                                    .append(Component.literal(uuid)
                                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.UNDERLINE));

                            current.withStyle(style -> style.withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/player lastseen " + uuid)));

                            response.append(current);
                        }

                        future.complete(response);
                    } else {
                        MutableComponent response;

                        if (playerJson.get("online").getAsBoolean()) {
                            response = Component.literal(
                                            playerJson.get("username").getAsString())
                                    .withStyle(ChatFormatting.AQUA)
                                    .append(Component.literal(" is online on ")
                                            .withStyle(ChatFormatting.GRAY)
                                            .append(Component.literal(playerJson
                                                            .get("server")
                                                            .getAsString())
                                                    .withStyle(ChatFormatting.GOLD)));
                        } else {
                            try {
                                Date joinedDate = DATE_FORMAT.parse(
                                        playerJson.get("lastJoin").getAsString());
                                long differenceInMillis = System.currentTimeMillis() - joinedDate.getTime();

                                response = Component.literal(
                                                playerJson.get("username").getAsString())
                                        .withStyle(ChatFormatting.AQUA)
                                        .append(Component.literal(" was last seen ")
                                                .withStyle(ChatFormatting.GRAY))
                                        .append(Component.literal(DATE_FORMATTER.format(differenceInMillis))
                                                .withStyle(ChatFormatting.GOLD)
                                                .append(Component.literal("ago").withStyle(ChatFormatting.GRAY)));
                            } catch (ParseException e) {
                                WynntilsMod.error("Error when trying to parse player last join.", e);
                                response = Component.literal("Failed to get player last seen")
                                        .withStyle(ChatFormatting.RED);
                            }
                        }

                        future.complete(response);
                    }
                },
                onError -> future.complete(Component.literal("Unable to get player last seen for " + username)
                        .withStyle(ChatFormatting.RED)));

        return future;
    }

    private void saveUserFailures(UUID uuid, String userName) {
        userFailures.putIfAbsent(uuid, 0);
        userFailures.compute(uuid, (k, v) -> v + 1);

        // Only log the error once
        // Call getEntries to clear old entries
        if (errors.getEntries().size() == MAX_ERRORS) {
            WynntilsMod.error("Athena user lookup has repeating failures. Disabling future lookups temporarily.");
        }

        // Only log the error once
        if (userFailures.get(uuid) == MAX_USER_ERRORS) {
            WynntilsMod.error("Athena user lookup has repeating failures for user " + userName
                    + ". Disabling future lookups for the user, until a reset.");
        }
    }

    private void clearNameMap() {
        nameMap.clear();
    }

    private void clearGhostCache() {
        ghosts.clear();
    }

    private boolean isNpcName(StyledText name) {
        // FIXME: Maybe make a better check using more native StyledText operations?
        return name.contains("\u0001") || name.contains("§");
    }

    private boolean isNpcUuid(UUID uuid) {
        // All players have UUID version 4,
        // while most NPCs have UUID version 2
        // Starting Wynncraft 2.1, all NPCs will have UUID version 2
        return uuid.version() == 2;
    }
}
