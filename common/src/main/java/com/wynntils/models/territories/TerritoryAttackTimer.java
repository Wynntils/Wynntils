/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories;

import net.minecraft.ChatFormatting;

public final class TerritoryAttackTimer {
    private final String territory;
    private final int minutes;
    private final int seconds;

    private String defense;

    public TerritoryAttackTimer(String territory, int minutes, int seconds) {
        this.territory = territory;
        this.minutes = minutes;
        this.seconds = seconds;
        this.defense = "Unknown";
    }

    public String asString() {
        return ChatFormatting.GRAY + territory + ChatFormatting.YELLOW + " (" + defense + ")" + ChatFormatting.AQUA
                + " " + timerString();
    }

    public int asSeconds() {
        return minutes * 60 + seconds;
    }

    public String timerString() {
        return "%02d:%02d".formatted(minutes, seconds);
    }

    public String territory() {
        return territory;
    }

    public String defense() {
        return defense;
    }

    public boolean isDefenseKnown() {
        return !defense.equals("Unknown");
    }

    public void setDefense(String defense) {
        this.defense = defense;
    }
}
