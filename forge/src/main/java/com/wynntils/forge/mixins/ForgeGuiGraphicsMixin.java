/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.forge.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class ForgeGuiGraphicsMixin {
    // Note: Call site 3 of 3 of ItemTooltipRenderEvent. Check the event class for more info.
    @Inject(
            method =
                    "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void renderTooltipPre(
            Font font,
            List<Component> textComponents,
            Optional<TooltipComponent> tooltipComponent,
            ItemStack itemStack,
            int mouseX,
            int mouseY,
            CallbackInfo callbackInfo,
            @Local(ordinal = 0) LocalRef<List<Component>> textComponentRef,
            @Local(ordinal = 0) LocalRef<ItemStack> itemStackRef,
            @Local(ordinal = 0) LocalIntRef mouseXRef,
            @Local(ordinal = 1) LocalIntRef mouseYRef) {
        ItemTooltipRenderEvent.Pre event = new ItemTooltipRenderEvent.Pre(
                (GuiGraphics) (Object) this,
                itemStackRef.get(),
                textComponentRef.get(),
                mouseXRef.get(),
                mouseYRef.get());
        MixinHelper.post(event);

        if (event.isCanceled()) {
            callbackInfo.cancel();
            return;
        }

        textComponentRef.set(event.getTooltips());
        itemStackRef.set(event.getItemStack());
        mouseXRef.set(event.getMouseX());
        mouseYRef.set(event.getMouseY());
    }

    @Inject(
            method =
                    "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("RETURN"),
            remap = false)
    private void renderTooltipPost(
            Font font,
            List<Component> textComponents,
            Optional<TooltipComponent> tooltipComponent,
            ItemStack itemStack,
            int mouseX,
            int mouseY,
            CallbackInfo ci) {
        MixinHelper.post(new ItemTooltipRenderEvent.Post((GuiGraphics) (Object) this, itemStack, mouseX, mouseY));
    }
}
