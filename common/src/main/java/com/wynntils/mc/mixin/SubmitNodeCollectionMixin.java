/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.NametagBackgroundOpacityEvent;
import com.wynntils.mc.event.NametagScaleEvent;
import net.minecraft.client.renderer.SubmitNodeCollection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin {
    @ModifyArg(
            method =
                    "submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/state/OptionsRenderState;getBackgroundOpacity(F)F"))
    private float onNametagOpacityGet(float backgroundOpacity) {
        NametagBackgroundOpacityEvent event = new NametagBackgroundOpacityEvent(backgroundOpacity);
        MixinHelper.post(event);

        return event.getOpacity();
    }

    @WrapOperation(
            method =
                    "submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void modifyNametagScale(PoseStack poseStack, float x, float y, float z, Operation<Void> original) {
        NametagScaleEvent event = new NametagScaleEvent();
        MixinHelper.post(event);

        original.call(poseStack, x * event.getScale(), y * event.getScale(), z * event.getScale());
    }
}
