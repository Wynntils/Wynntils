/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.statemanaged;

import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.StateManagedFeature;
import com.wynntils.core.webapi.profiles.item.ItemTier;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.utils.ContainerUtils;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// FIXME: This feature is only needed because we do not have a way to save function data persistently. Remove this when
// we add persistent data storage other than configs.
public class LootrunFunctionsFeature extends StateManagedFeature {
    private final int LOOT_CHEST_ITEM_COUNT = 27;

    public static LootrunFunctionsFeature INSTANCE;

    @Config(visible = false)
    public int dryCount = 0;

    @Config(visible = false)
    public int dryBoxes = 0;

    private int nextExpectedLootContainerId = -1;

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent event) {
        if (ContainerUtils.isLootOrRewardChest(ComponentUtils.getUnformatted(event.getTitle()))) {
            nextExpectedLootContainerId = event.getContainerId();
            System.out.println("nextExpectedLootContainerId = " + nextExpectedLootContainerId);
        }
    }

    @SubscribeEvent
    public void onSetContent(ContainerSetContentEvent event) {
        if (nextExpectedLootContainerId == event.getContainerId()) {
            int dryBoxesIfNoMythic = 0;
            boolean empty = true; // if container has only air, it is not loaded yet

            List<ItemStack> items = event.getItems();
            for (int i = 0; i < Math.min(items.size(), LOOT_CHEST_ITEM_COUNT); i++) {
                ItemStack itemStack = items.get(i);

                if (itemStack.getItem() != Items.AIR) {
                    empty = false;
                }

                System.out.println("itemStack.getHoverName() = " + itemStack.getHoverName());

                if (!WynnItemMatchers.isUnidentified(itemStack)) continue;

                ItemTier itemTier = ItemTier.fromComponent(itemStack.getHoverName());

                if (itemTier == ItemTier.MYTHIC) {
                    dryBoxes = 0;
                    dryCount = 0;
                    return;
                } else {
                    dryBoxesIfNoMythic++;
                }
            }

            if (!empty) {
                System.out.println("reset nextExpectedLootContainerId = " + nextExpectedLootContainerId);
                nextExpectedLootContainerId = -1;
            }

            dryBoxes += dryBoxesIfNoMythic;
            dryCount++;
            ConfigManager.saveConfig();
        } else {
            System.out.println("event.getContainerId() = " + event.getContainerId());
        }
    }
}
