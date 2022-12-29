/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

public class GearIdentification {
    private final String idName;
    private final int value;
    private final int stars;

    public GearIdentification(String idName, int value, int stars) {
        this.idName = idName;
        this.value = value;
        this.stars = stars;
    }

    public String getIdName() {
        return idName;
    }

    public int getValue() {
        return value;
    }

    public int getStars() {
        return stars;
    }
}
