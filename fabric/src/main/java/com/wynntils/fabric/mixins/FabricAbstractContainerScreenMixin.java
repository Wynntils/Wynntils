/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class FabricAbstractContainerScreenMixin {
    // Note: Call site 2 of 3 of ItemTooltipRenderEvent. Check the event class for more info.
    //       See NeoForgeGuiGraphicsMixin#renderTooltipPre for the Forge mixin.
    @WrapOperation(
            method = "extractTooltip(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;Z)V"))
    private void extractTooltipPre(
            GuiGraphicsExtractor instance,
            Font font,
            List<Component> tooltipLines,
            Optional<TooltipComponent> visualTooltipComponent,
            int mouseX,
            int mouseY,
            Identifier backgroundTexture,
            boolean extraSpaceAfterFirstLine,
            Operation<Void> operation,
            @Local ItemStack itemStack) {
        ItemTooltipRenderEvent.Pre event =
                new ItemTooltipRenderEvent.Pre(instance, itemStack, tooltipLines, mouseX, mouseY);
        MixinHelper.post(event);
        if (event.isCanceled()) return;

        operation.call(
                instance,
                font,
                event.getTooltips(),
                event.getItemStack().getTooltipImage(),
                event.getMouseX(),
                event.getMouseY(),
                backgroundTexture,
                extraSpaceAfterFirstLine);
    }

    // See the NeoForgeAbstractContainerScreenMixin#renderSlotPreCount for the Forge mixin.
    @Inject(
            method =
                    "extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/inventory/Slot;II)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void extractSlotPreCount(
            GuiGraphicsExtractor guiGraphics, Slot slot, int mouseX, int mouseY, CallbackInfo info) {
        MixinHelper.post(new SlotRenderEvent.CountPre(guiGraphics, (Screen) (Object) this, slot));
    }
}
