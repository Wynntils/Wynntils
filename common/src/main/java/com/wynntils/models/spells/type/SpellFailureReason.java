/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

import com.wynntils.core.text.StyledText;

public enum SpellFailureReason {
    NOT_ENOUGH_MANA(StyledText.fromString("§4You don't have enough mana to cast that spell!")),
    NOT_ENOUGH_HEALTH(StyledText.fromString("§4You don't have enough health to cast that spell!")),
    NOT_UNLOCKED(StyledText.fromString("§4You have not unlocked this spell!"));

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
