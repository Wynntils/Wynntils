/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.highlights;

import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.type.TerritoryUpgrade;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TerritoryBonusEffectHighlighter implements TerritoryHighlighter {
    public static final CustomColor MULTI_ATTACKS_COLOR = CommonColors.AQUA;
    public static final CustomColor EMERALD_SEEKING_COLOR = CommonColors.LIGHT_GREEN;
    public static final CustomColor TOME_SEEKING_COLOR = CommonColors.BLUE;
    public static final CustomColor MOB_EXPERIENCE = CommonColors.YELLOW;
    public static final CustomColor MOB_DAMAGE = CommonColors.RED;
    public static final CustomColor GATHERING_EXPERIENCE = CommonColors.MAGENTA;

    private static final Map<TerritoryUpgrade, CustomColor> UPGRADE_COLORS = Map.of(
            TerritoryUpgrade.TOWER_MULTI_ATTACKS, MULTI_ATTACKS_COLOR,
            TerritoryUpgrade.EMERALD_SEEKING, EMERALD_SEEKING_COLOR,
            TerritoryUpgrade.TOME_SEEKING, TOME_SEEKING_COLOR,
            TerritoryUpgrade.MOB_EXPERIENCE, MOB_EXPERIENCE,
            TerritoryUpgrade.MOB_DAMAGE, MOB_DAMAGE,
            TerritoryUpgrade.GATHERING_EXPERIENCE, GATHERING_EXPERIENCE);

    @Override
    public List<CustomColor> getBackgroundColors(TerritoryItem territoryItem) {
        return territoryItem.getUpgrades().keySet().stream()
                .map(UPGRADE_COLORS::get)
                .filter(Objects::nonNull)
                .map(color -> color.withAlpha(0.5f))
                .toList();
    }
}
