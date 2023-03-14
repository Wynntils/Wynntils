/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.features.wynntils.DataStorageFeature;
import com.wynntils.mc.event.ChestMenuQuickMoveEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.wynn.WynnItemMatchers;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class LootChestModel extends Model {
    private static final int LOOT_CHEST_ITEM_COUNT = 27;

    private int nextExpectedLootContainerId = -2;

    public LootChestModel(ContainerModel containerModel) {
        super(List.of(containerModel));
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent event) {
        if (Models.Container.isLootChest(ComponentUtils.getUnformatted(event.getTitle()))) {
            nextExpectedLootContainerId = event.getContainerId();

            DataStorageFeature.INSTANCE.dryCount.updateConfig(DataStorageFeature.INSTANCE.dryCount.get() + 1);
            Managers.Config.saveConfig();
        }
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent event) {
        if (event.getContainerId() != nextExpectedLootContainerId) return;
        if (event.getSlot() >= LOOT_CHEST_ITEM_COUNT) return;

        ItemStack itemStack = event.getItemStack();

        if (!WynnItemMatchers.isGearBox(itemStack)) return;

        GearTier gearTier = GearTier.fromComponent(itemStack.getHoverName());

        if (gearTier == GearTier.MYTHIC) {
            DataStorageFeature.INSTANCE.dryBoxes.updateConfig(0);
            DataStorageFeature.INSTANCE.dryCount.updateConfig(0);
        } else {
            DataStorageFeature.INSTANCE.dryBoxes.updateConfig(DataStorageFeature.INSTANCE.dryBoxes.get() + 1);
        }

        Managers.Config.saveConfig();
    }

    @SubscribeEvent
    public void onQuickMove(ChestMenuQuickMoveEvent event) {
        if (event.getContainerId() == nextExpectedLootContainerId) {
            nextExpectedLootContainerId = -2;
        }
    }
}
