/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.NametagRenderEvent;
import java.util.List;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends EntityRenderer<Player> {
    protected PlayerRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method =
                    "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true)
    public void onNameTagRenderPre(
            AbstractClientPlayer entity,
            Component displayName,
            PoseStack matrixStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci) {
        matrixStack.pushPose();

        NametagRenderEvent event = EventFactory.onNameTagRender(entity, displayName, matrixStack, buffer, packedLight);
        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        List<MutableComponent> injected = event.getInjectedLines();

        for (MutableComponent component : injected) {
            super.renderNameTag(entity, component, matrixStack, buffer, packedLight);
            matrixStack.translate(0.0, 0.25875f, 0.0);
        }

        super.renderNameTag(entity, displayName, matrixStack, buffer, packedLight);

        matrixStack.popPose();

        // Cancel the original method, we already rendered the name (this acts as a non-intrusive redirect)
        ci.cancel();
    }
}
