/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type.wynnplayer;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.guild.type.GuildInfo;
import com.wynntils.models.guild.type.GuildMemberInfo;
import com.wynntils.models.guild.type.GuildRank;
import com.wynntils.services.leaderboard.type.LeaderboardType;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public record WynnPlayerInfo(
        String username,
        boolean online,
        String server,
        Optional<UUID> activeCharacter,
        String nickname,
        UUID uuid,
        String rank,
        LegacyRankColor legacyRankColor,
        String supportRank,
        boolean veteran,
        Optional<Instant> lastJoinTimestamp,
        Optional<PlayerGuildInfo> guildInfo,
        Map<LeaderboardType, Integer> leaderboardPlacements,
        Optional<Instant> firstJoinTimestamp,
        Optional<Double> playtime,
        Optional<GlobalData> globalData,
        Map<UUID, CharacterData> characters) {
    public static class WynnPlayerInfoDeserializer implements JsonDeserializer<WynnPlayerInfo> {
        @Override
        public WynnPlayerInfo deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            if (!jsonObject.has("username")) {
                return null;
            } else {
                String playerUsername = jsonObject.get("username").getAsString();
                boolean online = jsonObject.get("online").getAsBoolean();
                String onlineServer = jsonObject.get("server").isJsonNull()
                        ? null
                        : jsonObject.get("server").getAsString();
                Optional<UUID> activeCharacter =
                        jsonObject.get("activeCharacter").isJsonNull()
                                ? Optional.empty()
                                : Optional.of(UUID.fromString(
                                        jsonObject.get("activeCharacter").getAsString()));
                String nickname = jsonObject.get("nickname").isJsonNull()
                        ? null
                        : jsonObject.get("nickname").getAsString();
                UUID playerUuid = UUID.fromString(jsonObject.get("uuid").getAsString());
                String rank = jsonObject.get("rank").getAsString();

                LegacyRankColor legacyRankColor = null;
                if (!jsonObject.get("legacyRankColour").isJsonNull()) {
                    JsonObject legacyRankColorObj = jsonObject.getAsJsonObject("legacyRankColour");

                    legacyRankColor = new LegacyRankColor(
                            CustomColor.fromHexString(
                                    legacyRankColorObj.get("main").getAsString()),
                            CustomColor.fromHexString(
                                    legacyRankColorObj.get("sub").getAsString()));
                }

                String supportRank = jsonObject.get("supportRank").isJsonNull()
                        ? null
                        : jsonObject.get("supportRank").getAsString();
                boolean veteran = !jsonObject.get("veteran").isJsonNull()
                        && jsonObject.get("veteran").getAsBoolean();
                Optional<Instant> lastJoinTimestamp = jsonObject.get("lastJoin").isJsonNull()
                        ? Optional.empty()
                        : Optional.of(Instant.parse(jsonObject.get("lastJoin").getAsString()));

                Optional<PlayerGuildInfo> guildInfo = Optional.empty();

                if (!jsonObject.get("guild").isJsonNull()) {
                    JsonObject guildInfoObj = jsonObject.getAsJsonObject("guild");

                    UUID guildUuid = UUID.fromString(guildInfoObj.get("uuid").getAsString());
                    String guildName = guildInfoObj.get("name").getAsString();
                    String guildPrefix = guildInfoObj.get("prefix").getAsString();
                    GuildRank guildRank =
                            GuildRank.fromName(guildInfoObj.get("rank").getAsString());
                    Optional<Integer> guildContributionRank = Optional.empty();
                    Optional<Long> guildContributedXp = Optional.empty();
                    Optional<Instant> guildJoinTimestamp = Optional.empty();

                    CompletableFuture<GuildInfo> completableFuture = Models.Guild.getGuild(guildName);

                    GuildInfo guild;

                    try {
                        guild = completableFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        McUtils.sendMessageToClient(Component.literal("Error trying to parse player guild")
                                .withStyle(ChatFormatting.RED));
                        guild = null;
                    }

                    if (guild != null) {
                        Optional<GuildMemberInfo> guildMemberInfo = guild.guildMembers().stream()
                                .filter(guildMember -> guildMember.uuid().equals(playerUuid))
                                .findFirst();

                        if (guildMemberInfo.isPresent()) {
                            guildContributionRank =
                                    Optional.of(guildMemberInfo.get().contributionRank());
                            guildContributedXp =
                                    Optional.of(guildMemberInfo.get().contributedXp());
                            guildJoinTimestamp = Optional.of(
                                    Instant.parse(guildMemberInfo.get().joinTimestamp()));
                        } else {
                            WynntilsMod.warn("Could not find player " + playerUsername + " in guild " + guildName);
                        }
                    }

                    guildInfo = Optional.of(new PlayerGuildInfo(
                            guildUuid,
                            guildName,
                            guildPrefix,
                            guildRank,
                            guildContributionRank,
                            guildContributedXp,
                            guildJoinTimestamp));
                }

                JsonObject leaderboardRankingsObj = jsonObject.getAsJsonObject("ranking");
                Map<LeaderboardType, Integer> rankings = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : leaderboardRankingsObj.entrySet()) {
                    LeaderboardType leaderboardType = LeaderboardType.fromKey(entry.getKey());

                    if (leaderboardType == null) {
                        WynntilsMod.warn("Unknown leaderboard type: " + entry.getKey());
                        continue;
                    }

                    rankings.put(leaderboardType, entry.getValue().getAsInt());
                }

                Optional<Instant> firstJoinTimestamp = !jsonObject.has("firstJoin")
                                || jsonObject.get("firstJoin").isJsonNull()
                        ? Optional.empty()
                        : Optional.of(Instant.parse(jsonObject.get("firstJoin").getAsString()));
                Optional<Double> playtime = !jsonObject.has("playtime")
                        ? Optional.empty()
                        : Optional.of(jsonObject.get("playtime").getAsDouble());

                Optional<GlobalData> globalData = Optional.empty();

                if (jsonObject.has("globalData")) {
                    JsonObject globalDataObj = jsonObject.getAsJsonObject("globalData");

                    int contentCompletion =
                            globalDataObj.get("contentCompletion").getAsInt();
                    int wars = globalDataObj.get("wars").getAsInt();
                    int totalLevel = globalDataObj.get("totalLevel").getAsInt();
                    long mobsKilled = globalDataObj.get("mobsKilled").getAsLong();
                    int chestsFound = globalDataObj.get("chestsFound").getAsInt();

                    JsonObject dungeonsObj = globalDataObj.getAsJsonObject("dungeons");
                    int totalDungeons = dungeonsObj.get("total").getAsInt();
                    Map<String, Integer> dungeonCompletions = new HashMap<>();
                    for (Map.Entry<String, JsonElement> entry :
                            dungeonsObj.getAsJsonObject("list").entrySet()) {
                        dungeonCompletions.put(entry.getKey(), entry.getValue().getAsInt());
                    }
                    ContentCompletedData dungeonData = new ContentCompletedData(totalDungeons, dungeonCompletions);

                    JsonObject raidsObj = globalDataObj.getAsJsonObject("raids");
                    int totalRaids = raidsObj.get("total").getAsInt();
                    Map<String, Integer> raidCompletions = new HashMap<>();
                    for (Map.Entry<String, JsonElement> entry :
                            raidsObj.getAsJsonObject("list").entrySet()) {
                        raidCompletions.put(entry.getKey(), entry.getValue().getAsInt());
                    }
                    ContentCompletedData raidData = new ContentCompletedData(totalRaids, raidCompletions);

                    int worldEvents = globalDataObj.get("worldEvents").getAsInt();
                    int lootruns = globalDataObj.get("lootruns").getAsInt();
                    int caves = globalDataObj.get("caves").getAsInt();
                    int completedQuests = globalDataObj.get("completedQuests").getAsInt();

                    JsonObject pvpObj = globalDataObj.getAsJsonObject("pvp");
                    int pvpKills = pvpObj.get("kills").getAsInt();
                    int pvpDeaths = pvpObj.get("deaths").getAsInt();
                    PvpData pvpData = new PvpData(pvpKills, pvpDeaths);

                    globalData = Optional.of(new GlobalData(
                            contentCompletion,
                            wars,
                            totalLevel,
                            mobsKilled,
                            chestsFound,
                            dungeonData,
                            raidData,
                            worldEvents,
                            lootruns,
                            caves,
                            completedQuests,
                            pvpData));
                }

                Map<UUID, CharacterData> characters = new HashMap<>();
                if (jsonObject.has("characters")
                        && !jsonObject.get("characters").isJsonNull()) {
                    JsonObject charactersObj = jsonObject.getAsJsonObject("characters");

                    for (Map.Entry<String, JsonElement> entry : charactersObj.entrySet()) {
                        try {
                            UUID characterUuid = UUID.fromString(entry.getKey());
                            JsonObject characterObj = entry.getValue().getAsJsonObject();

                            Type characterType = new TypeToken<CharacterData>() {}.getType();
                            CharacterData characterData =
                                    Models.Player.PLAYER_GSON.fromJson(characterObj, characterType);

                            characters.put(characterUuid, characterData);
                        } catch (Exception e) {
                            WynntilsMod.warn("Failed to parse character " + entry.getKey() + ": " + e.getMessage());
                        }
                    }
                }

                return new WynnPlayerInfo(
                        playerUsername,
                        online,
                        onlineServer,
                        activeCharacter,
                        nickname,
                        playerUuid,
                        rank,
                        legacyRankColor,
                        supportRank,
                        veteran,
                        lastJoinTimestamp,
                        guildInfo,
                        rankings,
                        firstJoinTimestamp,
                        playtime,
                        globalData,
                        characters);
            }
        }
    }
}
