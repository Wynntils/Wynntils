/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.players;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.PlayerRenderLayerEvent;
import com.wynntils.mc.event.RenderLayerRegistrationEvent;
import com.wynntils.models.players.cosmetics.CosmeticInfo;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.PLAYERS)
public class WynntilsCosmeticsFeature extends UserFeature {
    @Config
    public boolean renderOwnCape = true;

    @SubscribeEvent
    public void onLayerRegisteration(RenderLayerRegistrationEvent event) {
        event.registerLayer(new WynntilsCapeLayer(event.getPlayerRenderer(), this));
        event.registerLayer(new WynntilsElytraLayer(
                event.getPlayerRenderer(), event.getContext().getModelSet(), this));
    }

    @SubscribeEvent
    public void onCapeRender(PlayerRenderLayerEvent.Cape event) {
        if (shouldRenderCosmetic(event.getPlayer(), Type.CAPE)) {
            // Cancel default cape rendering, so ours doesn't cause a double up of capes
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onElytraRender(PlayerRenderLayerEvent.Elytra event) {
        if (shouldRenderCosmetic(event.getPlayer(), Type.ELYTRA)) {
            // This might not be necessary?
            event.setCanceled(true);
        }
    }

    private boolean shouldRenderCosmetic(Player player, Type type) {
        if (!Managers.Connection.onServer()) return false;
        if (player.isInvisible() || !player.isModelPartShown(PlayerModelPart.CAPE)) return false;
        if (McUtils.player().is(player) && !renderOwnCape) return false;
        if (Models.Player.getUser(player.getUUID()) == null
                || Models.Player.getUserCosmeticTexture(player.getUUID()) == null) return false;

        CosmeticInfo cosmetics = Models.Player.getUser(player.getUUID()).cosmetics();
        return (type == Type.CAPE ? cosmetics.hasCape() : cosmetics.hasElytra());
    }

    private ResourceLocation getTexture(Player player) {
        ResourceLocation[] textures = Models.Player.getUserCosmeticTexture(player.getUUID());
        if (textures == null) return null;

        int frames = textures.length;
        if (frames == 1) return textures[0];

        // This makes animated capes cycle through all their frames in 2 seconds,
        // regardless of how many frames of animation are present. Could be changed to an account setting?
        double percentage = ((System.currentTimeMillis() % 2000) / 2000d);
        int currentFrame = (int) (frames * percentage);
        return textures[currentFrame];
    }

    private static final class WynntilsCapeLayer
            extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        private final WynntilsCosmeticsFeature parent;

        private WynntilsCapeLayer(
                RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent,
                WynntilsCosmeticsFeature parent) {
            super(renderLayerParent);
            this.parent = parent;
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
            if (!parent.shouldRenderCosmetic(player, Type.CAPE)) return;

            ResourceLocation texture = parent.getTexture(player);
            if (texture == null) return;

            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 0.125f);
            double d = Mth.lerp(partialTick, player.xCloakO, player.xCloak)
                    - Mth.lerp(partialTick, player.xo, player.getX());
            double e = Mth.lerp(partialTick, player.yCloakO, player.yCloak)
                    - Mth.lerp(partialTick, player.yo, player.getY());
            double f = Mth.lerp(partialTick, player.zCloakO, player.zCloak)
                    - Mth.lerp(partialTick, player.zo, player.getZ());
            float g = player.yBodyRotO + (player.yBodyRot - player.yBodyRotO);
            double h = Mth.sin(g * ((float) Math.PI / 180));
            double i = -Mth.cos(g * ((float) Math.PI / 180));
            float j = (float) e * 10.0f;
            j = Mth.clamp(j, -6.0f, 32.0f);
            float k = (float) (d * h + f * i) * 100.0f;
            k = Mth.clamp(k, 0.0f, 150.0f);
            float l = (float) (d * i - f * h) * 100.0f;
            l = Mth.clamp(l, -20.0f, 20.0f);
            if (k < 0.0f) {
                k = 0.0f;
            }
            float m = Mth.lerp(partialTick, player.oBob, player.bob);
            j += Mth.sin(Mth.lerp(partialTick, player.walkDistO, player.walkDist) * 6.0f) * 32.0f * m;
            if (player.isCrouching()) {
                j += 25.0f;
            }
            poseStack.mulPose(Axis.XP.rotationDegrees(6.0f + k / 2.0f + j));
            poseStack.mulPose(Axis.ZP.rotationDegrees(l / 2.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - l / 2.0f));

            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entitySolid(texture));
            this.getParentModel().renderCloak(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

    private static final class WynntilsElytraLayer
            extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        private final WynntilsCosmeticsFeature parent;
        private final ElytraModel<AbstractClientPlayer> elytraModel;

        private WynntilsElytraLayer(
                RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent,
                EntityModelSet entityModelSet,
                WynntilsCosmeticsFeature parent) {
            super(renderLayerParent);
            this.elytraModel = new ElytraModel(entityModelSet.bakeLayer(ModelLayers.ELYTRA));
            this.parent = parent;
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
            if (!parent.shouldRenderCosmetic(player, Type.ELYTRA)) return;

            ResourceLocation texture = parent.getTexture(player);
            if (texture == null) return;

            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 0.125F);
            this.getParentModel().copyPropertiesTo(this.elytraModel);
            this.elytraModel.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer vertexConsumer =
                    ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(texture), false, false);
            this.elytraModel.renderToBuffer(
                    poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }
    }
}
