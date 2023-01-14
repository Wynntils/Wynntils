/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear;

import com.wynntils.wynn.objects.Element;

public enum GearDamageType {
    ANY(null, "", ""),
    NEUTRAL(null, "Neutral ", "Neutral"),
    RAINBOW(null, "Elemental ", "Elemental"),
    AIR(Element.AIR, "Air ", "Air"),
    EARTH(Element.EARTH, "Earth ", "Earth"),
    FIRE(Element.FIRE, "Fire ", "Fire"),
    THUNDER(Element.THUNDER, "Thunder ", "Thunder"),
    WATER(Element.WATER, "Water ", "Water");

    private final Element element;
    private final String displayName;
    private final String apiName;

    GearDamageType(Element element, String displayName, String apiName) {
        this.element = element;
        this.displayName = displayName;
        this.apiName = apiName;
    }

    public Element getElement() {
        return element;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApiName() {
        return apiName;
    }
}
