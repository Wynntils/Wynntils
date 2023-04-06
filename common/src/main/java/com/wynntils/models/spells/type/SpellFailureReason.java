/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

import com.wynntils.core.text.StyledText2;

public enum SpellFailureReason {
    NOT_ENOUGH_MANA(StyledText2.of("§4You don't have enough mana to cast that spell!")),
    NOT_UNLOCKED(StyledText2.of("§4You have not unlocked this spell!"));

    private final StyledText2 message;

    SpellFailureReason(StyledText2 message) {
        this.message = message;
    }

    public static SpellFailureReason fromMsg(StyledText2 msg) {
        for (SpellFailureReason failureReason : values()) {
            if (failureReason.message.equals(msg)) return failureReason;
        }
        return null;
    }

    public StyledText2 getMessage() {
        return message;
    }
}
