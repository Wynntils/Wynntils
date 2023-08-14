/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.GroundItemEntityTransformEvent;
import com.wynntils.mc.event.ItemCountOverlayRenderEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Unique
    private int wynntilsCountOverlayColor;

    @Inject(
            method =
                    "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at =
                    @At(
                            target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
                            shift = At.Shift.BEFORE,
                            value = "INVOKE"))
    private void onRenderItem(
            ItemStack itemStack,
            ItemDisplayContext itemDisplayContext,
            boolean leftHand,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay,
            BakedModel model,
            CallbackInfo ci) {
        if (itemDisplayContext != ItemDisplayContext.GROUND) return;

        MixinHelper.post(new GroundItemEntityTransformEvent(poseStack, itemStack));
    }

    @ModifyVariable(
            method =
                    "renderGuiItemDecorations(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private String renderGuiItemDecorations(
            String text,
            PoseStack poseStack,
            Font font,
            ItemStack itemStack,
            int xPosition,
            int yPosition,
            String ignored) {
        if (!MixinHelper.onWynncraft()) {
            wynntilsCountOverlayColor = 0xFFFFFF;
            return text;
        }

        String count = (itemStack.getCount() == 1) ? "" : String.valueOf(itemStack.getCount());
        String countString = (text == null) ? count : text;

        ItemCountOverlayRenderEvent event = new ItemCountOverlayRenderEvent(itemStack, countString, 0xFFFFFF);
        MixinHelper.post(event);
        // Storing the color in a field assumes this is only called single-threaded by the render thread
        wynntilsCountOverlayColor = event.getCountColor();

        return event.getCountString();
    }

    @WrapOperation(
            method =
                    "renderGuiItemDecorations(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/Font;drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I"))
    private int changeCountOverlayColor(
            Font instance,
            String text,
            float x,
            float y,
            int color,
            boolean dropShadow,
            Matrix4f matrix,
            MultiBufferSource bufferSource,
            Font.DisplayMode displayMode,
            int backgroundColor,
            int packedLightCoords,
            Operation<Integer> original) {
        return original.call(
                instance,
                text,
                x,
                y,
                wynntilsCountOverlayColor,
                dropShadow,
                matrix,
                bufferSource,
                displayMode,
                backgroundColor,
                packedLightCoords);
    }
}
