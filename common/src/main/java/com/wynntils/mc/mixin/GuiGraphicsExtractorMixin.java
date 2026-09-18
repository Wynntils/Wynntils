/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ItemCooldownRenderEvent;
import com.wynntils.mc.event.ItemCountOverlayRenderEvent;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
    // Note: Call site 1 of 3 of ItemTooltipRenderEvent. Check the event class for more info.
    //       This mixin works on Fabric, and on NeoForge as well.
    @WrapOperation(
            method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;Z)V"))
    private void setTooltipForNextFramePre(
            GuiGraphicsExtractor instance,
            Font font,
            List<Component> tooltipLines,
            Optional<TooltipComponent> visualTooltipComponent,
            int mouseX,
            int mouseY,
            Identifier backgroundTexture,
            boolean extraSpaceAfterFirstLine,
            Operation<Void> operation,
            @Local(argsOnly = true) ItemStack itemStack) {
        ItemTooltipRenderEvent.Pre event = new ItemTooltipRenderEvent.Pre(
                (GuiGraphicsExtractor) (Object) this,
                itemStack,
                Screen.getTooltipFromItem(McUtils.mc(), itemStack),
                mouseX,
                mouseY);
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

    @ModifyVariable(
            method =
                    "itemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private String itemCountPre(
            String text,
            Font font,
            ItemStack itemStack,
            int x,
            int y,
            String ignored,
            @Share("wynntilsCountOverlayColor") LocalIntRef wynntilsCountOverlayColor) {
        if (!MixinHelper.onWynncraft()) {
            wynntilsCountOverlayColor.set(0xFFFFFFFF);
            return text;
        }

        String count = (itemStack.getCount() == 1) ? null : String.valueOf(itemStack.getCount());
        String countString = (text == null) ? count : text;

        ItemCountOverlayRenderEvent event = new ItemCountOverlayRenderEvent(itemStack, countString, 0xFFFFFFFF);
        MixinHelper.post(event);
        wynntilsCountOverlayColor.set(event.getCountColor());

        return event.getCountString();
    }

    @Inject(
            method = "itemCooldown(Lnet/minecraft/world/item/ItemStack;II)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;IIIII)V"),
            cancellable = true)
    private void itemCooldownPre(ItemStack stack, int x, int y, CallbackInfo ci) {
        ItemCooldownRenderEvent event = new ItemCooldownRenderEvent(stack);
        MixinHelper.post(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @WrapOperation(
            method =
                    "itemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V"))
    private void changeCountOverlayColor(
            GuiGraphicsExtractor guiGraphics,
            Font font,
            String text,
            int x,
            int y,
            int color,
            boolean dropShadow,
            Operation<Void> original,
            @Share("wynntilsCountOverlayColor") LocalIntRef wynntilsCountOverlayColor) {
        original.call(guiGraphics, font, text, x, y, wynntilsCountOverlayColor.get(), dropShadow);
    }
}
