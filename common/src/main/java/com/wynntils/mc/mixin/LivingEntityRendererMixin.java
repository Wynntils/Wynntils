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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Shadow
    @Nullable
    protected abstract RenderType getRenderType(LivingEntity livingEntity, boolean bl, boolean bl2, boolean bl3);

    // Can't find an impl without saving args
    float overrideTranslucence;

    @Redirect(
            method =
                    "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;"))
    public RenderType onTranslucentCheck(
            LivingEntityRenderer<?, ?> instance, LivingEntity livingEntity, boolean bl, boolean bl2, boolean bl3) {
        LivingEntityRenderTranslucentCheckEvent event =
                new LivingEntityRenderTranslucentCheckEvent(bl2, livingEntity, bl2 ? 0.15f : 1f);
        WynntilsMod.getEventBus().post(event);

        overrideTranslucence = event.getTranslucence();
        return getRenderType(livingEntity, bl, event.isTranslucent(), bl3);
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
        instance.renderToBuffer(poseStack, consumer, packedLight, packetOverlay, r, g, b, overrideTranslucence);
    }
}
