/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.profiles;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TerritoryProfile {

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    String name;
    String friendlyName;
    int startX;
    int startZ;
    int endX;
    int endZ;

    String guild;
    String guildPrefix;
    String guildColor;
    String attacker;
    Date acquired;

    int level;

    public TerritoryProfile(
            String name,
            String friendlyName,
            String guildPrefix,
            String guildColor,
            int level,
            int startX,
            int startZ,
            int endX,
            int endZ,
            String guild,
            String attacker,
            Date acquired) {
        this.name = name;
        this.friendlyName = friendlyName;

        this.level = level;

        this.guildPrefix = guildPrefix;
        this.guildColor = guildColor;
        this.guild = guild;
        this.attacker = attacker;

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

    public String getGuildColor() {
        return guildColor;
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

    public int getLevel() {
        return level;
    }

    public String getAttacker() {
        return attacker;
    }

    public Date getAcquired() {
        return acquired;
    }

    public boolean insideArea(int playerX, int playerZ) {
        return startX <= playerX && endX >= playerX && startZ <= playerZ && endZ >= playerZ;
    }

    public static class TerritoryDeserializer implements JsonDeserializer<TerritoryProfile> {

        @Override
        public TerritoryProfile deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject territory = json.getAsJsonObject();
            int startX = Integer.MAX_VALUE - 1;
            int startZ = Integer.MAX_VALUE - 1;
            int endX = Integer.MAX_VALUE;
            int endZ = Integer.MAX_VALUE;
            if (territory.has("location")) {
                JsonObject location = territory.getAsJsonObject("location");
                startX = location.get("startX").getAsInt();
                startZ = location.get("startZ").getAsInt();
                endX = location.get("endX").getAsInt();
                endZ = location.get("endZ").getAsInt();
            }
            String territoryName = territory.get("territory").getAsString();
            String friendlyName = territoryName.replace('’', '\'');

            String guild;
            if (territory.get("guild").isJsonNull()) guild = "Unknown";
            else guild = territory.get("guild").getAsString();

            Date acquired = null;
            try {
                acquired = dateFormat.parse(territory.get("acquired").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String attacker = null;
            if (!territory.get("attacker").isJsonNull()) {
                attacker = territory.get("attacker").getAsString();
            }

            String guildPrefix;
            if (territory.get("guildPrefix").isJsonNull()) guildPrefix = "UNK";
            else guildPrefix = territory.get("guildPrefix").getAsString();

            int level = territory.get("level").getAsInt();

            String guildColor;
            if (territory.get("guildColor").getAsString().isEmpty()) guildColor = null;
            else guildColor = territory.get("guildColor").getAsString();

            return new TerritoryProfile(
                    territoryName,
                    friendlyName,
                    guildPrefix,
                    guildColor,
                    level,
                    startX,
                    startZ,
                    endX,
                    endZ,
                    guild,
                    attacker,
                    acquired);
        }
    }
}
