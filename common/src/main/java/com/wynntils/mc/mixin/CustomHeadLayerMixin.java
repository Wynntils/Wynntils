/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin<T extends LivingEntityRenderState> {
    //    @Inject(
    //            method =
    //
    // "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V",
    //            at = @At("HEAD"),
    //            cancellable = true)
    //    private void render(
    //            PoseStack matrixStack,
    //            MultiBufferSource buffer,
    //            int packedLight,
    //            T renderState,
    //            float yRot,
    //            float xRot,
    //            CallbackInfo ci) {
    //        if (!(renderState instanceof AvatarRenderState avatarRenderState)) return;
    //
    //        PlayerRenderLayerEvent.Armor event = new PlayerRenderLayerEvent.Armor(avatarRenderState,
    // EquipmentSlot.HEAD);
    //        MixinHelper.post(event);
    //        if (event.isCanceled()) {
    //            ci.cancel();
    //        }
    //    }
}
