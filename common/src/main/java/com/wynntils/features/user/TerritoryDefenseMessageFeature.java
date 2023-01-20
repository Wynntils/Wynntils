/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.InventoryMouseClickedEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.LoreUtils;
import com.wynntils.mc.utils.McUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TerritoryDefenseMessageFeature extends UserFeature {
    private static final Pattern ATTACK_SCREEN_TITLE = Pattern.compile("Attacking: (.+)");
    private static final Pattern TERRITORY_DEFENSE_PATTERN = Pattern.compile("Territory Defences: (.+)");

    @SubscribeEvent
    public void onInventoryClick(InventoryMouseClickedEvent event) {
        if (event.getHoveredSlot() == null || McUtils.mc().screen == null) return;
        Matcher titleMatcher =
                ATTACK_SCREEN_TITLE.matcher(McUtils.mc().screen.getTitle().getString());
        if (!titleMatcher.matches()) return;

        ItemStack item = event.getHoveredSlot().getItem();

        for (Component tooltipLine : LoreUtils.getTooltipLines(item)) {
            String unformatted = ComponentUtils.getUnformatted(tooltipLine);
            Matcher matcher = TERRITORY_DEFENSE_PATTERN.matcher(unformatted);
            if (matcher.matches()) {
                McUtils.sendCommand("g %s defense is %s".formatted(titleMatcher.group(1), matcher.group(1)));
                return;
            }
        }
    }
}
