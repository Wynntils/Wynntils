/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.extension.PlayerModelExtension;
import net.minecraft.client.model.PlayerModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin implements PlayerModelExtension {
    @Unique
    private float wynntilsTranslucence;

    //    @WrapOperation(
    //            method =
    // "renderCloak(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V",
    //            at =
    //                    @At(
    //                            value = "INVOKE",
    //                            target =
    //
    // "net/minecraft/client/model/geom/ModelPart.render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    //    private void renderCloakWithTransparency(
    //            ModelPart part,
    //            PoseStack poseStack,
    //            VertexConsumer buffer,
    //            int packedLight,
    //            int packedOverlay,
    //            Operation<Void> original) {
    //        // Add 'wynntilsTranslucence' into cloak model, original only call render() without 'color' argument.
    //        part.render(
    //                poseStack,
    //                buffer,
    //                packedLight,
    //                packedOverlay,
    //                CommonColors.WHITE.withAlpha(this.wynntilsTranslucence).asInt());
    //    }

    @Override
    public void setTranslucenceCape(float translucence) {
        this.wynntilsTranslucence = translucence;
    }
}
