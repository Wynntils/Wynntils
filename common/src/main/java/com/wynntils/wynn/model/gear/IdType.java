/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.gear;

public interface IdType {
    String getKey();

    IsVariable getIsVariable();

    String getDisplayName();

    String getUnit();

    String getAthenaName();

    String getLoreName();

    String getApiName();

    enum IsVariable {
        YES,
        NO,
        UNKNOWN;
    }
}
