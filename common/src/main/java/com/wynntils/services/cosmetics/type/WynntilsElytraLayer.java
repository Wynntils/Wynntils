/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.cosmetics.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.features.embellishments.WynntilsCosmeticsFeature;
import com.wynntils.mc.extension.EntityRenderStateExtension;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public final class WynntilsElytraLayer extends WynntilsLayer {
    private final ElytraModel elytraModel;

    public WynntilsElytraLayer(
            RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent,
            EntityRendererProvider.Context renderProviderContext) {
        super(renderLayerParent);
        this.elytraModel = new ElytraModel(renderProviderContext.getModelSet().bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            PlayerRenderState playerRenderState,
            float yRot,
            float xRot) {
        if (!Managers.Feature.getFeatureInstance(WynntilsCosmeticsFeature.class).isEnabled()) return;

        Entity entity = ((EntityRenderStateExtension) playerRenderState).getEntity();
        if (!(entity instanceof AbstractClientPlayer player)) return;
        if (!Services.Cosmetics.shouldRenderCape(player, true)) return;

        ResourceLocation texture = Services.Cosmetics.getCapeTexture(player);
        if (texture == null) return;

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        elytraModel.setupAnim(playerRenderState);
        VertexConsumer vertexConsumer =
                ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(texture), false);
        this.elytraModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
