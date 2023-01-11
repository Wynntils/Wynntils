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
    private void onNameTagRenderPre(
            AbstractClientPlayer entity,
            Component displayName,
            PoseStack matrixStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci) {

        NametagRenderEvent event = EventFactory.onNameTagRender(entity, displayName, matrixStack, buffer, packedLight);
        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        List<MutableComponent> injected = event.getInjectedLines();
        if (injected.isEmpty()) return; // don't need to interfere with vanilla rendering

        // pose is always translated by ((entityHeight + 0.5) * scale) in super.renderNameTag
        // final offset is (entityHeight + (scale*0.25) + 0.25), so that nametag always starts 0.25 units above player
        float scale = event.getInjectedLinesScale();
        float scaleOffset = (entity.getBbHeight()) * (1 - scale) - (0.25f) * (scale) + 0.25f;

        matrixStack.pushPose();
        matrixStack.translate(0f, scaleOffset, 0f);
        matrixStack.scale(scale, scale, scale);

        for (MutableComponent component : injected) {
            // Note that the super qualifier is really needed, since this code is actually executing
            // in this.renderNameTag
            super.renderNameTag(entity, component, matrixStack, buffer, packedLight);
            matrixStack.translate(0.0, 0.25875f, 0.0);
        }

        // reset scale and undo the translation we added to account for the modified scale
        matrixStack.scale(1f / scale, 1f / scale, 1f / scale);
        matrixStack.translate(0f, -1 * scaleOffset, 0f);

        // finally, draw vanilla nametag on top
        super.renderNameTag(entity, displayName, matrixStack, buffer, packedLight);
        matrixStack.popPose();

        // Cancel the original method, we already rendered the name (this acts as a non-intrusive redirect)
        ci.cancel();
    }
}
