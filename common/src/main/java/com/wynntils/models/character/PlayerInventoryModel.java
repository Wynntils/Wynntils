/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character;

import com.wynntils.core.components.Model;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.List;
import java.util.Objects;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class PlayerInventoryModel extends Model {
    private int emptySlots = 0;

    public PlayerInventoryModel() {
        super(List.of());

        resetCache();
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        if (e.getNewState() == WorldState.WORLD) {
            updateCache();
        } else {
            resetCache();
        }
    }

    @SubscribeEvent
    public void onContainerSetEvent(ContainerSetContentEvent.Post e) {
        // Only update if the container is the player inventory
        if (e.getContainerId() == McUtils.player().inventoryMenu.containerId) {
            updateCache();
        }
    }

    @SubscribeEvent
    public void onSlotSetEvent(SetSlotEvent.Post e) {
        // Only update if the container is the player inventory
        if (Objects.equals(e.getContainer(), McUtils.player().getInventory())) {
            updateCache();
        }
    }

    private void updateCache() {
        emptySlots = InventoryUtils.getEmptySlots(McUtils.inventory());
    }

    private void resetCache() {
        emptySlots = 0;
    }

    public CappedValue getUsedSlots() {
        return new CappedValue(28 - emptySlots, 28);
    }
}
