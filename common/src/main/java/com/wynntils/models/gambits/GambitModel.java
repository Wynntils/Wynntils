/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gambits;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.containers.containers.RaidStartContainer;
import com.wynntils.models.gambits.type.Gambit;
import com.wynntils.models.gambits.type.GambitStatus;
import com.wynntils.models.items.items.gui.GambitItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public final class GambitModel extends Model {
    private final List<Gambit> activeGambits = new ArrayList<>();
    private final List<GambitItem> gambitItems = new ArrayList<>();

    public GambitModel() {
        super(List.of());
    }

    public List<Gambit> getActiveGambits() {
        return Collections.unmodifiableList(activeGambits);
    }

    @SubscribeEvent
    public void onContentSet(ContainerSetContentEvent.Pre event) {
        if (!(Models.Container.getCurrentContainer() instanceof RaidStartContainer container)) return;

        activeGambits.clear();
        gambitItems.clear();
        for (int slot : container.getGambitSlots()) {
            ItemStack itemStack = event.getItems().get(slot);
            if (itemStack.isEmpty()) continue;

            Optional<GambitItem> gambitItem = Models.Item.asWynnItem(itemStack, GambitItem.class);
            if (gambitItem.isEmpty()) {
                WynntilsMod.warn("Failed to parse GambitItem.");
                return;
            }

            gambitItems.add(gambitItem.get());
        }

        parseGambitItems();
    }

    @SubscribeEvent
    public void onSlotSet(ContainerSetSlotEvent.Pre event) {
        if (!(Models.Container.getCurrentContainer() instanceof RaidStartContainer container)) return;
        if (gambitItems.size() != 4) return;

        List<Integer> gambitSlots = new ArrayList<>(container.getGambitSlots());
        for (int i = 0; i < gambitSlots.size(); i++) {
            int slot = gambitSlots.get(i);
            if (slot == event.getSlot()) {
                ItemStack itemStack = event.getItemStack();
                if (itemStack.isEmpty()) return;

                Optional<GambitItem> gambitItem = Models.Item.asWynnItem(itemStack, GambitItem.class);
                if (gambitItem.isEmpty()) return;

                gambitItems.set(i, gambitItem.get());
                parseGambitItems();
                return;
            }
        }
    }

    private void parseGambitItems() {
        boolean isFirst = true;
        for (GambitItem item : gambitItems) {
            GambitStatus status = item.getGambitStatus();

            // only clear the gambits before adding the first item, skip if player is readied up
            if (isFirst && (status != GambitStatus.PLAYER_READY)) {
                isFirst = false;
                activeGambits.clear();
            }

            if (status == GambitStatus.ENABLED) {
                activeGambits.add(item.getGambit());
            }
        }
    }
}
