/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories;

import com.wynntils.core.components.Models;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.territories.type.GuildResourceValues;
import com.wynntils.utils.mc.McUtils;
import java.util.Optional;
import net.minecraft.ChatFormatting;

public record TerritoryAttackTimer(String territoryName, long timerEnd) {
    public String asString() {
        Optional<GuildResourceValues> defense = defense();
        ChatFormatting defenseColor =
                defense.isEmpty() ? ChatFormatting.GRAY : defense.get().getDefenceColor();
        String defenseString = defense.isEmpty() ? "Unknown" : defense.get().getAsString();

        TerritoryProfile currentTerritory =
                Models.Territory.getTerritoryProfileForPosition(McUtils.player().position());
        boolean isCurrentTerritory =
                currentTerritory != null && currentTerritory.getName().equals(territoryName);

        return ChatFormatting.GRAY
                + (isCurrentTerritory ? ChatFormatting.DARK_PURPLE.toString() + ChatFormatting.BOLD : "")
                + territoryName + defenseColor + " (" + defenseString + ")" + ChatFormatting.AQUA + " " + timerString();
    }

    public int getMinutesRemaining() {
        return (int) ((timerEnd - System.currentTimeMillis()) / 60000);
    }

    public int getSecondsRemaining() {
        return (int) ((timerEnd - System.currentTimeMillis()) / 1000) % 60;
    }

    public int asSeconds() {
        return getMinutesRemaining() * 60 + getSecondsRemaining();
    }

    public String timerString() {
        return "%02d:%02d".formatted(getMinutesRemaining(), getSecondsRemaining());
    }

    public Optional<GuildResourceValues> defense() {
        return Models.GuildAttackTimer.getDefenseForTerritory(territoryName);
    }
}
