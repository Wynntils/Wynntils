/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

public enum SpellFailureReason {
    NOT_ENOUGH_MANA("§4You don't have enough mana to cast that spell!"),
    NOT_UNLOCKED("§4You have not unlocked this spell!");

    private final String msg;

    SpellFailureReason(String msg) {
        this.msg = msg;
    }

    public static SpellFailureReason fromMsg(String msg) {
        for (SpellFailureReason failureReason : values()) {
            if (failureReason.msg.equals(msg)) return failureReason;
        }
        return null;
    }
}
