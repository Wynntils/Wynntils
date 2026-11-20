/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.mixin.accessors.TooltipAccessor;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.inventory.SlotWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlotWidget.class)
public class SlotWidgetMixin {
    @Shadow
    @Final
    private Slot slot;

    // Note: Call site 1 of 3 of ItemTooltipRenderEvent. Check the event class for more info.
    @Inject(
            method = "extractTooltipForNextRenderPass(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/AbstractWidget;extractTooltipForNextRenderPass(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"),
            cancellable = true)
    private void extractItemTooltip(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (!MixinHelper.onWynncraft()) return;

        ItemStack itemStack = slot.getItem();

        ItemTooltipRenderEvent event =
                new ItemTooltipRenderEvent(graphics, itemStack, Screen.getTooltipFromItem(McUtils.mc(), itemStack));
        MixinHelper.post(event);

        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        SlotWidget widget = (SlotWidget) (Object) this;
        List<Component> lines = event.getTooltips();

        Tooltip tooltip = Tooltip.createForItem(lines, itemStack);
        // These components are already laid out line by line.
        ((TooltipAccessor) tooltip).setSplitLines(false);

        widget.setTooltip(tooltip);
    }

    @Inject(
            method = "extractWidgetRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void extractSlotPre(GuiGraphicsExtractor guiGraphics, CallbackInfo info) {
        SlotRenderEvent.Pre event = new SlotRenderEvent.Pre(guiGraphics, slot);
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
        MixinHelper.post(new SlotRenderEvent.CountPre(guiGraphics, slot));
    }

    @Inject(method = "extractWidgetRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At("RETURN"))
    private void extractSlotPost(GuiGraphicsExtractor guiGraphics, CallbackInfo info) {
        MixinHelper.post(new SlotRenderEvent.Post(guiGraphics, slot));
    }
}
