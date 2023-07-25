/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.InventoryMouseClickedEvent;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UNCATEGORIZED)
public class TerritoryDefenseMessageFeature extends Feature {
    private static final Pattern ATTACK_SCREEN_TITLE = Pattern.compile("Attacking: (.+)");
    private static final Pattern TERRITORY_DEFENSE_PATTERN = Pattern.compile("Territory Defences: (.+)");

    @SubscribeEvent
    public void onInventoryClick(InventoryMouseClickedEvent event) {
        if (event.getHoveredSlot() == null || McUtils.mc().screen == null) return;
        Matcher titleMatcher =
                ATTACK_SCREEN_TITLE.matcher(McUtils.mc().screen.getTitle().getString());
        if (!titleMatcher.matches()) return;

        ItemStack itemStack = event.getHoveredSlot().getItem();

        for (Component tooltipLine : LoreUtils.getTooltipLines(itemStack)) {
            Matcher matcher = StyledText.fromComponent(tooltipLine)
                    .getMatcher(TERRITORY_DEFENSE_PATTERN, PartStyle.StyleType.NONE);
            if (matcher.matches()) {
                McUtils.sendCommand("g %s defense is %s".formatted(titleMatcher.group(1), matcher.group(1)));
                return;
            }
        }
    }
}
