/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public enum TerritoryDefenseLevel {
    OFF(ChatFormatting.GRAY + "Off", 0),
    VERY_LOW(ChatFormatting.DARK_GREEN + "Very Low", 1),
    LOW(ChatFormatting.GREEN + "Low", 2),
    MEDIUM(ChatFormatting.YELLOW + "Medium", 3),
    HIGH(ChatFormatting.RED + "High", 4),
    VERY_HIGH(ChatFormatting.DARK_RED + "Very High", 5);

    private final String asColoredString;
    private final int level;

    TerritoryDefenseLevel(String asColoredString, int level) {
        this.asColoredString = asColoredString;
        this.level = level;
    }

    public String asColoredString() {
        return asColoredString;
    }

    public TerritoryDefenseLevel next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public int getLevel() {
        return level;
    }

    public List<Component> getTerritoryDefenseLevelFilterTooltip() {
        return List.of(
                new TextComponent("[>] ")
                        .withStyle(ChatFormatting.BLUE)
                        .append(new TranslatableComponent("screens.wynntils.guildMap.cycleDefenseFilter.name")),
                new TranslatableComponent("screens.wynntils.guildMap.cycleDefenseFilter.description1")
                        .withStyle(ChatFormatting.GRAY),
                new TranslatableComponent("screens.wynntils.guildMap.cycleDefenseFilter.description2")
                        .withStyle(ChatFormatting.GRAY),
                new TranslatableComponent("screens.wynntils.guildMap.cycleDefenseFilter.description3")
                        .withStyle(ChatFormatting.GRAY),
                new TranslatableComponent("screens.wynntils.guildMap.cycleDefenseFilter.description4")
                        .withStyle(ChatFormatting.GRAY)
                        .append(asColoredString));
    }
}
