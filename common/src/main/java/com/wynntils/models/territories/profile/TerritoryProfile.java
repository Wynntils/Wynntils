/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories.profile;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.SimpleDateFormatter;
import com.wynntils.utils.mc.type.PoiLocation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Position;

public class TerritoryProfile {
    private static final SimpleDateFormatter DATE_FORMATTER = new SimpleDateFormatter();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);

    private final String name;
    private final String friendlyName;

    private final TerritoryLocation territoryLocation;

    private final GuildInfo guildInfo;

    private final Date acquired;

    public TerritoryProfile(
            String name, String friendlyName, TerritoryLocation territoryLocation, GuildInfo guildInfo, Date acquired) {
        this.name = name;
        this.friendlyName = friendlyName;
        this.territoryLocation = territoryLocation;
        this.guildInfo = guildInfo;
        this.acquired = acquired;
    }

    public GuildInfo getGuildInfo() {
        return guildInfo;
    }

    public String getName() {
        return name;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public int getStartX() {
        return territoryLocation.startX();
    }

    public int getStartZ() {
        return territoryLocation.startZ();
    }

    public int getEndX() {
        return territoryLocation.endX();
    }

    public int getEndZ() {
        return territoryLocation.endZ();
    }

    public String getGuild() {
        return guildInfo.name();
    }

    public String getGuildPrefix() {
        return guildInfo.prefix();
    }

    public Date getAcquired() {
        return acquired;
    }

    public PoiLocation getCenterLocation() {
        int xMiddle = (this.getStartX() + this.getEndX()) / 2;
        int zMiddle = (this.getStartZ() + this.getEndZ()) / 2;
        return new PoiLocation(xMiddle, 0, zMiddle);
    }

    public boolean insideArea(Position position) {
        return position.x() >= this.getStartX()
                && position.x() <= this.getEndX()
                && position.z() >= this.getStartZ()
                && position.z() <= this.getEndZ();
    }

    private long getTimeHeldInMillis() {
        return new Date().getTime() - this.getAcquired().getTime() + getTimezoneOffset();
    }

    private long getTimezoneOffset() {
        return ((long) new Date().getTimezoneOffset() * 60 * 1000);
    }

    public boolean isOnCooldown() {
        return getTimeHeldInMillis() < 10 * 60 * 1000;
    }

    public String getReadableRelativeTimeAcquired() {
        long difference = getTimeHeldInMillis();
        return DATE_FORMATTER.format(difference);
    }

    public ChatFormatting getTimeAcquiredColor() {
        // 0 - 1 hours > Green
        // 1 hour - 1 day > Yellow
        // 1 day - > Red

        long difference = getTimeHeldInMillis();
        if (difference < 60 * 60 * 1000) {
            return ChatFormatting.GREEN;
        } else if (difference < 24 * 60 * 60 * 1000) {
            return ChatFormatting.YELLOW;
        } else {
            return ChatFormatting.RED;
        }
    }

    public static class TerritoryDeserializer implements JsonDeserializer<TerritoryProfile> {
        @Override
        public TerritoryProfile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject territory = json.getAsJsonObject();

            int startX = Integer.MAX_VALUE - 1;
            int startZ = Integer.MAX_VALUE - 1;
            int endX = Integer.MAX_VALUE;
            int endZ = Integer.MAX_VALUE;
            if (territory.has("location")) {
                JsonObject location = territory.getAsJsonObject("location");

                JsonArray start = location.getAsJsonArray("start");
                startX = start.get(0).getAsInt();
                startZ = start.get(1).getAsInt();

                JsonArray end = location.getAsJsonArray("end");
                endX = end.get(0).getAsInt();
                endZ = end.get(1).getAsInt();

                // Sometimes the start and end coordinates are swapped, so we need to check for that
                if (startX > endX) {
                    int temp = startX;
                    startX = endX;
                    endX = temp;
                }

                if (startZ > endZ) {
                    int temp = startZ;
                    startZ = endZ;
                    endZ = temp;
                }
            }

            TerritoryLocation territoryLocation = new TerritoryLocation(startX, startZ, endX, endZ);

            String territoryName = territory.get("name").getAsString();
            String friendlyName = territoryName.replace('’', '\'');

            GuildInfo guild;
            JsonElement guildJson = territory.get("guild");
            if (guildJson.isJsonNull()
                    || !guildJson.isJsonObject()
                    || guildJson.getAsJsonObject().get("name").isJsonNull()
                    || guildJson.getAsJsonObject().get("prefix").isJsonNull()) {
                guild = GuildInfo.NONE;
            } else {
                guild = context.deserialize(guildJson, GuildInfo.class);
            }

            Date acquired = null;
            try {
                acquired = DATE_FORMAT.parse(territory.get("acquired").getAsString());
            } catch (ParseException e) {
                WynntilsMod.error("Error when trying to parse territory profile data.", e);
            }

            return new TerritoryProfile(territoryName, friendlyName, territoryLocation, guild, acquired);
        }
    }

    public record GuildInfo(String name, String prefix) {
        public static final GuildInfo NONE = new GuildInfo("No owner", "None");
    }

    public record TerritoryLocation(int startX, int startZ, int endX, int endZ) {}
}
