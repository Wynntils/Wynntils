/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.models.abilitytree.AbilityTreeContainerQueries;
import com.wynntils.models.items.items.gui.AbilityTreeItem;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.Optional;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// NOTE: This feature was intented to be used on fully reset ability trees.
//       Although support for parsing any tree is present, I would still recommend using a fresh tree to avoid any
// issues.
@StartDisabled
@ConfigCategory(Category.DEBUG)
public class AbilityTreeDataDumpFeature extends Feature {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onInventoryClick(ContainerClickEvent event) {
        if (!(McUtils.mc().screen instanceof AbstractContainerScreen<?> abstractContainerScreen)) return;
        if (!KeyboardUtils.isShiftDown()) return;

        Optional<AbilityTreeItem> abilityTreeItem = Models.Item.asWynnItem(event.getItemStack(), AbilityTreeItem.class);

        if (abilityTreeItem.isEmpty()) return;

        event.setCanceled(true);
        McUtils.player().closeContainer();

        // Wait for the container to close
        Managers.TickScheduler.scheduleNextTick(
                () -> Models.AbilityTree.ABILITY_TREE_CONTAINER_QUERIES.queryAbilityTree(
                        new AbilityTreeContainerQueries.AbilityPageDumper()));
    }
}
