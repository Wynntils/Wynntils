/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.PlayerRenderLayerEvent;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends HumanoidRenderState, A extends HumanoidModel<T>> {
    @Unique
    private PlayerRenderState playerRenderState;

    @Inject(
            method =
                    "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
            at = @At("HEAD"))
    private void captureRenderState(
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int i,
            T humanoidRenderState,
            float f,
            float g,
            CallbackInfo ci) {
        this.playerRenderState =
                humanoidRenderState instanceof PlayerRenderState ? (PlayerRenderState) humanoidRenderState : null;
    }

    @Inject(
            method =
                    "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void renderArmorPiece(
            PoseStack poseStack,
            MultiBufferSource buffer,
            ItemStack armorItem,
            EquipmentSlot slot,
            int packedLight,
            A model,
            CallbackInfo ci) {
        if (playerRenderState == null) return;

        PlayerRenderLayerEvent.Armor event = new PlayerRenderLayerEvent.Armor(playerRenderState, slot);
        MixinHelper.post(event);
        if (event.isCanceled()) ci.cancel();
    }
}
