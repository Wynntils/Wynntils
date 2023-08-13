/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.cosmetics.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.features.embellishments.WynntilsCosmeticsFeature;
import com.wynntils.utils.MathUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class WynntilsCapeLayer extends WynntilsLayer {
    public WynntilsCapeLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        if (!Managers.Feature.getFeatureInstance(WynntilsCosmeticsFeature.class).isEnabled()) return;
        if (!Services.Cosmetics.shouldRenderCape(player, false)) return;

        ResourceLocation texture = Services.Cosmetics.getCapeTexture(player);
        if (texture == null) return;

        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, 0.125f);
        double xOffset = MathUtils.lerp(player.xCloakO, player.xCloak, partialTick)
                - MathUtils.lerp(player.xo, player.getX(), partialTick);
        double yOffset = MathUtils.lerp(player.yCloakO, player.yCloak, partialTick)
                - MathUtils.lerp(player.yo, player.getY(), partialTick);
        double zOffset = MathUtils.lerp(player.zCloakO, player.zCloak, partialTick)
                - MathUtils.lerp(player.zo, player.getZ(), partialTick);

        float rotation = player.yBodyRotO + (player.yBodyRot - player.yBodyRotO);
        float rotRadians = rotation / 360 * Mth.TWO_PI;
        double rotationSin = Mth.sin(rotRadians);
        double rotationCos = -Mth.cos(rotRadians);

        float capeX = (float) (xOffset * rotationSin + zOffset * rotationCos) * 100.0f;
        capeX = MathUtils.clamp(capeX, 0.0f, 150.0f);
        if (capeX < 0.0f) {
            capeX = 0.0f;
        }

        float capeY = MathUtils.clamp((float) yOffset * 10f, -6.0f, 32.0f);
        float bobOffset = MathUtils.lerp(player.oBob, player.bob, partialTick);
        float dist = MathUtils.lerp(player.walkDistO, player.walkDist, partialTick);
        capeY += Mth.sin(dist * 6.0f) * 32.0f * bobOffset;
        if (player.isCrouching()) {
            capeY += 25.0f;
        }

        float capeZ = (float) (xOffset * rotationCos - zOffset * rotationSin) * 100.0f;
        capeZ = MathUtils.clamp(capeZ, -20.0f, 20.0f);

        poseStack.mulPose(Axis.XP.rotationDegrees(6.0f + capeX / 2.0f + capeY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(capeZ / 2.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - capeZ / 2.0f));

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entitySolid(texture));
        this.getParentModel().renderCloak(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
