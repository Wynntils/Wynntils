/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.cosmetics.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.features.embellishments.WynntilsCosmeticsFeature;
import com.wynntils.mc.extension.EntityRenderStateExtension;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public final class WynntilsElytraLayer extends WynntilsLayer {
    private final ElytraModel elytraModel;
    private final EquipmentLayerRenderer equipmentRenderer;

    public WynntilsElytraLayer(
            RenderLayerParent<AvatarRenderState, PlayerModel> renderLayerParent,
            EntityRendererProvider.Context renderProviderContext) {
        super(renderLayerParent);
        this.elytraModel = new ElytraModel(renderProviderContext.getModelSet().bakeLayer(ModelLayers.ELYTRA));
        this.equipmentRenderer = renderProviderContext.getEquipmentRenderer();
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

        ResourceLocation texture = Services.Cosmetics.getCapeTexture(player);
        if (texture == null) return;

        // FIXME: Always render the elytra
        ItemStack itemStack = renderState.chestEquipment;
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.assetId().isPresent()) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 0.125F);
            this.equipmentRenderer.renderLayers(
                    EquipmentClientInfo.LayerType.WINGS,
                    equippable.assetId().get(),
                    elytraModel,
                    renderState,
                    itemStack,
                    poseStack,
                    nodeCollector,
                    packedLight,
                    texture,
                    renderState.outlineColor,
                    0);
            poseStack.popPose();
        }
    }
}
