/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear;

public enum GearAttackType {
    ANY("", ""),
    MAIN_ATTACK("Main Attack ", "MainAttack"),
    SPELL("Spell ", "Spell");

    private final String displayName;
    private final String apiName;

    GearAttackType(String displayName, String apiName) {
        this.displayName = displayName;
        this.apiName = apiName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApiName() {
        return apiName;
    }
}
