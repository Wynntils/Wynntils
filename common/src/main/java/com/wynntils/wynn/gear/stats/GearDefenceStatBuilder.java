/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.stats;

import com.wynntils.wynn.gear.GearStatUnit;
import com.wynntils.wynn.objects.Element;
import java.util.List;

public class GearDefenceStatBuilder {
    public static void addStats(List<GearStat> registry) {
        for (Element element : Element.values()) {
            // The difference in spelling (defence/defense) is due to Wynncraft. Do not change.
            String displayName = element.getDisplayName() + " Defence";
            String apiName = "bonus" + element.getDisplayName() + "Defense";
            String loreName = element.name() + "DEFENSE";
            String key = "DEFENCE_" + element.name();
            GearStat rawType = new GearStat(key, displayName, apiName, loreName, GearStatUnit.PERCENT);
            registry.add(rawType);
        }
    }
}
