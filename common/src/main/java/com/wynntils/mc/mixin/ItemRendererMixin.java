/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.mc.EventFactory;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemCache;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Inject(
            method = "render",
            at =
                    @At(
                            target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
                            shift = At.Shift.BEFORE,
                            value = "INVOKE"))
    private void onRenderItem(
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

    @ModifyVariable(
            method =
                    "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private String renderGuiItemDecorations(
            String text, Font font, ItemStack itemStack, int xPosition, int yPosition, String ignored) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return text;

        WynnItem wynnItem = wynnItemOpt.get();
        Boolean hideCount = wynnItem.getCache().get(WynnItemCache.HIDE_COUNT_KEY);
        if (hideCount == null || !hideCount) return text;

        return "";
    }
}
