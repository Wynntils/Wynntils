/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public abstract class ItemStackLayerRenderStateMixin {
    @Shadow
    @Final
    private ItemStackRenderState field_55345;

    //    @Inject(
    //            method =
    //
    // "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
    //            at =
    //                    @At(
    //                            target =
    //
    // "Lnet/minecraft/client/renderer/block/model/ItemTransform;apply(ZLcom/mojang/blaze3d/vertex/PoseStack;)V",
    //                            value = "INVOKE"))
    //    private void onRenderItem(
    //            PoseStack poseStack,
    //            MultiBufferSource multiBufferSource,
    //            int packedLight,
    //            int packedOverlay,
    //            CallbackInfo ci) {
    //        if (field_55345.displayContext != ItemDisplayContext.GROUND) return;
    //
    //        if (field_55345 instanceof ItemStackRenderStateExtension extension) {
    //            if (extension.getItemStack() == null) return;
    //
    //            MixinHelper.post(new GroundItemEntityTransformEvent(poseStack, extension.getItemStack()));
    //        }
    //    }
}
