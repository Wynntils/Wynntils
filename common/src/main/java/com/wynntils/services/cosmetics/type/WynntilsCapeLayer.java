/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.cosmetics.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.features.embellishments.WynntilsCosmeticsFeature;
import com.wynntils.mc.event.RenderTranslucentCheckEvent;
import com.wynntils.mc.extension.EntityRenderStateExtension;
import com.wynntils.utils.colors.CommonColors;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public final class WynntilsCapeLayer extends WynntilsLayer {
    private final HumanoidModel<PlayerRenderState> model;
    private final EquipmentAssetManager equipmentAssets;

    public WynntilsCapeLayer(
            RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent,
            EntityRendererProvider.Context renderProviderContext) {
        super(renderLayerParent);
        this.model = new PlayerCapeModel<>(renderProviderContext.getModelSet().bakeLayer(ModelLayers.PLAYER_CAPE));
        this.equipmentAssets = renderProviderContext.getEquipmentAssets();
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
        if (!Services.Cosmetics.shouldRenderCape(player, false)) return;

        ResourceLocation texture = Services.Cosmetics.getCapeTexture(player);
        if (texture == null) return;

        poseStack.pushPose();
        if (this.hasLayer(playerRenderState.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID)) {
            poseStack.translate(0.0F, -0.053125F, 0.06875F);
        }

        RenderTranslucentCheckEvent.Cape translucentCheckEvent =
                new RenderTranslucentCheckEvent.Cape(false, playerRenderState, 1.0f);
        MixinHelper.post(translucentCheckEvent);

        VertexConsumer vertexConsumer = buffer.getBuffer(
                translucentCheckEvent.getTranslucence() == 1.0f
                        ? RenderType.entityCutout(texture)
                        : RenderType.entityTranslucent(texture));
        this.getParentModel().copyPropertiesTo(this.model);
        this.model.setupAnim(playerRenderState);
        this.model.renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                CommonColors.WHITE
                        .withAlpha(translucentCheckEvent.getTranslucence())
                        .asInt());
        poseStack.popPose();
    }

    private boolean hasLayer(ItemStack itemStack, EquipmentClientInfo.LayerType layerType) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.assetId().isPresent()) {
            EquipmentClientInfo equipmentClientInfo =
                    this.equipmentAssets.get(equippable.assetId().get());
            return !equipmentClientInfo.getLayers(layerType).isEmpty();
        } else {
            return false;
        }
    }
}
