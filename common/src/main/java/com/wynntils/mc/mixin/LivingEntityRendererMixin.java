/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.LivingEntityRenderTranslucentCheckEvent;
import com.wynntils.mc.extension.EntityExtension;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Unique
    private float wynntilsTranslucence;

    @WrapOperation(
            method =
                    "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType onTranslucentCheck(
            LivingEntityRenderer<?, ?> instance,
            LivingEntity livingEntity,
            boolean bodyVisible,
            boolean translucent,
            boolean glowing,
            Operation<RenderType> original) {
        LivingEntityRenderTranslucentCheckEvent event =
                new LivingEntityRenderTranslucentCheckEvent(translucent, livingEntity, translucent ? 0.15f : 1f);
        MixinHelper.post(event);

        // Save translucence value for later use
        wynntilsTranslucence = event.getTranslucence();

        return original.call(instance, livingEntity, bodyVisible, event.isTranslucent(), glowing);
    }

    @WrapOperation(
            method =
                    "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
    private void onOpacityUse(
            EntityModel<? extends LivingEntity> instance,
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            int overlayCoords,
            int originalTranslucence,
            Operation<Void> original) {
        // FIXME: Use wynntilsTranslucence instead of originalTranslucence
        original.call(instance, poseStack, consumer, packedLight, overlayCoords, originalTranslucence);
    }

    @Inject(
            method =
                    "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private <T extends LivingEntity> void onRender(
            T entity,
            float entityYaw,
            float partialTicks,
            PoseStack matrixStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci) {
        if (!((EntityExtension) entity).isRendered()) {
            ci.cancel();
        }
    }
}
