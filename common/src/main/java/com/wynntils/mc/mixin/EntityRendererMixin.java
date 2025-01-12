/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.EntityNameTagRenderEvent;
import com.wynntils.mc.extension.EntityRenderStateExtension;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    public abstract Font getFont();

    @Inject(
            method =
                    "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V",
            at = @At("RETURN"))
    private void onExtractRenderState(T entity, S entityRenderState, float f, CallbackInfo ci) {
        if (entityRenderState instanceof EntityRenderStateExtension) {
            ((EntityRenderStateExtension) entityRenderState).setEntity(entity);
        }
    }

    @ModifyArg(
            method =
                    "renderNameTag(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getBackgroundOpacity(F)F"))
    private float onNameTagRenderPre(
            float backgroundOpacity,
            @Local(ordinal = 0, argsOnly = true) S entityRenderState,
            @Local(ordinal = 0, argsOnly = true) Component displayName,
            @Local(ordinal = 0, argsOnly = true) PoseStack poseStack,
            @Local(ordinal = 0, argsOnly = true) MultiBufferSource bufferSource,
            @Local(ordinal = 0, argsOnly = true) int packedLight,
            @Share("cancelRender") LocalBooleanRef cancelRender) {
        EntityNameTagRenderEvent event = new EntityNameTagRenderEvent(
                entityRenderState,
                displayName,
                poseStack,
                bufferSource,
                packedLight,
                this.entityRenderDispatcher,
                this.getFont(),
                backgroundOpacity);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            cancelRender.set(true);
            return backgroundOpacity;
        }

        return event.getBackgroundOpacity();
    }

    @Inject(
            method =
                    "renderNameTag(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/Options;getBackgroundOpacity(F)F",
                            shift = At.Shift.AFTER),
            cancellable = true)
    private void onNameTagRenderPre(
            S entityRenderState,
            Component displayName,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci,
            @Share("cancelRender") LocalBooleanRef cancelRender) {
        if (cancelRender.get()) {
            // We inject in the middle of the method, to have locals,
            // but we need to manually pop the matrix stack now
            poseStack.popPose();
            ci.cancel();
        }
    }
}
