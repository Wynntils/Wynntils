/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.horse.type;

import java.util.Locale;

public enum HorseTier {
    BROWN(1),
    BLACK(2),
    CHESTNUT(3),
    WHITE(4);

    private final int numeral;

    HorseTier(int numeral) {
        this.numeral = numeral;
    }

    public static HorseTier fromName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }

    public static HorseTier fromNumeral(int numeral) {
        return values()[numeral - 1];
    }

    public int getNumeral() {
        return numeral;
    }
}
