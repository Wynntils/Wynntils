/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.scoreboard.guild;

import net.minecraft.ChatFormatting;

public record TerritoryAttackTimer(String territory, String timeUntil) {
    public String asString() {
        return ChatFormatting.GRAY + territory + ChatFormatting.AQUA + " " + timeUntil;
    }
}
