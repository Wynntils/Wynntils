/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.BarOverlay;
import com.wynntils.core.features.overlays.OverlaySize;
import com.wynntils.core.features.overlays.RenderState;
import com.wynntils.core.features.overlays.annotations.OverlayGroup;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HealthTexture;
import com.wynntils.utils.render.type.ManaTexture;
import com.wynntils.utils.render.type.ObjectivesTextures;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;

@ConfigCategory(Category.OVERLAYS)
public class CustomBarsOverlayFeature extends Feature {
    @OverlayGroup(instances = 0, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.Pre)
    private final List<CustomUniversalBarOverlay> customUniversalBarOverlays = new ArrayList<>();

    @OverlayGroup(instances = 0, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.Pre)
    private final List<CustomHealthBarOverlay> customHealthBarOverlays = new ArrayList<>();

    @OverlayGroup(instances = 0, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.Pre)
    private final List<CustomManaBarOverlay> customManaBarOverlays = new ArrayList<>();

    @OverlayGroup(instances = 0, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.Pre)
    private final List<CustomExperienceBarOverlay> customExperienceBarOverlays = new ArrayList<>();

    @OverlayGroup(instances = 0, renderType = RenderEvent.ElementType.GUI, renderAt = RenderState.Pre)
    private final List<CustomBubbleBarOverlay> customBubbleBarOverlays = new ArrayList<>();

    protected static class CustomUniversalBarOverlay extends CustomBarOverlayBase {
        @RegisterConfig
        public final Config<CustomColor> color = new Config<>(CommonColors.WHITE);

        public CustomUniversalBarOverlay(int id) {
            super(id, new OverlaySize(81, 21));
        }

        @Override
        public CustomColor getRenderColor() {
            return color.get();
        }

        @Override
        public Texture getTexture() {
            return Texture.UNIVERSAL_BAR;
        }

        @Override
        protected float getTextureHeight() {
            return Texture.UNIVERSAL_BAR.height() / 2f;
        }

        @Override
        protected BarOverlayTemplatePair getActualPreviewTemplate() {
            return new BarOverlayTemplatePair("3/10", "capped(3; 10)");
        }
    }

    protected static class CustomHealthBarOverlay extends CustomBarOverlayBase {
        @RegisterConfig("overlay.healthBar.healthTexture")
        public final Config<HealthTexture> healthTexture = new Config<>(HealthTexture.a);

        public CustomHealthBarOverlay(int id) {
            super(id, new OverlaySize(81, 21));
        }

        @Override
        public Texture getTexture() {
            return Texture.HEALTH_BAR;
        }

        @Override
        public float getTextureHeight() {
            return healthTexture.get().getHeight();
        }

        @Override
        protected void renderBar(
                PoseStack poseStack,
                MultiBufferSource.BufferSource bufferSource,
                float renderY,
                float renderHeight,
                float progress) {
            BufferedRenderUtils.drawProgressBar(
                    poseStack,
                    bufferSource,
                    Texture.HEALTH_BAR,
                    getRenderX(),
                    renderY,
                    getRenderX() + getWidth(),
                    renderY + renderHeight,
                    0,
                    healthTexture.get().getTextureY1(),
                    81,
                    healthTexture.get().getTextureY2(),
                    progress);
        }

        @Override
        protected BarOverlayTemplatePair getActualPreviewTemplate() {
            return new BarOverlayTemplatePair("432/1500", "capped(432; 1500)");
        }
    }

    protected static class CustomManaBarOverlay extends CustomBarOverlayBase {
        @RegisterConfig("overlay.wynntils.manaBar.manaTexture")
        public final Config<ManaTexture> manaTexture = new Config<>(ManaTexture.a);

        public CustomManaBarOverlay(int id) {
            super(id, new OverlaySize(81, 21));
        }

        @Override
        public Texture getTexture() {
            return Texture.MANA_BAR;
        }

        @Override
        protected float getTextureHeight() {
            return manaTexture.get().getHeight();
        }

        @Override
        protected void renderBar(
                PoseStack poseStack,
                MultiBufferSource.BufferSource bufferSource,
                float renderY,
                float renderHeight,
                float progress) {
            BufferedRenderUtils.drawProgressBar(
                    poseStack,
                    bufferSource,
                    Texture.MANA_BAR,
                    getRenderX(),
                    renderY,
                    getRenderX() + getWidth(),
                    renderY + renderHeight,
                    0,
                    manaTexture.get().getTextureY1(),
                    81,
                    manaTexture.get().getTextureY2(),
                    progress);
        }

