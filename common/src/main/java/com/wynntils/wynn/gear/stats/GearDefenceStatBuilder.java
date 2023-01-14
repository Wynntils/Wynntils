/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.wynntils.wynn.gear.GearStatUnit;
import com.wynntils.wynn.objects.Element;
import java.util.List;

public class GearDefenceStatBuilder implements GearStat {
    public static void addStats(List<GearStat> registry) {
        for (Element element : Element.values()) {
            // The difference in spelling (defence/defense) is due to Wynncraft. Do not change.
            String displayName = element.getDisplayName() + " Defence";
            String apiName = "bonus" + element.getDisplayName() + "Defense";
            String loreName = element.name() + "DEFENSE";
            String key = "DEFENCE_" + element.name();
            GearDefenceStatBuilder rawType = new GearDefenceStatBuilder(displayName, loreName, apiName, key);
            registry.add(rawType);
        }
    }


    private final String displayName;
    private final GearStatUnit unit;
    private final String loreName;
    private final String apiName;
    private final String key;

    GearDefenceStatBuilder(String displayName, String loreName, String apiName, String key) {
        this.displayName = displayName;
        this.unit = GearStatUnit.PERCENT;
        this.loreName = loreName;
        this.apiName = apiName;
        this.key = key;
    }


    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public GearStatUnit getUnit() {
        return unit;
    }

    @Override
    public String getLoreName() {
        return loreName;
    }

    @Override
    public String getApiName() {
        return apiName;
    }
}
