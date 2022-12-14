/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.fabric.mixins;

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

// This mixin is duplicated in the Fabric and Quilt side, because there has to be a special Forge version.
// If you modify this, then you should modify the other one too.
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(
            method = "render",
            at =
                    @At(
                            target =
                                    "Lnet/minecraft/client/renderer/block/model/ItemTransform;apply(ZLcom/mojang/blaze3d/vertex/PoseStack;)V",
                            shift = At.Shift.AFTER,
                            value = "INVOKE"))
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
