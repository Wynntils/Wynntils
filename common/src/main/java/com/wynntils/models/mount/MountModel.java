/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.actionbar.matchers.MountEnergySegmentMatcher;
import com.wynntils.models.mount.actionbar.segments.MountEnergySegment;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public final class MountModel extends Model {
    // Parsed from the UI element so is not as accurate as the item tooltip
    private CappedValue currentMountEnergy = CappedValue.EMPTY;

    private boolean hideMountEnergy = false;

    public MountModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(new MountEnergySegmentMatcher());
    }

    @SubscribeEvent
    public void onActionBarRender(ActionBarRenderEvent event) {
        if (!hideMountEnergy) return;

        event.setSegmentEnabled(MountEnergySegment.class, false);
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresentOrElse(MountEnergySegment.class, this::updateMountEnergy, this::clearMountEnergy);
    }

    public Optional<MountItem> getMount() {
        int mountSlot = findMountSlotNum();
        if (mountSlot == -1) return Optional.empty();

        return Models.Item.asWynnItem(McUtils.inventory().getItem(mountSlot), MountItem.class);
    }

    public int findMountSlotNum() {
        Inventory inventory = McUtils.inventory();
        for (int slotNum = 0; slotNum < Inventory.INVENTORY_SIZE; slotNum++) {
            ItemStack itemStack = inventory.getItem(slotNum);
            if (Models.Item.asWynnItem(itemStack, MountItem.class).isPresent()) {
                return slotNum;
            }
        }
        return -1;
    }

    public void setHideMountEnergy(boolean hide) {
        hideMountEnergy = hide;
    }

    public Optional<CappedValue> getCurrentMountEnergy() {
        if (currentMountEnergy == CappedValue.EMPTY) return Optional.empty();
        return Optional.of(currentMountEnergy);
    }

    private void updateMountEnergy(MountEnergySegment segment) {
        currentMountEnergy = segment.getCappedEnergy();
    }

    private void clearMountEnergy() {
        currentMountEnergy = CappedValue.EMPTY;
    }
}
