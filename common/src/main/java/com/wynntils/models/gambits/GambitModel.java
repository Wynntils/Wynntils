/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gambits;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemHandler;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.models.containers.Container;
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

    public GambitModel() {
        super(List.of());
    }

    public List<Gambit> getActiveGambits() {
        return Collections.unmodifiableList(activeGambits);
    }

    @SubscribeEvent
    public void onContentSet(ContainerSetContentEvent.Pre event) {
        Container currentContainer = Models.Container.getCurrentContainer();

        if (currentContainer instanceof RaidStartContainer raidStartContainer) {
            List<Integer> gambitSlots = new ArrayList<>(raidStartContainer.getGambitSlots());
            boolean isFirst = true;

            for (Integer i : gambitSlots) {
                ItemStack itemStack = event.getItems().get(i);
                if (itemStack.isEmpty()) continue;

                Optional<ItemAnnotation> item = ItemHandler.getItemStackAnnotation(itemStack);
                Gambit gambit = null;
                GambitStatus gambitStatus = null;
                if (item.isPresent() && item.get() instanceof GambitItem) {
                    gambit = ((GambitItem) item.get()).getGambit();
                    gambitStatus = ((GambitItem) item.get()).getGambitStatus();
                }

                if (gambit == null) continue;

                // only clear the gambits before adding the first item, skip if player is readied up
                if (isFirst && (gambitStatus != GambitStatus.PLAYER_READY)) {
                    isFirst = false;
                    activeGambits.clear();
                }

                if (gambitStatus == gambitStatus.ENABLED) {
                    activeGambits.add(gambit);
                }
            }
        }
    }
}
