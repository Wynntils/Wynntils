/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.PlayerRenderLayerEvent;
import com.wynntils.mc.event.RenderTranslucentCheckEvent;
import com.wynntils.utils.colors.CommonColors;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.spongepowered.asm.mixin.Mixin;
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
    @Unique
    private float wynntilsTranslucence;

    @Inject(
            method =
                    "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
            at = @At("HEAD"),
            cancellable = true)
    private void render(
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int packedLight,
            AvatarRenderState renderState,
            float yRot,
            float xRot,
            CallbackInfo ci) {
        PlayerRenderLayerEvent.Cape event = new PlayerRenderLayerEvent.Cape(renderState);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @ModifyArg(
            method =
                    "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    private RenderType setTranslucenceCapeRenderType(
            RenderType original, @Local(argsOnly = true) AvatarRenderState avatarRenderState) {
        // Always set default translucence value to 1.0f, because cape layer doesn't rendered same as ghost player.
        // It hidden by checking if player is invisible or cape model part is turned off
        RenderTranslucentCheckEvent.Cape event = new RenderTranslucentCheckEvent.Cape(false, avatarRenderState, 1.0f);
        MixinHelper.post(event);

        float translucence = event.getTranslucence();

        wynntilsTranslucence = translucence;

        return event.isTranslucent()
                ? RenderTypes.entityTranslucent(avatarRenderState.skin.cape().texturePath())
                : original;
    }

    @WrapOperation(
            method =
                    "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    private void setTranslucenceCapeRenderType(
            SubmitNodeCollector instance,
            Model model,
            Object renderState,
            PoseStack poseStack,
            RenderType renderType,
            int packedLight,
            int packedOverlay,
            int outlineColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            Operation<Void> original) {
        // If translucence is 1.0f, then call original method
        if (wynntilsTranslucence == 1f) {
            original.call(
                    instance,
                    model,
                    renderState,
                    poseStack,
                    renderType,
                    packedLight,
                    packedOverlay,
                    outlineColor,
                    crumblingOverlay);
            return;
        }

        // Otherwise, render cape with custom translucence value
        instance.submitModel(
                model,
                renderState,
                poseStack,
                renderType,
                packedLight,
                packedOverlay,
                CommonColors.WHITE.withAlpha(wynntilsTranslucence).asInt(),
                null,
                outlineColor,
                crumblingOverlay);
    }
}