        @Override
        protected BarOverlayTemplatePair getActualPreviewTemplate() {
            return new BarOverlayTemplatePair("12/100", "capped(12; 100)");
        }
    }

    protected static class CustomExperienceBarOverlay extends CustomBarOverlayBase {
        @RegisterConfig("overlay.wynntils.objectivesTexture")
        public final Config<ObjectivesTextures> objectivesTexture = new Config<>(ObjectivesTextures.a);

        public CustomExperienceBarOverlay(int id) {
            super(id, new OverlaySize(150, 30));
        }

        @Override
        public Texture getTexture() {
            return Texture.EXPERIENCE_BAR;
        }

        @Override
        protected float getTextureHeight() {
            return 5;
        }

        @Override
        protected void renderBar(
                PoseStack poseStack,
                MultiBufferSource.BufferSource bufferSource,
                float renderY,
                float barHeight,
                float progress) {
            BufferedRenderUtils.drawProgressBar(
                    poseStack,
                    bufferSource,
                    getTexture(),
                    getRenderX(),
                    renderY,
                    getRenderX() + getWidth(),
                    renderY + barHeight,
                    0,
                    objectivesTexture.get().getTextureYOffset(),
                    182,
                    objectivesTexture.get().getTextureYOffset() + 10,
                    progress);
        }

        @Override
        protected BarOverlayTemplatePair getActualPreviewTemplate() {
            return new BarOverlayTemplatePair("{capped_xp}", "capped_xp");
        }
    }

    protected static class CustomBubbleBarOverlay extends CustomBarOverlayBase {
        @RegisterConfig("overlay.wynntils.objectivesTexture")
        public final Config<ObjectivesTextures> objectivesTexture = new Config<>(ObjectivesTextures.a);

        public CustomBubbleBarOverlay(int id) {
            super(id, new OverlaySize(150, 30));
        }

        @Override
        public Texture getTexture() {
            return Texture.BUBBLE_BAR;
        }

        @Override
        protected float getTextureHeight() {
            return 5;
        }

        @Override
        protected void renderBar(
                PoseStack poseStack,
                MultiBufferSource.BufferSource bufferSource,
                float renderY,
                float barHeight,
                float progress) {
            BufferedRenderUtils.drawProgressBar(
                    poseStack,
                    bufferSource,
                    getTexture(),
                    getRenderX(),
                    renderY,
                    getRenderX() + getWidth(),
                    renderY + barHeight,
                    0,
                    objectivesTexture.get().getTextureYOffset(),
                    182,
                    objectivesTexture.get().getTextureYOffset() + 10,
                    progress);
        }

        @Override
        protected BarOverlayTemplatePair getActualPreviewTemplate() {
            return new BarOverlayTemplatePair("{capped_ingredient_pouch_slots}", "capped_ingredient_pouch_slots");
        }
    }

    protected abstract static class CustomBarOverlayBase extends BarOverlay {
        @RegisterConfig("feature.wynntils.customBarsOverlay.overlay.customBarBase.textTemplate")
        public final Config<String> textTemplate = new Config<>("");

        @RegisterConfig("feature.wynntils.customBarsOverlay.overlay.customBarBase.valueTemplate")
        public final Config<String> valueTemplate = new Config<>("");

        @RegisterConfig("feature.wynntils.customBarsOverlay.overlay.customBarBase.enabledTemplate")
        public final Config<String> enabledTemplate = new Config<>("true");

        protected CustomBarOverlayBase(int id, OverlaySize overlaySize) {
            super(id, overlaySize);
        }

        @Override
        public BarOverlayTemplatePair getTemplate() {
            return new BarOverlayTemplatePair(textTemplate.get(), valueTemplate.get());
        }

        @Override
        public BarOverlayTemplatePair getPreviewTemplate() {
            if (!valueTemplate.get().isEmpty()) {
                return getTemplate();
            }

            return getActualPreviewTemplate();
        }

        @Override
        public boolean isRendered() {
            if (valueTemplate.get().isEmpty()) return false;

            ErrorOr<Boolean> enabledOrError =
                    Managers.Function.tryGetRawValueOfType(enabledTemplate.get(), Boolean.class);
            return !enabledOrError.hasError() && enabledOrError.getValue();
        }

        protected abstract BarOverlayTemplatePair getActualPreviewTemplate();
    }
}
