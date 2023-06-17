/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ChestMenuQuickMoveEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.models.containers.event.MythicFoundEvent;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.GearBoxItem;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class LootChestModel extends Model {
    private static final int LOOT_CHEST_ITEM_COUNT = 27;

    private final Storage<Integer> dryCount = new Storage<>(0);
    private final Storage<Integer> dryBoxes = new Storage<>(0);

    private int nextExpectedLootContainerId = -2;

    public LootChestModel(ContainerModel containerModel) {
        super(List.of(containerModel));
    }

    public int getDryCount() {
        return dryCount.get();
    }

    public Integer getDryBoxes() {
        return dryBoxes.get();
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent event) {
        if (Models.Container.isLootChest(
                StyledText.fromComponent(event.getTitle()).getStringWithoutFormatting())) {
            nextExpectedLootContainerId = event.getContainerId();

            dryCount.store(dryCount.get() + 1);
            Managers.Config.saveConfig();
        }
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent.Post event) {
        if (event.getContainerId() != nextExpectedLootContainerId) return;
        if (event.getSlot() >= LOOT_CHEST_ITEM_COUNT) return;

        ItemStack itemStack = event.getItemStack();
        Optional<GearBoxItem> wynnItem = Models.Item.asWynnItem(itemStack, GearBoxItem.class);
        if (wynnItem.isEmpty()) return;
        GearBoxItem gearBox = wynnItem.get();
        if (gearBox.getGearTier() == GearTier.MYTHIC) {
            if (gearBox.getGearType() != GearType.MASTERY_TOME) {
                dryBoxes.store(0);
                dryCount.store(0);
            }
            WynntilsMod.postEvent(new MythicFoundEvent(itemStack));
        } else {
            dryBoxes.store(dryBoxes.get() + 1);
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
