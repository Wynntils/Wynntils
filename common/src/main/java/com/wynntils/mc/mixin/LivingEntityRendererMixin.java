/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.LivingEntityRenderTranslucentCheckEvent;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    // Can't find an impl without saving args
    LivingEntity capturedEntity;
    float overrideTranslucence;

    @Inject(
            method =
                    "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    public void onRender(
            LivingEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack matrixStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci) {
        capturedEntity = entity;
    }

    @ModifyVariable(
            method =
                    "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("STORE"),
            ordinal = 1)
    public boolean onTranslucentCheck(boolean bl2) {
        LivingEntityRenderTranslucentCheckEvent event =
                new LivingEntityRenderTranslucentCheckEvent(bl2, capturedEntity, bl2 ? 0.15f : 1f);
        WynntilsMod.getEventBus().post(event);

        overrideTranslucence = event.getTranslucence();
        return event.isTranslucent();
    }

    @Redirect(
            method =
                    "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    public void onOpacityUse(
            EntityModel<?> instance,
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            int packetOverlay,
            float r,
            float g,
            float b,
            float a) {
        instance.renderToBuffer(
                poseStack, consumer, packedLight, packetOverlay, r, g, b, overrideTranslucence);
    }
}
