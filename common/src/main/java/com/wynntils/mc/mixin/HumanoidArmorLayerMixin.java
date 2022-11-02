/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.LivingEntityArmorTranslucenceEvent;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity> {

    private LivingEntity livingEntity;

    @Inject(
            method =
                    "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("INVOKE"),
            cancellable = true)
    private void captureEntity(
            PoseStack matrixStack,
            MultiBufferSource buffer,
            int packedLight,
            LivingEntity livingEntity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci) {
        this.livingEntity = livingEntity;
    }

    @Redirect(
            method = "renderModel",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void onOpacityUse(
            EntityModel<?> instance,
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            int packetOverlay,
            float r,
            float g,
            float b,
            float a) {
        LivingEntityArmorTranslucenceEvent event = EventFactory.onLivingEntityArmorTranslucence(livingEntity);

        instance.renderToBuffer(poseStack, consumer, packedLight, packetOverlay, r, g, b, event.getTranslucence());
    }
}
