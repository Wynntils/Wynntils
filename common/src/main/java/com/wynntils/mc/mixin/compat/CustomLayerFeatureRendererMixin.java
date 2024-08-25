/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin.compat;

import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.core.components.Models;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.LivingEntityRenderTranslucentCheckEvent;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "dev.tr7zw.skinlayers.renderlayers.CustomLayerFeatureRenderer")
@Pseudo
public class CustomLayerFeatureRendererMixin {
    @ModifyArg(
            method = "renderLayers",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "dev/tr7zw/skinlayers/api/Mesh.render(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"),
            index = 5)
    private int setTranslucentFor3DSkinLayer(
            int original, @Local(argsOnly = true) AbstractClientPlayer abstractClientPlayer) {
        boolean isGhostPlayer = Models.Player.isPlayerGhost(abstractClientPlayer);
        LivingEntityRenderTranslucentCheckEvent event = new LivingEntityRenderTranslucentCheckEvent(
                isGhostPlayer, abstractClientPlayer, isGhostPlayer ? 0.15f : 1f);
        MixinHelper.post(event);
        return CustomColor.fromInt(original).withAlpha(event.getTranslucence()).asInt();
    }
}
