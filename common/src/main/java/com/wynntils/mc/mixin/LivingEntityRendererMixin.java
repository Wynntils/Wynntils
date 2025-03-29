/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.GetCameraEntityEvent;
import com.wynntils.mc.event.PlayerRenderEvent;
import com.wynntils.mc.event.RenderTranslucentCheckEvent;
import com.wynntils.utils.colors.CommonColors;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntityRenderState> {
    @Unique
    private float wynntilsTranslucence;

    @WrapOperation(
            method =
                    "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType onTranslucentCheck(
            LivingEntityRenderer<?, ?, ?> instance,
            LivingEntityRenderState livingEntityRenderState,
            boolean bodyVisible,
            boolean translucent,
            boolean glowing,
            Operation<RenderType> original) {
        RenderTranslucentCheckEvent.Body event =
                new RenderTranslucentCheckEvent.Body(translucent, livingEntityRenderState, translucent ? 0.15f : 1f);
        MixinHelper.post(event);

        // Save translucence value for later use
        wynntilsTranslucence = event.getTranslucence();

        return original.call(instance, livingEntityRenderState, bodyVisible, translucent, glowing);
    }

    @WrapOperation(
            method =
                    "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
    private void onOpacityUse(
            EntityModel<? super LivingEntityRenderState> instance,
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            int overlayCoords,
            int originalTranslucence,
            Operation<Void> original) {
        original.call(
                instance,
                poseStack,
                consumer,
                packedLight,
                overlayCoords,
                CommonColors.WHITE.withAlpha(wynntilsTranslucence).asInt());
    }

    @Inject(
            method =
                    "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN"))
    private void onRenderPost(
            T renderState, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (!(renderState instanceof PlayerRenderState playerRenderState)) return;

        PlayerRenderEvent event = new PlayerRenderEvent(playerRenderState, matrixStack, buffer, packedLight);
        MixinHelper.post(event);
    }

    @ModifyExpressionValue(
            method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
    private Entity getCameraEntity(Entity entity) {
        GetCameraEntityEvent cameraEntityEvent = new GetCameraEntityEvent(entity);
        MixinHelper.post(cameraEntityEvent);

        return cameraEntityEvent.getEntity();
    }
}
