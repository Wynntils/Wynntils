/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.components.Models;
import com.wynntils.models.guild.type.GuildInfo;
import com.wynntils.models.guild.type.GuildMemberInfo;
import com.wynntils.models.guild.type.GuildRank;
import com.wynntils.utils.mc.McUtils;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public record WynnPlayerInfo(
        String username,
        boolean online,
        String server,
        Instant lastJoinTimestamp,
        String guildName,
        String guildPrefix,
        GuildRank guildRank,
        Instant guildJoinTimestamp) {
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
                Instant lastJoinTimestamp =
                        Instant.parse(jsonObject.get("lastJoin").getAsString());

                if (!jsonObject.get("guild").isJsonNull()) {
                    JsonObject guildInfo = jsonObject.getAsJsonObject("guild");

                    String guildName = guildInfo.get("name").getAsString();
                    String guildPrefix = guildInfo.get("prefix").getAsString();
                    GuildRank guildRank =
                            GuildRank.fromName(guildInfo.get("rank").getAsString());

                    CompletableFuture<GuildInfo> completableFuture = Models.Guild.getGuild(guildName);

                    GuildInfo guild;

                    try {
                        guild = completableFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        McUtils.sendMessageToClient(Component.literal("Error trying to parse player guild")
                                .withStyle(ChatFormatting.RED));
                        return null;
                    }

                    Instant guildJoinTimestamp = null;

                    if (guild != null) {
                        Optional<String> guildJoinedTimestampOpt = guild.guildMembers().stream()
                                .filter(guildMember -> guildMember.username().equals(playerUsername))
                                .map(GuildMemberInfo::joinTimestamp)
                                .findFirst();

                        guildJoinTimestamp = Instant.parse(guildJoinedTimestampOpt.orElse(null));
                    }

                    return new WynnPlayerInfo(
                            playerUsername,
                            online,
                            onlineServer,
                            lastJoinTimestamp,
                            guildName,
                            guildPrefix,
                            guildRank,
                            guildJoinTimestamp);
                } else {
                    return new WynnPlayerInfo(
                            playerUsername, online, onlineServer, lastJoinTimestamp, null, null, null, null);
                }
            }
        }
    }
}
