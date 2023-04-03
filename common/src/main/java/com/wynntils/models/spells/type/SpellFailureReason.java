/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

import com.wynntils.utils.mc.type.StyledText;

public enum SpellFailureReason {
    NOT_ENOUGH_MANA(StyledText.of("§4You don't have enough mana to cast that spell!")),
    NOT_UNLOCKED(StyledText.of("§4You have not unlocked this spell!"));

    private final StyledText message;

    SpellFailureReason(StyledText message) {
        this.message = message;
    }

    public static SpellFailureReason fromMsg(StyledText msg) {
        for (SpellFailureReason failureReason : values()) {
            if (failureReason.message.equals(msg)) return failureReason;
        }
        return null;
    }

    public StyledText getMessage() {
        return message;
    }
}
