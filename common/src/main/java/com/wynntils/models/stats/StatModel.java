/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.models.stats.builders.DamageStatBuilder;
import com.wynntils.models.stats.builders.DefenceStatBuilder;
import com.wynntils.models.stats.builders.MiscStatBuilder;
import com.wynntils.models.stats.builders.SkillStatBuilder;
import com.wynntils.models.stats.builders.SpellStatBuilder;
import com.wynntils.models.stats.builders.StatBuilder;
import com.wynntils.models.stats.type.DamageStatType;
import com.wynntils.models.stats.type.DefenceStatType;
import com.wynntils.models.stats.type.MiscStatType;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.models.stats.type.SpellStatType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.stats.type.StatUnit;
import com.wynntils.utils.type.RangedValue;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class StatModel extends Model {
    private final List<StatType> statTypeRegistry = new ArrayList<>();
    private final StatLookupTable statTypeLookup = new StatLookupTable();
    private final Map<StatListOrdering, List<StatType>> orderingLists;

    // An id map for stat type -> unique id keys, used for gear chat encoding
    private Map<StatType, Integer> statTypeIdMap = new HashMap<>();

    public StatModel() {
        super(List.of());

        // First build stats of all kinds
        List<SkillStatType> skillStats = buildStats(new SkillStatBuilder());
        List<MiscStatType> miscStats = buildStats(new MiscStatBuilder());
        List<DefenceStatType> defenceStats = buildStats(new DefenceStatBuilder());
        List<DamageStatType> damageStats = buildStats(new DamageStatBuilder());
        List<SpellStatType> spellStats = buildStats(new SpellStatBuilder());

        // Then put them all in the registry
        initRegistry(skillStats, miscStats, defenceStats, damageStats, spellStats);

        // Finally create ordered lists for sorting
        orderingLists = StatListOrderer.createOrderingMap(skillStats, miscStats, defenceStats, damageStats, spellStats);
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_IDENTIFICATION_KEYS).handleReader(this::handleIdentificationKeys);
    }

    public StatActualValue buildActualValue(
            StatType statType, int value, int stars, StatPossibleValues possibleValues) {
        RangedValue internalRoll = possibleValues != null
                ? StatCalculator.calculateInternalRollRange(possibleValues, value, stars)
                : RangedValue.NONE;
        return new StatActualValue(statType, value, stars, internalRoll);
    }

    public StatType fromDisplayName(String displayName, String unit) {
        return statTypeLookup.get(displayName, unit);
    }

    public StatType fromInternalRollId(String id) {
        for (StatType statType : statTypeRegistry) {
            if (statType.getInternalRollName().equals(id)) return statType;
        }

        return null;
    }

    public StatType fromApiName(String id) {
        for (StatType statType : statTypeRegistry) {
            if (statType.getApiName().equals(id)) return statType;
        }

        return null;
    }

    public Optional<Integer> getIdForStatType(StatType statType) {
        return Optional.ofNullable(statTypeIdMap.get(statType));
    }

    public Optional<StatType> getStatTypeForId(int id) {
        return statTypeIdMap.entrySet().stream()
                .filter(entry -> entry.getValue() == id)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public String getDisplayName(
            StatType statType, ClassType classReq, ClassType currentClass, RangedValue workingLevelRange) {
        if (statType instanceof SpellStatType spellStatType) {
            // If there is no class associated with the gear (i.e. it is not
            // a weapon), chose our current class
            ClassType classToUse = classReq != null ? classReq : currentClass;

            SpellType spellType = spellStatType.getSpellType().forOtherClass(classToUse);
            return SpellStatBuilder.getStatNameForSpell(spellType.getName());
        }

        // Inject level range into charm leveled stats
        if (statType.getSpecialStatType() == StatType.SpecialStatType.CHARM_LEVELED_STAT) {
            return statType.getDisplayName().replace("${}", workingLevelRange.low() + "-" + workingLevelRange.high());
        }

        return statType.getDisplayName();
    }

    public List<StatType> getOrderingList(StatListOrdering ordering) {
        return orderingLists.get(ordering);
    }

    public List<StatType> getSortedStats(GearInfo gearInfo, StatListOrdering ordering) {
        List<StatType> orderingList = orderingLists.get(ordering);

        List<StatType> sortedStats = new ArrayList<>(gearInfo.getVariableStats());
        sortedStats.sort(Comparator.comparingInt(orderingList::indexOf));

        return sortedStats;
    }

    public List<StatType> getAllStatTypes() {
        return statTypeRegistry;
    }

    private static <T extends StatType> List<T> buildStats(StatBuilder<T> builder) {
        List<T> statList = new ArrayList<>();

        builder.buildStats(statList::add);
        return statList;
    }

    private void initRegistry(
            List<SkillStatType> skillStats,
            List<MiscStatType> miscStats,
            List<DefenceStatType> defenceStats,
            List<DamageStatType> damageStats,
            List<SpellStatType> spellStats) {
        statTypeRegistry.addAll(skillStats);
        statTypeRegistry.addAll(miscStats);
        statTypeRegistry.addAll(defenceStats);
        statTypeRegistry.addAll(damageStats);
        statTypeRegistry.addAll(spellStats);

        // Create a fast lookup map
        for (StatType statType : statTypeRegistry) {
            statTypeLookup.put(statType.getDisplayName(), statType.getUnit(), statType);
        }
        // Spell Cost stats have a lot of aliases under which they can appear
        for (SpellStatType spellStatType : spellStats) {
            for (String alias : SpellStatBuilder.getAliases(spellStatType)) {
                statTypeLookup.put(alias, spellStatType.getUnit(), spellStatType);
            }
        }
    }

    private void handleIdentificationKeys(Reader reader) {
        Type type = new TypeToken<Map<String, Integer>>() {}.getType();
        Map<String, Integer> apiNamesToIdMap = Managers.Json.GSON.fromJson(reader, type);

        Map<StatType, Integer> tempMap = new HashMap<>();

        for (Map.Entry<String, Integer> entry : apiNamesToIdMap.entrySet()) {
            StatType statType = fromApiName(entry.getKey());
            if (statType == null) {
                WynntilsMod.warn("Unknown stat type for identification key: " + entry.getKey());
                continue;
            }

            tempMap.put(statType, entry.getValue());
        }

        statTypeIdMap = Map.copyOf(tempMap);
    }

    private static class StatLookupTable {
        private final Map<String, StatType> lookupTable = new HashMap<>();
        private final Map<Pattern, StatType> regexLookupTable = new HashMap<>();

        private StatType get(String displayName, String unit) {
            String lookupName = displayName + (unit == null ? "" : unit);
            StatType statType = lookupTable.get(lookupName);

            if (statType != null) return statType;

            for (Map.Entry<Pattern, StatType> entry : regexLookupTable.entrySet()) {
                if (entry.getKey().matcher(displayName).matches()) {
                    statType = entry.getValue();
                    break;
                }
            }

            return statType;
        }

        private void put(String displayName, StatUnit unit, StatType statType) {
            // If the stat is a tome base stat,
            // we don't want to add it to the lookup table,
            // as we won't look it up in a regular way
            if (statType.getSpecialStatType() == StatType.SpecialStatType.TOME_BASE_STAT) return;

            // If the stat is a charm leveled stat,
            // we want to add it to the regex lookup table
            // because the stat name will have a level range in it
            if (statType.getSpecialStatType() == StatType.SpecialStatType.CHARM_LEVELED_STAT) {
                regexLookupTable.put(
                        Pattern.compile(statType.getDisplayName().replace("${}", "(\\d+)-(\\d+)")), statType);
                return;
            }

            String lookupName = displayName + unit.getDisplayName();
            lookupTable.put(lookupName, statType);
        }
    }
}
