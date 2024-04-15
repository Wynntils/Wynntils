/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories;

import com.wynntils.core.components.Models;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.territories.type.GuildResourceValues;
import java.util.Optional;
import net.minecraft.ChatFormatting;

public final class TerritoryAttackTimer {
    private final TerritoryProfile territoryProfile;

    private final String territory;
    private final int minutes;
    private final int seconds;

    private GuildResourceValues defense;

    public TerritoryAttackTimer(String territory, int minutes, int seconds) {
        this.territoryProfile = Models.Territory.getTerritoryProfileFromShortName(territory);
        this.territory = territory;
        this.minutes = minutes;
        this.seconds = seconds;
        this.defense = null;
    }

    public String asString() {
        return ChatFormatting.GRAY + territory + ChatFormatting.YELLOW + " ("
                + (defense == null ? "Unknown" : defense.getAsString()) + ")" + ChatFormatting.AQUA + " "
                + timerString();
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

    public GuildResourceValues defense() {
        return defense;
    }

    public boolean isDefenseKnown() {
        return defense != null;
    }

    public Optional<TerritoryProfile> territoryProfile() {
        return Optional.ofNullable(territoryProfile);
    }

    public void setDefense(GuildResourceValues defense) {
        this.defense = defense;
    }
}
