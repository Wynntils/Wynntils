/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends HumanoidRenderState, A extends HumanoidModel<T>> {
    @Unique
    private AvatarRenderState avatarRenderState;

    //    @Inject(
    //            method =
    //
    // "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
    //            at = @At("HEAD"))
    //    private void captureRenderState(
    //            PoseStack poseStack,
    //            MultiBufferSource multiBufferSource,
    //            int i,
    //            T humanoidRenderState,
    //            float f,
    //            float g,
    //            CallbackInfo ci) {
    //        this.avatarRenderState =
    //                humanoidRenderState instanceof AvatarRenderState ? (AvatarRenderState) humanoidRenderState : null;
    //    }

    //    @Inject(
    //            method =
    //
    // "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
    //            at = @At("HEAD"),
    //            cancellable = true)
    //    private void renderArmorPiece(
    //            PoseStack poseStack,
    //            MultiBufferSource buffer,
    //            ItemStack armorItem,
    //            EquipmentSlot slot,
    //            int packedLight,
    //            A model,
    //            CallbackInfo ci) {
    //        if (avatarRenderState == null) return;
    //
    //        PlayerRenderLayerEvent.Armor event = new PlayerRenderLayerEvent.Armor(avatarRenderState, slot);
    //        MixinHelper.post(event);
    //        if (event.isCanceled()) ci.cancel();
    //    }
}
