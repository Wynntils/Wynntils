/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear;

import com.wynntils.wynn.objects.Element;

public enum GearDamageType {
    ANY(null, ""),
    NEUTRAL(null),
    RAINBOW(null, "Elemental"),
    AIR(Element.AIR),
    EARTH(Element.EARTH),
    FIRE(Element.FIRE),
    THUNDER(Element.THUNDER),
    WATER(Element.WATER);

    GearDamageType(Element element) {}

    GearDamageType(Element element, String name) {
        this(element);
    }
}
