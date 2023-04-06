/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

import com.wynntils.core.text.CodedString;

public enum SpellFailureReason {
    NOT_ENOUGH_MANA(CodedString.fromString("§4You don't have enough mana to cast that spell!")),
    NOT_UNLOCKED(CodedString.fromString("§4You have not unlocked this spell!"));

    private final CodedString message;

    SpellFailureReason(CodedString message) {
        this.message = message;
    }

    public static SpellFailureReason fromMsg(CodedString msg) {
        for (SpellFailureReason failureReason : values()) {
            if (failureReason.message.equals(msg)) return failureReason;
        }
        return null;
    }

    public CodedString getMessage() {
        return message;
    }
}
