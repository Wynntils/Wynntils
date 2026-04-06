/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.cosmetics.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.features.embellishments.WynntilsCosmeticsFeature;
import com.wynntils.mc.event.RenderTranslucentCheckEvent;
import com.wynntils.mc.extension.EntityRenderStateExtension;
import com.wynntils.utils.colors.CommonColors;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public final class WynntilsElytraLayer extends WynntilsLayer {
    private final ElytraModel elytraModel;

    public WynntilsElytraLayer(
            RenderLayerParent<AvatarRenderState, PlayerModel> renderLayerParent,
            EntityRendererProvider.Context renderProviderContext) {
        super(renderLayerParent);
        this.elytraModel = new ElytraModel(renderProviderContext.getModelSet().bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int packedLight,
            AvatarRenderState renderState,
            float yRot,
            float xRot) {
        if (!Managers.Feature.getFeatureInstance(WynntilsCosmeticsFeature.class).isEnabled()) return;

        Entity entity = ((EntityRenderStateExtension) renderState).getEntity();
        if (!(entity instanceof AbstractClientPlayer player)) return;
        if (!Services.Cosmetics.shouldRenderCape(player, true)) return;

        Identifier texture = Services.Cosmetics.getCapeTexture(player);
        if (texture == null) return;

        RenderTranslucentCheckEvent.Cape translucentCheckEvent =
                new RenderTranslucentCheckEvent.Cape(false, renderState, 1.0f);
        MixinHelper.post(translucentCheckEvent);

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);

        RenderType renderType = translucentCheckEvent.getTranslucence() == 1.0f
                ? RenderTypes.armorCutoutNoCull(texture)
                : RenderTypes.armorTranslucent(texture);

        nodeCollector.submitModel(
                elytraModel,
                renderState,
                poseStack,
                renderType,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                CommonColors.WHITE
                        .withAlpha(translucentCheckEvent.getTranslucence())
                        .asInt(),
                null,
                renderState.outlineColor,
                null);

        poseStack.popPose();
    }
}
