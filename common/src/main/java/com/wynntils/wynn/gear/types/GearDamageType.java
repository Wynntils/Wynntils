/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.types;

import com.wynntils.wynn.objects.Element;
import java.util.Optional;

public enum GearDamageType {
    ANY(""),
    NEUTRAL("Neutral"),
    RAINBOW("Elemental"),
    AIR(Element.AIR),
    EARTH(Element.EARTH),
    FIRE(Element.FIRE),
    THUNDER(Element.THUNDER),
    WATER(Element.WATER);

    private final Element element;
    private final String displayName;
    private final String apiName;

    GearDamageType(String name) {
        this.element = null;
        // displayName needs padding if non-empty
        this.displayName = name.isEmpty() ? "" : name + " ";
        this.apiName = name;
    }

    GearDamageType(Element element) {
        this.element = element;
        // displayName needs padding
        this.displayName = element.getDisplayName() + " ";
        this.apiName = element.getDisplayName();
    }

    public Optional<Element> getElement() {
        return Optional.ofNullable(element);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApiName() {
        return apiName;
    }
}
