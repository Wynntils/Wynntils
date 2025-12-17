/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type.wynnplayer;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.models.character.type.CharacterGamemode;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.profession.type.ProfessionProgress;
import com.wynntils.models.profession.type.ProfessionType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record CharacterData(
        ClassType classType,
        boolean reskin,
        String nickname,
        int level,
        long xp,
        int xpPercent,
        int totalLevel,
        Set<CharacterGamemode> gamemodes,
        Optional<Integer> contentCompletion,
        Optional<Integer> wars,
        Optional<Double> playtime,
        Optional<Long> mobsKilled,
        Optional<Integer> chestsFound,
        Optional<Integer> itemsIdentified,
        Optional<Long> blocksWalked, // Stored as an int in the API currently as it often overflows the integer limit
        Optional<Integer> logins,
        Optional<Integer> deaths,
        Optional<Integer> discoveries,
        Optional<PvpData> pvpData,
        Map<Skill, Integer> skillPoints,
        Map<ProfessionType, ProfessionProgress> professions,
        Optional<ContentCompletedData> dungeons,
        Optional<ContentCompletedData> raids,
        Optional<Integer> worldEvents,
        Optional<Integer> lootruns,
        Optional<Integer> caves,
        Set<String> quests) {
    public static class CharacterDataDeserializer implements JsonDeserializer<CharacterData> {
        @Override
        public CharacterData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            ClassType classType = ClassType.fromName(jsonObject.get("type").getAsString());
            boolean reskin = !jsonObject.get("reskin").isJsonNull()
                    && jsonObject.get("reskin").getAsBoolean();
            String nickname = jsonObject.get("nickname").isJsonNull()
                    ? null
                    : jsonObject.get("nickname").getAsString();
            int level = jsonObject.get("level").getAsInt();
            long xp = jsonObject.get("xp").getAsLong();
            int xpPercent = jsonObject.get("xpPercent").getAsInt();
            int totalLevel = jsonObject.get("totalLevel").getAsInt();

            JsonArray gamemodesArray = jsonObject.getAsJsonArray("gamemode");

            Set<CharacterGamemode> gamemodes = new HashSet<>();
            for (JsonElement gamemodeElement : gamemodesArray) {
                gamemodes.add(CharacterGamemode.fromApiName(gamemodeElement.getAsString()));
            }

            Optional<Integer> contentCompletion =
                    jsonObject.get("contentCompletion").isJsonNull()
                            ? Optional.empty()
                            : Optional.of(jsonObject.get("contentCompletion").getAsInt());
            Optional<Integer> wars = jsonObject.get("wars").isJsonNull()
                    ? Optional.empty()
                    : Optional.of(jsonObject.get("wars").getAsInt());
            Optional<Double> playtime = jsonObject.get("playtime").isJsonNull()
                    ? Optional.empty()
                    : Optional.of(jsonObject.get("playtime").getAsDouble());
            Optional<Long> mobsKilled = jsonObject.get("mobsKilled").isJsonNull()
                    ? Optional.empty()
                    : Optional.of(jsonObject.get("mobsKilled").getAsLong());
            Optional<Integer> chestsFound = jsonObject.get("chestsFound").isJsonNull()
                    ? Optional.empty()
                    : Optional.of(jsonObject.get("chestsFound").getAsInt());
            Optional<Integer> itemsIdentified =
                    jsonObject.get("itemsIdentified").isJsonNull()
                            ? Optional.empty()
                            : Optional.of(jsonObject.get("itemsIdentified").getAsInt());
            Optional<Long> blocksWalked = jsonObject.get("blocksWalked").isJsonNull()
                    ? Optional.empty()
                    : Optional.of(jsonObject.get("blocksWalked").getAsLong());
            Optional<Integer> logins = jsonObject.get("logins").isJsonNull()
                    ? Optional.empty()
                    : Optional.of(jsonObject.get("logins").getAsInt());
            Optional<Integer> deaths = jsonObject.get("deaths").isJsonNull()
                    ? Optional.empty()
                    : Optional.of(jsonObject.get("deaths").getAsInt());
            Optional<Integer> discoveries = jsonObject.get("discoveries").isJsonNull()
                    ? Optional.empty()
                    : Optional.of(jsonObject.get("discoveries").getAsInt());

            Optional<PvpData> pvpData = Optional.empty();
            if (jsonObject.has("pvp")) {
                JsonObject pvpObj = jsonObject.getAsJsonObject("pvp");
                int pvpKills = pvpObj.get("kills").isJsonNull()
                        ? 0
                        : pvpObj.get("kills").getAsInt();
                int pvpDeaths = pvpObj.get("deaths").isJsonNull()
                        ? 0
                        : pvpObj.get("deaths").getAsInt();
                pvpData = Optional.of(new PvpData(pvpKills, pvpDeaths));
            }

            JsonObject skillPointsObj = jsonObject.getAsJsonObject("skillPoints");
            Map<Skill, Integer> skillPoints = new HashMap<>();

            // When skill points are hidden, there is either an error message or they are not present at all
            if (skillPointsObj == null || skillPointsObj.isJsonNull() || skillPointsObj.has("error")) {
                skillPoints = Map.of();
            } else {
                for (Map.Entry<String, JsonElement> entry : skillPointsObj.entrySet()) {
                    Skill skill = Skill.fromApiId(entry.getKey());
                    // In the skill points list, defence uses the American spelling
                    if (skill == null && entry.getKey().equals("defense")) {
                        skill = Skill.DEFENCE;
                    }

                    int value = entry.getValue().getAsInt();
                    skillPoints.put(skill, value);
                }
            }

            Map<ProfessionType, ProfessionProgress> professions = new HashMap<>();

            if (jsonObject.has("professions")) {
                JsonObject professionsObj = jsonObject.getAsJsonObject("professions");

                for (Map.Entry<String, JsonElement> entry : professionsObj.entrySet()) {
                    ProfessionType professionType = ProfessionType.fromString(entry.getKey());

                    if (professionType == null) {
                        WynntilsMod.warn("Unknown profession type: " + entry.getKey());
                        continue;
                    }

                    JsonObject professionData = entry.getValue().getAsJsonObject();
                    professions.put(
                            professionType,
                            new ProfessionProgress(
                                    professionData.get("level").getAsInt(),
                                    professionData.get("xpPercent").getAsInt()));
                }
            }

            Optional<ContentCompletedData> dungeonData = Optional.empty();
            if (jsonObject.has("dungeons")) {
                JsonObject dungeonsObj = jsonObject.getAsJsonObject("dungeons");
                int totalDungeons = dungeonsObj.get("total").getAsInt();
                Map<String, Integer> dungeonCompletions = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry :
                        dungeonsObj.getAsJsonObject("list").entrySet()) {
                    dungeonCompletions.put(entry.getKey(), entry.getValue().getAsInt());
                }
                dungeonData = Optional.of(new ContentCompletedData(totalDungeons, dungeonCompletions));
            }

            Optional<ContentCompletedData> raidData = Optional.empty();
            if (jsonObject.has("raids")) {
                JsonObject raidsObj = jsonObject.getAsJsonObject("raids");
                int totalRaids = raidsObj.get("total").getAsInt();
                Map<String, Integer> raidCompletions = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry :
                        raidsObj.getAsJsonObject("list").entrySet()) {
                    raidCompletions.put(entry.getKey(), entry.getValue().getAsInt());
                }
                raidData = Optional.of(new ContentCompletedData(totalRaids, raidCompletions));
            }

            Optional<Integer> worldEvents = jsonObject.get("worldEvents").isJsonNull()
                    ? Optional.empty()
                    : Optional.of(jsonObject.get("worldEvents").getAsInt());
            Optional<Integer> lootruns = jsonObject.get("lootruns").isJsonNull()
                    ? Optional.empty()
                    : Optional.of(jsonObject.get("lootruns").getAsInt());
            Optional<Integer> caves = jsonObject.get("caves").isJsonNull()
                    ? Optional.empty()
                    : Optional.of(jsonObject.get("caves").getAsInt());

            Set<String> quests = new HashSet<>();
            if (jsonObject.has("quests")) {
                JsonArray questsArray = jsonObject.getAsJsonArray("quests");
                for (JsonElement questElement : questsArray) {
                    quests.add(questElement.getAsString());
                }
            }

            return new CharacterData(
                    classType,
                    reskin,
                    nickname,
                    level,
                    xp,
                    xpPercent,
                    totalLevel,
                    gamemodes,
                    contentCompletion,
                    wars,
                    playtime,
                    mobsKilled,
                    chestsFound,
                    itemsIdentified,
                    blocksWalked,
                    logins,
                    deaths,
                    discoveries,
                    pvpData,
                    skillPoints,
                    professions,
                    dungeonData,
                    raidData,
                    worldEvents,
                    lootruns,
                    caves,
                    quests);
        }
    }
}
