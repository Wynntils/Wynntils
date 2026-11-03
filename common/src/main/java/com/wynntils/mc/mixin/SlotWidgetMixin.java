/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.utils.mc.ItemTooltipSupplier;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.inventory.SlotWidget;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SlotWidget.class)
public class SlotWidgetMixin {
    @Shadow
    @Final
    private Function<Slot, Tooltip> tooltipSupplier;

    @Inject(
            method = "extractWidgetRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void extractSlotPre(GuiGraphicsExtractor guiGraphics, CallbackInfo info) {
        SlotRenderEvent.Pre event = new SlotRenderEvent.Pre(guiGraphics, (SlotWidget) (Object) this);
        MixinHelper.post(event);

        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(
            method = "extractWidgetRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void extractSlotPreCount(GuiGraphicsExtractor guiGraphics, CallbackInfo info) {
        MixinHelper.post(new SlotRenderEvent.CountPre(guiGraphics, (SlotWidget) (Object) this));
    }

    @Inject(method = "extractWidgetRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At("RETURN"))
    private void extractSlotPost(GuiGraphicsExtractor guiGraphics, CallbackInfo info) {
        MixinHelper.post(new SlotRenderEvent.Post(guiGraphics, (SlotWidget) (Object) this));
    }

    @Inject(method = "defaultTooltipSupplier", at = @At("HEAD"), cancellable = true)
    private static void captureDefaultTooltip(
            Minecraft minecraft, CallbackInfoReturnable<Function<Slot, Tooltip>> cir) {
        cir.setReturnValue(new ItemTooltipSupplier(minecraft));
    }

    @Inject(
            method = "extractTooltipForNextRenderPass(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/components/inventory/SlotWidget;updateTooltip()V",
                            shift = At.Shift.AFTER),
            cancellable = true)
    private void extractTooltipPre(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        SlotWidget widget = (SlotWidget) (Object) this;
        if (!widget.slot().hasItem() || !(tooltipSupplier instanceof ItemTooltipSupplier supplier)) return;

        ItemTooltipRenderEvent.Pre event = new ItemTooltipRenderEvent.Pre(
                graphics, supplier.getItemStack(), supplier.getTooltipLines(), graphics.mouseX(), graphics.mouseY());
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        widget.setTooltip(Tooltip.createForItem(event.getTooltips(), event.getItemStack()));
    }
}
