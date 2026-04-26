/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gambits;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.containers.containers.RaidStartContainer;
import com.wynntils.models.gambits.type.Gambit;
import com.wynntils.models.items.items.gui.RaidPlayerItem;
import com.wynntils.utils.mc.McUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public final class GambitModel extends Model {
    private List<Gambit> activeGambits = List.of();

    public GambitModel() {
        super(List.of());
    }

    public List<Gambit> getActiveGambits() {
        return Collections.unmodifiableList(activeGambits);
    }

    @SubscribeEvent
    public void onContentSet(ContainerSetContentEvent.Pre event) {
        if (!(Models.Container.getCurrentContainer() instanceof RaidStartContainer container)) return;

        for (int slot : container.getPlayerSlots()) {
            parsePlayerItem(event.getItems().get(slot));
        }
    }

    @SubscribeEvent
    public void onSlotSet(ContainerSetSlotEvent.Pre event) {
        if (!(Models.Container.getCurrentContainer() instanceof RaidStartContainer container)) return;

        for (int slot : container.getPlayerSlots()) {
            if (slot == event.getSlot()) {
                parsePlayerItem(event.getItemStack());
                return;
            }
        }
    }

    private void parsePlayerItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) return;

        Optional<RaidPlayerItem> playerItemOptional = Models.Item.asWynnItem(itemStack, RaidPlayerItem.class);
        if (playerItemOptional.isEmpty()) return;

        RaidPlayerItem playerItem = playerItemOptional.get();

        if (playerItem.isNickname()) {
            String nickname = Models.Character.getNickname();
            if (nickname == null) return;
            if (!nickname.equals(playerItem.getPlayerName())) return;
        } else {
            if (!McUtils.playerName().equals(playerItem.getPlayerName())) return;
        }

        activeGambits = playerItem.getEnabledGambits();
    }
}
