/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.LivingEntityRenderTranslucentCheckEvent;
import com.wynntils.mc.event.PlayerRenderLayerEvent;
import com.wynntils.mc.extension.PlayerModelExtension;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    CapeLayerMixin() {
        super(null);
    }

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

    @ModifyArg(
            method = "render",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "net/minecraft/client/renderer/MultiBufferSource.getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private RenderType setCapeRenderType(
            RenderType original,
            @Local(argsOnly = true) AbstractClientPlayer livingEntity,
            @Local PlayerSkin playerSkin) {
        boolean isGhostPlayer = Models.Player.isPlayerGhost(livingEntity);
        LivingEntityRenderTranslucentCheckEvent event =
                new LivingEntityRenderTranslucentCheckEvent(isGhostPlayer, livingEntity, isGhostPlayer ? 0.15f : 1f);
        MixinHelper.post(event);

        ((PlayerModelExtension) this.getParentModel()).setTranslucenceCape(event.getTranslucence());

        return isGhostPlayer ? RenderType.entityTranslucent(playerSkin.capeTexture()) : original;
    }
}
