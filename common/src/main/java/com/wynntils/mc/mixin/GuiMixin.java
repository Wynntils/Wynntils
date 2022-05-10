/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.wc.objects.item.render.RenderedHotbarBackground;
import com.wynntils.wc.objects.item.render.RenderedHotbarForeground;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(
            method = "renderSlot(IIFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("HEAD"))
    private void renderSlotPre(int x, int y, float ticks, Player player, ItemStack stack, int i, CallbackInfo info) {
        if (stack instanceof RenderedHotbarBackground) {
            ((RenderedHotbarBackground) stack).renderHotbarBackground(x, y, stack);
        }
    }

    @Inject(
            method = "renderSlot(IIFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At("RETURN"))
    private void renderSlotPost(int x, int y, float ticks, Player player, ItemStack stack, int i, CallbackInfo info) {
        if (stack instanceof RenderedHotbarForeground) {
            ((RenderedHotbarForeground) stack).renderHotbarForeground(x, y, stack);
        }
    }
}
