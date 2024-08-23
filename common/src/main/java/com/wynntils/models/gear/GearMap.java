/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.wynntils.models.stats.builders.DamageStatBuilder;
import com.wynntils.models.stats.builders.DefenceStatBuilder;
import com.wynntils.models.stats.builders.MiscStatBuilder;
import com.wynntils.models.stats.builders.SkillStatBuilder;
import com.wynntils.models.stats.builders.SpellStatBuilder;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.RangedValue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class GearMap {
    public static final String DIR = System.getenv("APPDATA") + File.separator + "Wynntils";
    public static final String GEAR_INFO_FILE = DIR + File.separator + "gear_info.json";
    public static final String GEAR_STATS = DIR + File.separator + "gear_stats.json";

    public static final Map<String, StatType> STAT_TYPE_MAP = loadStatTypeMap();
    public static final Map<String, Map<StatType, StatPossibleValues>> GEAR_INFO_MAP = loadGearInfoMap();

    public static HashMap<String, StatType> loadStatTypeMap() {
        HashMap<String, StatType> statTypeMap = new HashMap<>();

        SkillStatBuilder skillStatBuilder = new SkillStatBuilder();
        skillStatBuilder.buildStats(statType -> statTypeMap.put(statType.getApiName(), statType));

        DamageStatBuilder damageStatBuilder = new DamageStatBuilder();
        damageStatBuilder.buildStats(statType -> statTypeMap.put(statType.getApiName(), statType));

        DefenceStatBuilder defenceStatBuilder = new DefenceStatBuilder();
        defenceStatBuilder.buildStats(statType -> statTypeMap.put(statType.getApiName(), statType));

        MiscStatBuilder miscStatBuilder = new MiscStatBuilder();
        miscStatBuilder.buildStats(statType -> statTypeMap.put(statType.getApiName(), statType));

        SpellStatBuilder spellStatBuilder = new SpellStatBuilder();
        spellStatBuilder.buildStats(statType -> statTypeMap.put(statType.getApiName(), statType));

        // create directory Wynntils if it doesn't exist
        File directory = new File(DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }

        File file = new File(GEAR_STATS);
        if (!file.exists()) {
            try {
                FileWriter writer = new FileWriter(GEAR_STATS);
                writer.write("{\n");
                for (Map.Entry<String, StatType> entry : statTypeMap.entrySet()) {
                    writer.write("  \"" + entry.getKey() + "\": \""
                            + entry.getValue().getDisplayName()
                            + entry.getValue().getUnit().getDisplayName() + "\",\n");
                }
                writer.write("}");
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return statTypeMap;
    }

    public static Map<String, Map<StatType, StatPossibleValues>> loadGearInfoMap() {
        HashMap<String, Map<StatType, StatPossibleValues>> gearInfoMap = new HashMap<>();

        try {
            JsonReader reader = new JsonReader(new FileReader(GEAR_INFO_FILE));
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            for (String key : obj.keySet()) { // for item
                Map<StatType, StatPossibleValues> statMap = new HashMap<>();
                JsonObject o = obj.get(key).getAsJsonObject();
                for (String statKey : o.keySet()) { // for stat
                    StatType statType = STAT_TYPE_MAP.get(statKey);
                    JsonArray range = o.get(statKey).getAsJsonArray();
                    int min = range.get(0).getAsInt();
                    int max = range.get(1).getAsInt();
                    int baseValue;
                    if (min < 0) {
                        baseValue = (min + max) / 2;
                    } else {
                        baseValue = max - min;
                    }
                    StatPossibleValues statPossibleValues =
                            new StatPossibleValues(statType, new RangedValue(min, max), baseValue, false);
                    statMap.put(statType, statPossibleValues);
                }
                gearInfoMap.put(key, statMap);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            try {
                FileWriter writer = new FileWriter(GEAR_INFO_FILE);
                writer.write("{}");
                writer.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return gearInfoMap;
    }
}
