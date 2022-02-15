/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ContainerClickEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"))
    private void handleInventoryMouseClickPre(
            int containerId,
            int slotId,
            int mouseButton,
            ClickType clickType,
            Player player,
            CallbackInfo ci) {

        ItemStack itemStack;
        if (slotId >= 0) {
            itemStack = player.containerMenu.getSlot(slotId).getItem();
        } else {
            itemStack = ItemStack.EMPTY;
        }

        WynntilsMod.getEventBus()
                .post(new ContainerClickEvent(containerId, slotId, itemStack, clickType, slotId));
    }
}
