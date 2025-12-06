/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.guild.type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record GuildInfo(
        String name,
        String prefix,
        int level,
        int xpPercent,
        int territories,
        long wars,
        Instant createdTimestamp,
        int totalMembers,
        int onlineMembers,
        List<GuildMemberInfo> guildMembers) {
    public static class GuildDeserializer implements JsonDeserializer<GuildInfo> {
        @Override
        public GuildInfo deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            if (!jsonObject.has("name")) {
                return null;
            }

            String name = jsonObject.get("name").getAsString();
            String prefix = jsonObject.get("prefix").getAsString();
            int level = jsonObject.get("level").getAsInt();
            int xpPercent = jsonObject.get("xpPercent").getAsInt();
            int territories = jsonObject.get("territories").getAsInt();
            long wars = jsonObject.get("wars").isJsonNull()
                    ? 0L
                    : jsonObject.get("wars").getAsLong();
            Instant createdTimestamp = Instant.parse(jsonObject.get("created").getAsString());

            JsonObject guildMembersJson = jsonObject.getAsJsonObject("members");

            int totalMembers = guildMembersJson.get("total").getAsInt();
            int onlineMembers = jsonObject.get("online").getAsInt();

            List<GuildMemberInfo> guildMembers = new ArrayList<>();

            for (String rank : guildMembersJson.keySet()) {
                if (rank.equals("total")) continue;

                GuildRank currentGuildRank = GuildRank.fromName(rank);

                JsonObject roleMembers = guildMembersJson.getAsJsonObject(rank);

                for (String username : roleMembers.keySet()) {
                    JsonObject memberInfo = roleMembers.getAsJsonObject(username);
                    UUID uuid = UUID.fromString(memberInfo.get("uuid").getAsString());
                    boolean isOnline = memberInfo.get("online").getAsBoolean();
                    String onlineServer = memberInfo.get("server").isJsonNull()
                            ? null
                            : memberInfo.get("server").getAsString();
                    long contributedXp = memberInfo.get("contributed").getAsLong();
                    int contributionRank = memberInfo.get("contributionRank").getAsInt();
                    String joinedTimestamp = memberInfo.get("joined").getAsString();

                    guildMembers.add(new GuildMemberInfo(
                            uuid,
                            username,
                            currentGuildRank,
                            isOnline,
                            onlineServer,
                            contributedXp,
                            contributionRank,
                            joinedTimestamp));
                }
            }

            return new GuildInfo(
                    name,
                    prefix,
                    level,
                    xpPercent,
                    territories,
                    wars,
                    createdTimestamp,
                    totalMembers,
                    onlineMembers,
                    guildMembers);
        }
    }

    public List<GuildMemberInfo> getOnlineMembersbyRank(GuildRank guildRank) {
        return guildMembers.stream()
                .filter(guildMember -> guildMember.rank() == guildRank && guildMember.online())
                .collect(Collectors.toList());
    }
}
