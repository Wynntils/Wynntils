/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.guild.type;

import com.wynntils.models.territories.type.GuildResource;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class DiplomacyInfo {
    private final Map<GuildResource, Integer> sentTributes = new EnumMap<>(GuildResource.class);
    private final Map<GuildResource, Integer> receivedTributes = new EnumMap<>(GuildResource.class);
    private final String alliedGuildName;

    public DiplomacyInfo(String alliedGuildName) {
        this.alliedGuildName = alliedGuildName;
    }

    public String getAlliedGuildName() {
        return alliedGuildName;
    }

    public Map<GuildResource, Integer> getSentTributes() {
        return Collections.unmodifiableMap(sentTributes);
    }

    public Map<GuildResource, Integer> getReceivedTributes() {
        return Collections.unmodifiableMap(receivedTributes);
    }

    public void storeSentTribute(GuildResource resource, int amount) {
        sentTributes.put(resource, amount);
    }

    public void storeReceivedTribute(GuildResource resource, int amount) {
        receivedTributes.put(resource, amount);
    }

    public void removeSentTribute(GuildResource resource) {
        sentTributes.remove(resource);
    }

    public void removeReceivedTribute(GuildResource resource) {
        receivedTributes.remove(resource);
    }
}
