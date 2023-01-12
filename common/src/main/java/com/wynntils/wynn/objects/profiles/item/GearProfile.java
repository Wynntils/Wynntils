/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

import com.wynntils.wynn.objects.ClassType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GearProfile {
    private final String displayName;
    private final GearTier tier;
    private final boolean identified;
    private int powderAmount;

    private final GearAttackSpeed attackSpeed;

    private final GearInfoContainer itemInfo; // this needs to be named "itemInfo" to match json format
    private final Map<String, String> requirements;

    private final Map<String, String> damageTypes;
    private final Map<String, Integer> defenseTypes;
    private final Map<String, IdentificationProfile> statuses;

    private final List<String> majorIds;

    private final String restriction;
    private String lore;

    private transient List<MajorIdentification> majorIdentifications = new ArrayList<>();

    private transient Map<RequirementType, String> parsedRequirements = null;

    private transient Map<DamageType, String> parsedDamages = null;
    private transient Map<DamageType, Integer> parsedAvgDamages = null;
    private transient int parsedHealth = Integer.MIN_VALUE;
    private transient Map<DamageType, Integer> parsedDefenses = null;

    private transient boolean replacedLore = false;

    public GearProfile(
            String displayName,
            GearTier tier,
            boolean identified,
            GearAttackSpeed attackSpeed,
            GearInfoContainer gearInfo,
            Map<String, String> requirements,
            Map<String, String> damageTypes,
            Map<String, Integer> defenseTypes,
            Map<String, IdentificationProfile> statuses,
            List<String> majorIds,
            String restriction,
            String lore) {
        this.displayName = displayName;
        this.tier = tier;
        this.identified = identified;
        this.attackSpeed = attackSpeed;
        this.itemInfo = gearInfo;
        this.requirements = requirements;
        this.damageTypes = damageTypes;
        this.defenseTypes = defenseTypes;
        this.statuses = statuses;
        this.majorIds = majorIds;
        this.restriction = restriction;
        this.lore = lore;
    }

    public void registerIdTypes() {
        statuses.forEach((key, value) -> value.registerIdType(key));
    }

    public String getDisplayName() {
        return displayName;
    }

    public GearTier getTier() {
        return tier;
    }

    public boolean isIdentified() {
        return identified;
    }

    public int getPowderAmount() {
        return powderAmount;
    }

    public GearAttackSpeed getAttackSpeed() {
        return attackSpeed;
    }

    public GearInfoContainer getGearInfo() {
        return itemInfo;
    }

    private void parseRequirements() {
        if (parsedRequirements != null) return;

        parsedRequirements = new EnumMap<>(RequirementType.class);
        for (Map.Entry<String, String> entry : requirements.entrySet()) {
            RequirementType type = RequirementType.valueOf(entry.getKey().toUpperCase(Locale.ROOT));
            String reqStr = entry.getValue();
            if (reqStr.equals("0")) continue; // no req, ignore
            parsedRequirements.put(type, reqStr);
        }
        if (getClassNeeded() != null) {
            parsedRequirements.put(RequirementType.CLASSTYPE, getClassNeeded().toString());
        }
    }

    public Map<RequirementType, String> getRequirements() {
        parseRequirements();
        return parsedRequirements;
    }

    public Map<String, String> getDamageTypes() {
        return damageTypes;
    }

    private void parseDamages() {
        if (parsedDamages != null) return;

        parsedDamages = new EnumMap<>(DamageType.class);
        parsedAvgDamages = new EnumMap<>(DamageType.class);
        for (Map.Entry<String, String> entry : damageTypes.entrySet()) {
            String dmgStr = entry.getValue();
            DamageType type = DamageType.valueOf(entry.getKey().toUpperCase(Locale.ROOT));
            int n = dmgStr.indexOf('-');
            int avgDamage = Math.round(
                    (Integer.parseInt(dmgStr.substring(0, n)) + Integer.parseInt(dmgStr.substring(n + 1))) / 2f);

            parsedDamages.put(type, dmgStr);
            parsedAvgDamages.put(type, avgDamage);
        }
    }

    public Map<DamageType, String> getDamages() {
        parseDamages();
        return parsedDamages;
    }

    public Map<DamageType, Integer> getAverageDamages() {
        parseDamages();
        return parsedAvgDamages;
    }

    public Map<String, Integer> getDefenseTypes() {
        return defenseTypes;
    }

    private void parseDefenses() {
        if (parsedDefenses != null) return;

        parsedDefenses = new EnumMap<>(DamageType.class);
        for (Map.Entry<String, Integer> entry : defenseTypes.entrySet()) {
            if (entry.getKey().equals("health")) { // parse hp separately from defenses
                parsedHealth = entry.getValue();
                continue;
            }
            parsedDefenses.put(DamageType.valueOf(entry.getKey().toUpperCase(Locale.ROOT)), entry.getValue());
        }

        if (parsedHealth != Integer.MIN_VALUE) return;
        parsedHealth = 0; // no hp entry => item provides zero hp
    }

    public int getHealth() {
        parseDefenses();
        return parsedHealth;
    }

    public Map<DamageType, Integer> getElementalDefenses() {
        parseDefenses();
        return parsedDefenses;
    }

    public Map<String, IdentificationProfile> getStatuses() {
        return statuses;
    }

    public List<MajorIdentification> getMajorIds() {
        return majorIdentifications;
    }

    public String getRestriction() {
        return restriction;
    }

    public boolean hasRequirements() {
        parseRequirements();
        return (!parsedRequirements.isEmpty());
    }

    private ClassType getClassNeeded() {
        return itemInfo.getType().getClassReq();
    }

    public int getLevelRequirement() {
        if (!requirements.containsKey("level")) return 0;
        return Integer.parseInt(requirements.get("level"));
    }

    public String getLore() {
        if (lore != null && !replacedLore) {
            lore = lore.replace("\\[", "[")
                    .replace("\\]", "]")
                    .replace("[Community Event Winner] ", "[Community Event Winner]\n");
            replacedLore = true;
        }
        return lore;
    }

    public void updateMajorIdsFromStrings(Map<String, MajorIdentification> idMap) {
        if (majorIds == null) return;
        majorIdentifications = new ArrayList<>();
        for (String id : majorIds) {
            if (idMap.containsKey(id)) {
                majorIdentifications.add(idMap.get(id));
            }
        }
    }

    @Override
    public String toString() {
        return "GearProfile{" + "displayName='"
                + displayName + '\'' + ", tier="
                + tier + ", powderAmount="
                + powderAmount + ", attackSpeed="
                + attackSpeed + ", gearInfo="
                + itemInfo + ", requirements="
                + requirements + ", damageTypes="
                + damageTypes + ", defenseTypes="
                + defenseTypes + ", statuses="
                + statuses + ", restriction='"
                + restriction + '\'' + ", lore='"
                + lore + '\'' + ", majorIdentifications="
                + majorIdentifications + '}';
    }
}
