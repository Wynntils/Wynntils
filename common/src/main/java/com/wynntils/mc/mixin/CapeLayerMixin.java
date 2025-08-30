/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.PlayerRenderLayerEvent;
import com.wynntils.mc.event.RenderTranslucentCheckEvent;
import com.wynntils.utils.colors.CommonColors;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for {@link CapeLayer} to add support for custom cape translucence.
 * <b>Make sure to sync changes to {@link WynntilsCapeLayer}.</b>
 */
@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
    @Shadow
    @Final
    private HumanoidModel<PlayerRenderState> model;

    @Unique
    private float wynntilsTranslucence;

    @Inject(
            method =
                    "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V",
            at = @At("HEAD"),
            cancellable = true)
    private void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            PlayerRenderState playerRenderState,
            float f,
            float g,
            CallbackInfo ci) {
        PlayerRenderLayerEvent.Cape event = new PlayerRenderLayerEvent.Cape(playerRenderState);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @ModifyArg(
            method =
                    "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "net/minecraft/client/renderer/MultiBufferSource.getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private RenderType setTranslucenceCapeRenderType(
            RenderType original,
            @Local(argsOnly = true) PlayerRenderState playerRenderState,
            @Local PlayerSkin playerSkin) {
        // Always set default translucence value to 1.0f, because cape layer doesn't rendered same as ghost player.
        // It hidden by checking if player is invisible or cape model part is turned off
        RenderTranslucentCheckEvent.Cape event = new RenderTranslucentCheckEvent.Cape(false, playerRenderState, 1.0f);
        MixinHelper.post(event);

        float translucence = event.getTranslucence();

        wynntilsTranslucence = translucence;

        return event.isTranslucent() ? RenderType.entityTranslucent(playerSkin.capeTexture()) : original;
    }

    @WrapOperation(
            method =
                    "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/model/HumanoidModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void setTranslucenceCapeRenderType(
            HumanoidModel<?> instance,
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            Operation<Void> original) {
        // If translucence is 1.0f, then call original method
        if (wynntilsTranslucence == 1f) {
            original.call(instance, poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        // Otherwise, render cape with custom translucence value
        this.model.renderToBuffer(
                poseStack,
                buffer,
                packedLight,
                packedOverlay,
                CommonColors.WHITE.withAlpha(wynntilsTranslucence).asInt());
    }
}
