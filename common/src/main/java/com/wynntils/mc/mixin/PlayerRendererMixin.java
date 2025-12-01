/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.PlayerNametagRenderEvent;
import com.wynntils.services.cosmetics.CosmeticsService;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin
        extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {
    protected PlayerRendererMixin(Context context, PlayerModel entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Z)V",
            at = @At("RETURN"))
    private void onCtor(EntityRendererProvider.Context context, boolean bl, CallbackInfo ci) {
        // Note: This is needed because constructor is called in a static context, where class loading is unpredictable.
        //       This makes it so events can't be used here, since this might happen before initializing features.
        CosmeticsService.getRegisteredLayers()
                .forEach(layerProvider -> this.addLayer(layerProvider.apply(this, context)));
    }

    @Inject(
            method =
                    "renderNameTag(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onNameTagRenderPre(
            PlayerRenderState renderState,
            Component displayName,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci) {
        PlayerNametagRenderEvent event = new PlayerNametagRenderEvent(
                renderState, displayName, poseStack, buffer, packedLight, this.entityRenderDispatcher, this.getFont());
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
