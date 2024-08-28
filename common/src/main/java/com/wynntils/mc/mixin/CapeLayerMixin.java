/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.PlayerRenderLayerEvent;
import com.wynntils.mc.event.RenderTranslucentCheckEvent;
import com.wynntils.mc.extension.PlayerModelExtension;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
    @Inject(
            method =
                    "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
            at = @At("HEAD"),
            cancellable = true)
    private void render(
            PoseStack matrixStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer livingEntity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci) {
        PlayerRenderLayerEvent.Cape event = new PlayerRenderLayerEvent.Cape(livingEntity);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    /**
     * Translucence value needs to pass into {@link net.minecraft.client.model.PlayerModel} class,
     * because {@link net.minecraft.client.model.PlayerModel#renderCloak(PoseStack, VertexConsumer, int, int)} method does not accept {@code color}
     * argument
     */
    @ModifyArg(
            method =
                    "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "net/minecraft/client/renderer/MultiBufferSource.getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private RenderType setTranslucenceCapeRenderType(
            RenderType original,
            @Local(argsOnly = true) AbstractClientPlayer livingEntity,
            @Local PlayerSkin playerSkin) {
        // Always set default translucence value to 1.0f, because cape layer doesn't rendered same as ghost player.
        // It hidden by checking if player is invisible or cape model part is turned off
        RenderTranslucentCheckEvent.Cape event = new RenderTranslucentCheckEvent.Cape(false, livingEntity, 1.0f);
        MixinHelper.post(event);

        float translucence = event.getTranslucence();

        ((PlayerModelExtension) ((CapeLayer) (Object) this).getParentModel()).setTranslucenceCape(translucence);

        return event.isTranslucent() ? RenderType.entityTranslucent(playerSkin.capeTexture()) : original;
    }
}
