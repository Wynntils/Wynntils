/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories.profile;

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
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    private final String name;
    private final String friendlyName;
    private final int startX;
    private final int startZ;
    private final int endX;
    private final int endZ;

    private final String guild;
    private final String guildPrefix;
    private final Date acquired;

    public TerritoryProfile(
            String name,
            String friendlyName,
            String guildPrefix,
            int startX,
            int startZ,
            int endX,
            int endZ,
            String guild,
            Date acquired) {
        this.name = name;
        this.friendlyName = friendlyName;

        this.guildPrefix = guildPrefix;
        this.guild = guild;

        this.acquired = acquired;

        if (endX < startX) {
            this.startX = endX;
            this.endX = startX;
        } else {
            this.startX = startX;
            this.endX = endX;
        }

        if (endZ < startZ) {
            this.startZ = endZ;
            this.endZ = startZ;
        } else {
            this.startZ = startZ;
            this.endZ = endZ;
        }
    }

    public String getName() {
        return name;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartZ() {
        return startZ;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndZ() {
        return endZ;
    }

    public String getGuild() {
        return guild;
    }

    public String getGuildPrefix() {
        return guildPrefix;
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
                startX = location.get("startX").getAsInt();
                startZ = location.get("startY").getAsInt();
                endX = location.get("endX").getAsInt();
                endZ = location.get("endY").getAsInt();
            }
            String territoryName = territory.get("territory").getAsString();
            String friendlyName = territoryName.replace('’', '\'');

            String guild;
            if (territory.get("guild").isJsonNull()) {
                guild = "Unknown";
            } else {
                guild = territory.get("guild").getAsString();
            }

            Date acquired = null;
            try {
                acquired = DATE_FORMAT.parse(territory.get("acquired").getAsString());
            } catch (ParseException e) {
                WynntilsMod.error("Error when trying to parse territory profile data.", e);
            }

            String guildPrefix;
            if (territory.get("guildPrefix").isJsonNull()) {
                guildPrefix = "UNKNOWN";
            } else {
                guildPrefix = territory.get("guildPrefix").getAsString();
            }

            return new TerritoryProfile(
                    territoryName, friendlyName, guildPrefix, startX, startZ, endX, endZ, guild, acquired);
        }
    }
}
