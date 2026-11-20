/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric.mixins;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = AbstractContainerScreen.class, priority = Integer.MIN_VALUE)
public abstract class FabricAbstractContainerScreenMixin {
    // Note: Call site 2 of 3 of ItemTooltipRenderEvent. Check the event class for more info.
    //       See NeoForgeGuiGraphicsMixin#renderTooltipPre for the Forge mixin.
    //    @WrapOperation(
    //            method = "extractTooltip(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
    //            at =
    //                    @At(
    //                            value = "INVOKE",
    //                            target =
    //
    // "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;Z)V"))
    //    private void extractTooltipPre(
    //            GuiGraphicsExtractor instance,
    //            Font font,
    //            List<Component> tooltipLines,
    //            Optional<TooltipComponent> visualTooltipComponent,
    //            int mouseX,
    //            int mouseY,
    //            Identifier backgroundTexture,
    //            boolean extraSpaceAfterFirstLine,
    //            Operation<Void> operation,
    //            @Local ItemStack itemStack) {
    //        ItemTooltipRenderEvent.Pre event =
    //                new ItemTooltipRenderEvent.Pre(instance, itemStack, tooltipLines, mouseX, mouseY);
    //        MixinHelper.post(event);
    //        if (event.isCanceled()) return;
    //
    //        operation.call(
    //                instance,
    //                font,
    //                event.getTooltips(),
    //                event.getItemStack().getTooltipImage(),
    //                event.getMouseX(),
    //                event.getMouseY(),
    //                backgroundTexture,
    //                extraSpaceAfterFirstLine);
    //    }
}
