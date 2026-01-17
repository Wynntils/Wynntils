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
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public final class WynntilsCapeLayer extends WynntilsLayer {
    private final HumanoidModel<AvatarRenderState> model;
    private final EquipmentAssetManager equipmentAssets;

    public WynntilsCapeLayer(
            RenderLayerParent<AvatarRenderState, PlayerModel> renderLayerParent,
            EntityRendererProvider.Context renderProviderContext) {
        super(renderLayerParent);
        this.model = new PlayerCapeModel(renderProviderContext.getModelSet().bakeLayer(ModelLayers.PLAYER_CAPE));
        this.equipmentAssets = renderProviderContext.getEquipmentAssets();
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
        if (!Services.Cosmetics.shouldRenderCape(player, false)) return;

        Identifier texture = Services.Cosmetics.getCapeTexture(player);
        if (texture == null) return;

        if (!renderState.isInvisible && renderState.showCape) {
            poseStack.pushPose();
            if (this.hasLayer(renderState.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID)) {
                poseStack.translate(0.0F, -0.053125F, 0.06875F);
            }

            RenderTranslucentCheckEvent.Cape translucentCheckEvent =
                    new RenderTranslucentCheckEvent.Cape(false, renderState, 1.0f);
            MixinHelper.post(translucentCheckEvent);

            RenderType renderType = translucentCheckEvent.getTranslucence() == 1.0f
                    ? RenderTypes.entityCutout(texture)
                    : RenderTypes.entityTranslucent(texture);

            nodeCollector.submitModel(
                    this.model,
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

    private boolean hasLayer(ItemStack stack, EquipmentClientInfo.LayerType layer) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.assetId().isPresent()) {
            EquipmentClientInfo equipmentClientInfo =
                    this.equipmentAssets.get(equippable.assetId().get());
            return !equipmentClientInfo.getLayers(layer).isEmpty();
        } else {
            return false;
        }
    }
}
