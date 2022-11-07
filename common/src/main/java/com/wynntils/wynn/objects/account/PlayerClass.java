/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.account;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerClass {

    private String name;
    private int level;
    private Map<String, Integer> dungeonsCompleted;
    private List<String> questsCompleted;
    private int itemsIdentified;
    private int mobsKilled;
    private int pvpKills;
    private int pvpDeaths;
    private int chestsFound;
    private long blocksWalked;
    private int logins;
    private int deaths;
    private int playtime;
    private int skillStrength;
    private int skillDexterity;
    private int skillIntelligence;
    private int skillDefence;
    private int skillAgility;
    private int alchemismLevel;
    private float alchemismXp;
    private int armouringLevel;
    private float armouringXp;
    private int combatLevel;
    private float combatXp;
    private int cookingLevel;
    private float cookingXp;
    private int farmingLevel;
    private float farmingXp;
    private int fishingLevel;
    private float fishingXp;
    private int jewelingLevel;
    private float jewelingXp;
    private int miningLevel;
    private float miningXp;
    private int scribingLevel;
    private float scribingXp;
    private int tailoringLevel;
    private float tailoringXp;
    private int weaponsmithingLevel;
    private float weaponsmithingXp;
    private int woodcuttingLevel;
    private float woodcuttingXp;
    private int woodworkingLevel;
    private float woodworkingXp;
    private int discoveries;
    private int eventsWon;
    private boolean preEconomyUpdate;

    public PlayerClass(
            String name,
            int level,
            Map<String, Integer> dungeonsCompleted,
            ArrayList<String> questsCompleted,
            int itemsIdentified,
            int mobsKilled,
            int pvpKills,
            int pvpDeaths,
            int chestsFound,
            long blocksWalked,
            int logins,
            int deaths,
            int playtime,
            int skillStrength,
            int skillDexterity,
            int skillIntelligence,
            int skillDefence,
            int skillAgility,
            int alchemismLevel,
            float alchemismXp,
            int armouringLevel,
            float armouringXp,
            int combatLevel,
            float combatXp,
            int cookingLevel,
            float cookingXp,
            int farmingLevel,
            float farmingXp,
            int fishingLevel,
            float fishingXp,
            int jewelingLevel,
            float jewelingXp,
            int miningLevel,
            float miningXp,
            int scribingLevel,
            float scribingXp,
            int tailoringLevel,
            float tailoringXp,
            int weaponsmithingLevel,
            float weaponsmithingXp,
            int woodcuttingLevel,
            float woodcuttingXp,
            int woodworkingLevel,
            float woodworkingXp,
            int discoveries,
            int eventsWon,
            boolean preEconomyUpdate) {
        this.name = name;
        this.level = level;
        this.dungeonsCompleted = dungeonsCompleted;
        this.questsCompleted = questsCompleted;
        this.itemsIdentified = itemsIdentified;
        this.mobsKilled = mobsKilled;
        this.pvpKills = pvpKills;
        this.pvpDeaths = pvpDeaths;
        this.chestsFound = chestsFound;
        this.blocksWalked = blocksWalked;
        this.logins = logins;
        this.deaths = deaths;
        this.playtime = playtime;
        this.skillStrength = skillStrength;
        this.skillDexterity = skillDexterity;
        this.skillIntelligence = skillIntelligence;
        this.skillDefence = skillDefence;
        this.skillAgility = skillAgility;
        this.alchemismLevel = alchemismLevel;
        this.alchemismXp = alchemismXp;
        this.armouringLevel = armouringLevel;
        this.armouringXp = armouringXp;
        this.combatLevel = combatLevel;
        this.combatXp = combatXp;
        this.cookingLevel = cookingLevel;
        this.cookingXp = cookingXp;
        this.farmingLevel = farmingLevel;
        this.farmingXp = farmingXp;
        this.fishingLevel = fishingLevel;
        this.fishingXp = fishingXp;
        this.jewelingLevel = jewelingLevel;
        this.jewelingXp = jewelingXp;
        this.miningLevel = miningLevel;
        this.miningXp = miningXp;
        this.scribingLevel = scribingLevel;
        this.scribingXp = scribingXp;
        this.tailoringLevel = tailoringLevel;
        this.tailoringXp = tailoringXp;
        this.weaponsmithingLevel = weaponsmithingLevel;
        this.weaponsmithingXp = weaponsmithingXp;
        this.woodcuttingLevel = woodcuttingLevel;
        this.woodcuttingXp = woodcuttingXp;
        this.woodworkingLevel = woodworkingLevel;
        this.woodworkingXp = woodworkingXp;
        this.discoveries = discoveries;
        this.eventsWon = eventsWon;
        this.preEconomyUpdate = preEconomyUpdate;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public Map<String, Integer> getDungeonsCompleted() {
        return dungeonsCompleted;
    }

    public List<String> getQuestsCompleted() {
        return questsCompleted;
    }

    public int getItemsIdentified() {
        return itemsIdentified;
    }

    public int getMobsKilled() {
        return mobsKilled;
    }

    public int getPvpKills() {
        return pvpKills;
    }

    public int getPvpDeaths() {
        return pvpDeaths;
    }

    public int getChestsFound() {
        return chestsFound;
    }

    public long getBlocksWalked() {
        return blocksWalked;
    }

    public int getLogins() {
        return logins;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getPlaytime() {
        return playtime;
    }

    public int getSkillStrength() {
        return skillStrength;
    }

    public int getSkillDexterity() {
        return skillDexterity;
    }

    public int getSkillIntelligence() {
        return skillIntelligence;
    }

    public int getSkillDefence() {
        return skillDefence;
    }

    public int getSkillAgility() {
        return skillAgility;
    }

    public int getAlchemismLevel() {
        return alchemismLevel;
    }

    public float getAlchemismXp() {
        return alchemismXp;
    }

    public int getArmouringLevel() {
        return armouringLevel;
    }

    public float getArmouringXp() {
        return armouringXp;
    }

    public int getCombatLevel() {
        return combatLevel;
    }

    public float getCombatXp() {
        return combatXp;
    }

    public int getCookingLevel() {
        return cookingLevel;
    }

    public float getCookingXp() {
        return cookingXp;
    }

    public int getFarmingLevel() {
        return farmingLevel;
    }

    public float getFarmingXp() {
        return farmingXp;
    }

    public int getFishingLevel() {
        return fishingLevel;
    }

    public float getFishingXp() {
        return fishingXp;
    }

    public int getJewelingLevel() {
        return jewelingLevel;
    }

    public float getJewelingXp() {
        return jewelingXp;
    }

    public int getMiningLevel() {
        return miningLevel;
    }

    public float getMiningXp() {
        return miningXp;
    }

    public int getScribingLevel() {
        return scribingLevel;
    }

    public float getScribingXp() {
        return scribingXp;
    }

    public int getTailoringLevel() {
        return tailoringLevel;
    }

    public float getTailoringXp() {
        return tailoringXp;
    }

    public int getWeaponsmithingLevel() {
        return weaponsmithingLevel;
    }

    public float getWeaponsmithingXp() {
        return weaponsmithingXp;
    }

    public int getWoodcuttingLevel() {
        return woodcuttingLevel;
    }

    public float getWoodcuttingXp() {
        return woodcuttingXp;
    }

    public int getWoodworkingLevel() {
        return woodworkingLevel;
    }

    public float getWoodworkingXp() {
        return woodworkingXp;
    }

    public int getDiscoveries() {
        return discoveries;
    }

    public int getEventsWon() {
        return eventsWon;
    }

    public boolean isPreEconomyUpdate() {
        return preEconomyUpdate;
    }

    public static class PlayerClassProfileDeserializer implements JsonDeserializer<PlayerClass> {

        public PlayerClass deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject playerClass = json.getAsJsonObject();
            String name = playerClass.get("name").getAsString();
            int level = playerClass.get("level").getAsInt();

            Map<String, Integer> dungeonsCompleted = new HashMap<>();
            JsonArray dungeonsJson =
                    playerClass.get("dungeons").getAsJsonObject().get("list").getAsJsonArray();
            for (JsonElement dungeonJson : dungeonsJson) {
                JsonObject object = dungeonJson.getAsJsonObject();
                dungeonsCompleted.put(
                        object.get("name").getAsString(),
                        object.get("completed").getAsInt());
            }

            ArrayList<String> questsCompleted = new ArrayList<>();
            JsonArray questsJson =
                    playerClass.get("quests").getAsJsonObject().get("list").getAsJsonArray();
            for (JsonElement element : questsJson) questsCompleted.add(element.getAsString());

            int itemsIdentified = playerClass.get("itemsIdentified").getAsInt();
            int mobsKilled = playerClass.get("mobsKilled").getAsInt();

            JsonObject pvpStats = playerClass.get("pvp").getAsJsonObject();
            int pvpKills = pvpStats.get("kills").getAsInt();
            int pvpDeaths = pvpStats.get("deaths").getAsInt();

            // This field was removed from the Wynncraft API
            // int chestsFound = playerClass.get("chestsFound").getAsInt();
            int chestsFound = -1;
            long blocksWalked = playerClass.get("blocksWalked").getAsLong();
            int logins = playerClass.get("logins").getAsInt();
            int deaths = playerClass.get("deaths").getAsInt();
            int playtime = playerClass.get("playtime").getAsInt();

            JsonObject skills = playerClass.get("skills").getAsJsonObject();
            int skillStrength = skills.get("strength").getAsInt();
            int skillDexterity = skills.get("dexterity").getAsInt();
            int skillIntelligence = skills.get("intelligence").getAsInt();
            int skillDefence = skills.get("defence").getAsInt();
            int skillAgility = skills.get("agility").getAsInt();

            JsonObject professions = playerClass.get("professions").getAsJsonObject();
            JsonObject combat = professions.get("combat").getAsJsonObject();
            int combatLevel = combat.get("level").getAsInt();
            float combatXp = combat.get("xp").getAsFloat();
            int alchemismLevel = 1,
                    armouringLevel = 1,
                    cookingLevel = 1,
                    farmingLevel = 1,
                    fishingLevel = 1,
                    jewelingLevel = 1,
                    miningLevel = 1,
                    scribingLevel = 1,
                    tailoringLevel = 1,
                    weaponsmithingLevel = 1,
                    woodcuttingLevel = 1,
                    woodworkingLevel = 1;
            float alchemismXp = 0,
                    armouringXp = 0,
                    cookingXp = 0,
                    farmingXp = 0,
                    fishingXp = 0,
                    jewelingXp = 0,
                    miningXp = 0,
                    scribingXp = 0,
                    tailoringXp = 0,
                    weaponsmithingXp = 0,
                    woodcuttingXp = 0,
                    woodworkingXp = 0;
            boolean preEconomyUpdate = true;
            if (playerClass.has("preEconomyUpdate")
                    && !playerClass.get("preEconomyUpdate").getAsBoolean()) {
                preEconomyUpdate = false;
                JsonObject alchemism = professions.get("alchemism").getAsJsonObject();
                alchemismLevel = alchemism.get("level").getAsInt();
                alchemismXp = alchemism.get("xp").getAsFloat();
                JsonObject armouring = professions.get("armouring").getAsJsonObject();
                armouringLevel = armouring.get("level").getAsInt();
                armouringXp = armouring.get("xp").getAsFloat();
                JsonObject cooking = professions.get("cooking").getAsJsonObject();
                cookingLevel = cooking.get("level").getAsInt();
                cookingXp = cooking.get("xp").getAsFloat();
                JsonObject farming = professions.get("farming").getAsJsonObject();
                farmingLevel = farming.get("level").getAsInt();
                farmingXp = farming.get("xp").getAsFloat();
                JsonObject fishing = professions.get("fishing").getAsJsonObject();
                fishingLevel = fishing.get("level").getAsInt();
                fishingXp = fishing.get("xp").getAsFloat();
                JsonObject jeweling = professions.get("jeweling").getAsJsonObject();
                jewelingLevel = jeweling.get("level").getAsInt();
                jewelingXp = jeweling.get("xp").getAsFloat();
                JsonObject mining = professions.get("mining").getAsJsonObject();
                miningLevel = mining.get("level").getAsInt();
                miningXp = mining.get("xp").getAsFloat();
                JsonObject scribing = professions.get("scribing").getAsJsonObject();
                scribingLevel = scribing.get("level").getAsInt();
                scribingXp = scribing.get("xp").getAsFloat();
                JsonObject tailoring = professions.get("tailoring").getAsJsonObject();
                tailoringLevel = tailoring.get("level").getAsInt();
                tailoringXp = tailoring.get("xp").getAsFloat();
                JsonObject weaponsmithing = professions.get("weaponsmithing").getAsJsonObject();
                weaponsmithingLevel = weaponsmithing.get("level").getAsInt();
                weaponsmithingXp = weaponsmithing.get("xp").getAsFloat();
                JsonObject woodcutting = professions.get("woodcutting").getAsJsonObject();
                woodcuttingLevel = woodcutting.get("level").getAsInt();
                woodcuttingXp = woodcutting.get("xp").getAsFloat();
                JsonObject woodworking = professions.get("woodworking").getAsJsonObject();
                woodworkingLevel = woodworking.get("level").getAsInt();
                woodworkingXp = woodworking.get("xp").getAsFloat();
            }

            int discoveries = playerClass.get("discoveries").getAsInt();
            int eventsWon = playerClass.get("eventsWon").getAsInt();
            return new PlayerClass(
                    name,
                    level,
                    dungeonsCompleted,
                    questsCompleted,
                    itemsIdentified,
                    mobsKilled,
                    pvpKills,
                    pvpDeaths,
                    chestsFound,
                    blocksWalked,
                    logins,
                    deaths,
                    playtime,
                    skillStrength,
                    skillDexterity,
                    skillIntelligence,
                    skillDefence,
                    skillAgility,
                    alchemismLevel,
                    alchemismXp,
                    armouringLevel,
                    armouringXp,
                    combatLevel,
                    combatXp,
                    cookingLevel,
                    cookingXp,
                    farmingLevel,
                    farmingXp,
                    fishingLevel,
                    fishingXp,
                    jewelingLevel,
                    jewelingXp,
                    miningLevel,
                    miningXp,
                    scribingLevel,
                    scribingXp,
                    tailoringLevel,
                    tailoringXp,
                    weaponsmithingLevel,
                    weaponsmithingXp,
                    woodcuttingLevel,
                    woodcuttingXp,
                    woodworkingLevel,
                    woodworkingXp,
                    discoveries,
                    eventsWon,
                    preEconomyUpdate);
        }
    }
}
