/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.NametagRenderEvent;
import com.wynntils.mc.event.RenderLayerRegistrationEvent;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin
        extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    protected PlayerRendererMixin(Context context, PlayerModel<AbstractClientPlayer> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Z)V",
            at = @At("RETURN"))
    private void onCtor(EntityRendererProvider.Context context, boolean bl, CallbackInfo ci) {
        RenderLayerRegistrationEvent event =
                new RenderLayerRegistrationEvent((PlayerRenderer) (Object) this, context, bl);
        MixinHelper.postAlways(event);
        for (RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> layer : event.getRegisteredLayers()) {
            this.addLayer(layer);
        }
    }

    @Inject(
            method =
                    "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onNameTagRenderPre(
            AbstractClientPlayer entity,
            Component displayName,
            PoseStack matrixStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci) {
        NametagRenderEvent event = new NametagRenderEvent(
                entity, displayName, matrixStack, buffer, packedLight, this.entityRenderDispatcher, this.getFont());
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
