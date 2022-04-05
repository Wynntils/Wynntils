/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.model;

import com.wynntils.wc.Model;

import java.util.UUID;

public interface Character extends Model {
    boolean hasCharacter();

    CharacterInfo getCharacterInfo();

    enum ClassType {
        ARCHER,
        WARRIOR,
        MAGE,
        ASSASSIN,
        SHAMAN
    }

    interface CharacterInfo {
        ClassType getClassType();

        boolean isReskinned();

        int getLevel();

        UUID getId();
    }
}
