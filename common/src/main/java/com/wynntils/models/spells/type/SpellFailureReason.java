/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

import com.wynntils.core.text.StyledText;
import java.util.regex.Pattern;

public enum SpellFailureReason {
    NOT_ENOUGH_MANA(
            Pattern.compile("§4(?:\uE008\uE002|\uE001) You don't have enough mana to cast that spell!"),
            "You don't have enough mana to cast that spell!"),
    NOT_ENOUGH_HEALTH(
            Pattern.compile("§4(?:\uE008\uE002|\uE001) You don't have enough health to cast that spell!"),
            "You don't have enough health to cast that spell!"),
    NOT_UNLOCKED(
            Pattern.compile(
                    "§4(?:\uE008\uE002|\uE001) You have not unlocked this spell! Unlock it using your compass."),
            "You have not unlocked this spell!");

    private final Pattern pattern;
    private final String displayMessage;

    SpellFailureReason(Pattern pattern, String displayMessage) {
        this.pattern = pattern;
        this.displayMessage = displayMessage;
    }

    public static SpellFailureReason fromMsg(StyledText msg) {
        for (SpellFailureReason failureReason : values()) {
            if (msg.matches(failureReason.pattern)) return failureReason;
        }
        return null;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }
}
