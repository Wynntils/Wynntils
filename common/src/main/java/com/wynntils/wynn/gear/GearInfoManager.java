/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlId;
import com.wynntils.wynn.gear.stats.DamageStatBuilder;
import com.wynntils.wynn.gear.stats.DefenceStatBuilder;
import com.wynntils.wynn.gear.stats.MiscStatBuilder;
import com.wynntils.wynn.gear.stats.SpellStatBuilder;
import com.wynntils.wynn.gear.stats.StatBuilder;
import com.wynntils.wynn.gear.types.GearStat;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GearInfoManager extends Manager {
    private static final Gson GEAR_INFO_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(GearInfo.class, new GearInfoDeserializer())
            .create();

    private static final List<StatBuilder> STAT_BUILDERS =
            List.of(new MiscStatBuilder(), new DefenceStatBuilder(), new SpellStatBuilder(), new DamageStatBuilder());

    public final List<GearStat> registry = new ArrayList<>();
    public final Map<String, GearStat> lookup = new HashMap<>();
    private List<GearInfo> allGearInfos = List.of();

    public GearInfoManager(NetManager netManager) {
        super(List.of(netManager));

        for (StatBuilder builder : STAT_BUILDERS) {
            builder.buildStats(registry::add);
        }

        // Create a fast lookup map
        for (GearStat stat : registry) {
            String lookupName = stat.displayName() + stat.unit().getDisplayName();
            lookup.put(lookupName, stat);
        }

        loadInfoProfiles();
    }

    public GearStat getGearStat(String displayName, String unit) {
        String lookupName = displayName + unit;
        return lookup.get(lookupName);
    }

    private void loadInfoProfiles() {
        Download dl = Managers.Net.download(UrlId.DATA_WYNNCRAFT_GEARS);
        dl.handleReader(reader -> {
            Type type = new TypeToken<WynncraftGearInfoResponse>() {}.getType();
            WynncraftGearInfoResponse newGearInfoList = GEAR_INFO_GSON.fromJson(reader, type);
            allGearInfos = newGearInfoList.items;
        });
    }

    private static class WynncraftGearInfoResponse {
        List<GearInfo> items;
    }
}
