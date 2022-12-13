/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.forge.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.EventFactory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// This mixin is a special version for forge, as opposed to the ones for Fabric and Quilt, which are the same.
// If you modify this, then you might have to modify the Fabric/Quilt ones too.
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(
            method = "render",
            at =
                    @At(
                            target =
                                    "Lnet/minecraftforge/client/ForgeHooksClient;handleCameraTransforms(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;Z)Lnet/minecraft/client/resources/model/BakedModel;",
                            shift = At.Shift.AFTER,
                            value = "INVOKE",
                            remap = false))
    public void onRenderItem(
            ItemStack itemStack,
            ItemTransforms.TransformType transformType,
            boolean leftHand,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            int combinedOverlay,
            BakedModel model,
            CallbackInfo ci) {
        if (transformType == ItemTransforms.TransformType.GROUND) EventFactory.onGroundItemRender(poseStack, itemStack);
    }
}
