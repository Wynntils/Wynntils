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
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.utils.mc.McUtils;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public record WynnPlayerInfo(
        String username,
        boolean online,
        String server,
        Date lastJoinTimestamp,
        String guildName,
        String guildPrefix,
        GuildRank guildRank,
        Date guildJoinTimestamp) {
    public static class WynnPlayerInfoDeserializer implements JsonDeserializer<WynnPlayerInfo> {
        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);

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
                Date lastJoinDate = null;

                try {
                    lastJoinDate = DATE_FORMAT.parse(jsonObject.get("lastJoin").getAsString());
                } catch (ParseException e) {
                    WynntilsMod.error("Error when trying to player last join date.", e);
                }

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

                    Date guildJoinedDate = null;

                    if (guild != null) {
                        Optional<String> guildJoinedTimestampOpt = guild.guildMembers().stream()
                                .filter(guildMember -> guildMember.username().equals(playerUsername))
                                .map(GuildMemberInfo::joinTimestamp)
                                .findFirst();

                        String guildJoinTimestamp = guildJoinedTimestampOpt.orElse(null);

                        if (guildJoinTimestamp != null) {
                            try {
                                guildJoinedDate = DATE_FORMAT.parse(guildJoinTimestamp);
                            } catch (ParseException e) {
                                WynntilsMod.error("Error when trying to parse player guild join date.", e);
                            }
                        }
                    }

                    return new WynnPlayerInfo(
                            playerUsername,
                            online,
                            onlineServer,
                            lastJoinDate,
                            guildName,
                            guildPrefix,
                            guildRank,
                            guildJoinedDate);
                } else {
                    return new WynnPlayerInfo(
                            playerUsername, online, onlineServer, lastJoinDate, null, null, null, null);
                }
            }
        }
    }
}
