/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.TerritoryUpgradeItem;
import com.wynntils.models.territories.type.TerritoryUpgrade;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class TerritoryUpgradeAnnotator implements GuiItemAnnotator {
    // Test in TerritoryUpgradeAnnotator_TERRITORY_UPGRADE_PATTERN
    private static final Pattern TERRITORY_UPGRADE_PATTERN =
            Pattern.compile("^§[6abcd]§l(?<upgrade>.+) §7\\[Lv\\. (?<level>[0-9]+)\\](§8 \\(Max\\))?$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(TERRITORY_UPGRADE_PATTERN);
        if (!matcher.matches()) return null;

        TerritoryUpgrade territoryUpgrade = TerritoryUpgrade.fromName(matcher.group("upgrade"));
        int level = Integer.parseInt(matcher.group("level"));

        return new TerritoryUpgradeItem(territoryUpgrade, level);
    }
}
