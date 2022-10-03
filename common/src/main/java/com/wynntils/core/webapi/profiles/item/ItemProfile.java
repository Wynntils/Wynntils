/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.profiles.item;

import com.wynntils.wynn.objects.ClassType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItemProfile {
    String displayName;
    ItemTier tier;
    boolean identified;
    int powderAmount;

    final ItemAttackSpeed attackSpeed = null;

    ItemInfoContainer itemInfo;
    Map<String, String> requirements;

    final Map<String, String> damageTypes = new HashMap<>();
    final Map<String, Integer> defenseTypes = new HashMap<>();
    final Map<String, IdentificationProfile> statuses = new HashMap<>();

    final List<String> majorIds = new ArrayList<>();

    String restriction;
    String lore;

    transient List<MajorIdentification> majorIdentifications = new ArrayList<>();

    transient Map<RequirementType, String> parsedRequirements = null;

    transient Map<DamageType, String> parsedDamages = null;
    transient Map<DamageType, Integer> parsedAvgDamages = null;
    transient int parsedHealth = Integer.MIN_VALUE;
    transient Map<DamageType, Integer> parsedDefenses = null;

    transient boolean replacedLore = false;

    public ItemProfile(
            String displayName,
            ItemTier tier,
            boolean identified,
            ItemAttackSpeed attackSpeed,
            ItemInfoContainer itemInfo,
            Map<String, String> requirements,
            Map<String, String> damageTypes,
            Map<String, Integer> defenseTypes,
            Map<String, IdentificationProfile> statuses,
            ArrayList<String> majorIds,
            String restriction,
            String lore) {}

    public void registerIdTypes() {
        statuses.forEach((key, value) -> value.registerIdType(key));
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemTier getTier() {
        return tier;
    }

    public boolean isIdentified() {
        return identified;
    }

    public int getPowderAmount() {
        return powderAmount;
    }

    public ItemAttackSpeed getAttackSpeed() {
        return attackSpeed;
    }

    public ItemInfoContainer getItemInfo() {
        return itemInfo;
    }

    public void parseRequirements() {
        if (parsedRequirements != null) return;

        parsedRequirements = new EnumMap<>(RequirementType.class);
        for (Map.Entry<String, String> entry : requirements.entrySet()) {
            RequirementType type = RequirementType.valueOf(entry.getKey().toUpperCase(Locale.ROOT));
            String reqStr = entry.getValue();
            if (reqStr.equals("0")) continue; // no req, ignore
            parsedRequirements.put(type, reqStr);
        }
        if (getClassNeeded() != null) {
            parsedRequirements.put(RequirementType.CLASS, getClassNeeded().toString());
        }
    }

    public Map<RequirementType, String> getRequirements() {
        parseRequirements();
        return parsedRequirements;
    }

    public Map<String, String> getDamageTypes() {
        return damageTypes;
    }

    public void parseDamages() {
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

    public ClassType getClassNeeded() {
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

    public void addMajorIds(Map<String, MajorIdentification> idMap) {
        if (majorIds == null) return;
        majorIdentifications = new ArrayList<>();
        for (String id : majorIds) {
            if (idMap.containsKey(id)) majorIdentifications.add(idMap.get(id));
        }
    }
}
