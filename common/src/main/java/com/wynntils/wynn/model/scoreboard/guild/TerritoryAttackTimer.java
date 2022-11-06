/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.scoreboard.guild;

import net.minecraft.ChatFormatting;

public record TerritoryAttackTimer(String territory, int minutes, int seconds) {
    public String asString() {
        return ChatFormatting.GRAY + territory + ChatFormatting.AQUA + " " + timerString();
    }

    public int asSeconds() {
        return minutes * 60 + seconds;
    }

    public String timerString() {
        return "%02d:%02d".formatted(minutes, seconds);
    }
}
