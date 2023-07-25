/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.overlays.Overlay;
import com.wynntils.core.consumers.features.overlays.OverlayPosition;
import com.wynntils.core.consumers.features.overlays.OverlaySize;
import com.wynntils.core.consumers.features.overlays.RenderState;
import com.wynntils.core.consumers.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.event.BossBarAddedEvent;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.abilities.bossbars.AwakenedBar;
import com.wynntils.models.abilities.bossbars.BloodPoolBar;
import com.wynntils.models.abilities.bossbars.CorruptedBar;
import com.wynntils.models.abilities.bossbars.FocusBar;
import com.wynntils.models.abilities.bossbars.ManaBankBar;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HealthTexture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.ManaTexture;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class GameBarsOverlayFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.HEALTH_BAR, renderAt = RenderState.REPLACE)
    private final HealthBarOverlay healthBarOverlay = new HealthBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final BloodPoolBarOverlay bloodPoolBarOverlay = new BloodPoolBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.FOOD_BAR, renderAt = RenderState.REPLACE)
    private final ManaBarOverlay manaBarOverlay = new ManaBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final ManaBankBarOverlay manaBankBarOverlay = new ManaBankBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final FocusBarOverlay focusBarOverlay = new FocusBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final AwakenedProgressBarOverlay awakenedProgressBarOverlay = new AwakenedProgressBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final CorruptedBarOverlay corruptedBarOverlay = new CorruptedBarOverlay();

    private final Map<Class<? extends TrackedBar>, BaseBarOverlay> barToOverlayMap = Map.of(
            BloodPoolBar.class,
            bloodPoolBarOverlay,
            ManaBankBar.class,
            manaBankBarOverlay,
            AwakenedBar.class,
            awakenedProgressBarOverlay,
            FocusBar.class,
            focusBarOverlay,
            CorruptedBar.class,
            corruptedBarOverlay);

    @SubscribeEvent
    public void onBossBarAdd(BossBarAddedEvent event) {
        BaseBarOverlay overlay = getOverlayFromTrackedBar(event.getTrackedBar());
        if (overlay == null) return;

        if (!overlay.shouldDisplayOriginal.get()) {
            event.setCanceled(true);
        }
    }

    private BaseBarOverlay getOverlayFromTrackedBar(TrackedBar trackedBar) {
        return barToOverlayMap.get(trackedBar.getClass());
    }

    public abstract static class BaseBarOverlay extends Overlay {
        @RegisterConfig("feature.wynntils.gameBarsOverlay.overlay.baseBar.textShadow")
        public final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

        @RegisterConfig("feature.wynntils.gameBarsOverlay.overlay.baseBar.flip")
        public final Config<Boolean> flip = new Config<>(false);

        @RegisterConfig("feature.wynntils.gameBarsOverlay.overlay.baseBar.animationTime")
        public final Config<Float> animationTime = new Config<>(2f);

        @RegisterConfig("feature.wynntils.gameBarsOverlay.overlay.baseBar.shouldDisplayOriginal")
        public final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

        // hacky override of custom color
        @RegisterConfig("feature.wynntils.gameBarsOverlay.overlay.baseBar.textColor")
        public final Config<CustomColor> textColor = new Config<>(CommonColors.WHITE);

        protected float currentProgress = 0f;

        protected BaseBarOverlay(OverlayPosition position, OverlaySize size, CustomColor textColor) {
            super(position, size);
            this.textColor.updateConfig(textColor);
        }

        protected float textureHeight() {
            return Texture.UNIVERSAL_BAR.height() / 2f;
        }

        protected abstract BossBarProgress progress();

        protected abstract String icon();

        protected abstract boolean isActive();

        @SubscribeEvent
        public void onTick(TickEvent event) {
            if (!Models.WorldState.onWorld() || !isActive()) return;

            if (animationTime.get() == 0) {
                currentProgress = progress().progress();
                return;
            }

            currentProgress -=
                    (animationTime.get() * 0.1f) * (currentProgress - progress().progress());
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
            if (!Models.WorldState.onWorld() || !isActive()) return;

            float barHeight = textureHeight() * (this.getWidth() / 81);
            float renderY = getModifiedRenderY(barHeight + 10);

            BossBarProgress barProgress = progress();

            String text = String.format(
                    "%s %s %s",
                    barProgress.value().current(), icon(), barProgress.value().max());
            renderText(poseStack, bufferSource, renderY, text);

            float renderedProgress = Math.round((flip.get() ? -1 : 1) * currentProgress * 100) / 100f;
            renderBar(poseStack, bufferSource, renderY + 10, barHeight, renderedProgress);
        }

        protected float getModifiedRenderY(float renderedHeight) {
            return switch (this.getRenderVerticalAlignment()) {
                case TOP -> this.getRenderY();
                case MIDDLE -> this.getRenderY() + (this.getHeight() - renderedHeight) / 2;
                case BOTTOM -> this.getRenderY() + this.getHeight() - renderedHeight;
            };
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}

        protected void renderBar(
                PoseStack poseStack,
                MultiBufferSource bufferSource,
                float renderY,
                float renderHeight,
                float progress) {
            Texture universalBarTexture = Texture.UNIVERSAL_BAR;

            BufferedRenderUtils.drawColoredProgressBar(
                    poseStack,
                    bufferSource,
                    universalBarTexture,
                    this.textColor.get(),
                    this.getRenderX(),
                    renderY,
                    this.getRenderX() + this.getWidth(),
                    renderY + renderHeight,
                    0,
                    0,
                    universalBarTexture.width(),
                    universalBarTexture.height(),
                    progress);
        }

        protected void renderText(PoseStack poseStack, MultiBufferSource bufferSource, float renderY, String text) {
            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            bufferSource,
                            StyledText.fromString(text),
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            renderY,
                            0,
                            this.textColor.get(),
                            this.getRenderHorizontalAlignment(),
                            this.textShadow.get());
        }
    }

    protected abstract static class OverflowableBarOverlay extends BaseBarOverlay {
        protected OverflowableBarOverlay(OverlayPosition position, OverlaySize size, CustomColor textColor) {
            super(position, size, textColor);
        }

        @Override
        protected void renderBar(
                PoseStack poseStack,
                MultiBufferSource bufferSource,
                float renderY,
                float renderHeight,
                float progress) {
            int textureY1 = getTextureY1();
            int textureY2 = getTextureY2();

            Texture texture = getTexture();

            // Handle overflow
            if (Math.abs(progress) > 1) {
                Texture overflowTexture = getOverflowTexture();

                float x1 = this.getRenderX();
                float x2 = this.getRenderX() + this.getWidth();

                int half = (textureY1 + textureY2) / 2 + (textureY2 - textureY1) % 2;
                BufferedRenderUtils.drawProgressBarBackground(
                        poseStack,
                        bufferSource,
                        texture,
                        x1,
                        renderY,
                        x2,
                        renderY + renderHeight,
                        0,
                        textureY1,
                        81,
                        half);
                BufferedRenderUtils.drawProgressBarForeground(
                        poseStack,
                        bufferSource,
                        texture,
                        x1,
                        renderY,
                        x2,
                        renderY + renderHeight,
                        0,
                        half,
                        81,
                        textureY2 + (textureY2 - textureY1) % 2,
                        1f);

                float overflowProgress = progress < 0 ? progress + 1 : progress - 1;
                BufferedRenderUtils.drawProgressBarForeground(
                        poseStack,
                        bufferSource,
                        overflowTexture,
                        x1,
                        renderY,
                        x2,
                        renderY + renderHeight,
                        0,
                        half,
                        81,
                        textureY2 + (textureY2 - textureY1) % 2,
                        overflowProgress);

                return;
            }

            BufferedRenderUtils.drawProgressBar(
                    poseStack,
                    bufferSource,
                    texture,
                    this.getRenderX(),
                    renderY,
                    this.getRenderX() + this.getWidth(),
                    renderY + renderHeight,
                    0,
                    textureY1,
                    81,
                    textureY2,
                    progress);
        }

        protected abstract Texture getTexture();

        protected abstract Texture getOverflowTexture();

        protected abstract int getTextureY1();

        protected abstract int getTextureY2();
    }

    protected static class HealthBarOverlay extends OverflowableBarOverlay {
        @RegisterConfig("overlay.wynntils.healthBar.healthTexture")
        public final Config<HealthTexture> healthTexture = new Config<>(HealthTexture.A);

        protected HealthBarOverlay() {
            this(
                    new OverlayPosition(
                            -29,
                            -52,
                            VerticalAlignment.BOTTOM,
                            HorizontalAlignment.CENTER,
                            OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                    new OverlaySize(81, 21));
        }

        protected HealthBarOverlay(OverlayPosition overlayPosition, OverlaySize overlaySize) {
            super(overlayPosition, overlaySize, CommonColors.RED);
        }

        @Override
        public float textureHeight() {
            return healthTexture.get().getHeight();
        }

        @Override
        public String icon() {
            return "❤";
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            Models.CharacterStats.hideHealth(!this.shouldDisplayOriginal.get());
        }

        @Override
        public BossBarProgress progress() {
            CappedValue health = Models.CharacterStats.getHealth();
            return new BossBarProgress(health, (float) health.getProgress());
        }

        @Override
        protected Texture getTexture() {
            return Texture.HEALTH_BAR;
        }

        @Override
        protected Texture getOverflowTexture() {
            return Texture.HEALTH_BAR_OVERFLOW;
        }

        @Override
        protected int getTextureY1() {
            return healthTexture.get().getTextureY1();
        }

        @Override
        protected int getTextureY2() {
            return healthTexture.get().getTextureY2();
        }
    }

    public static class BloodPoolBarOverlay extends HealthBarOverlay {
        protected BloodPoolBarOverlay() {
            super(
                    new OverlayPosition(
                            -30,
                            -150,
                            VerticalAlignment.BOTTOM,
                            HorizontalAlignment.CENTER,
                            OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                    new OverlaySize(81, 21));
        }

        @Override
        public String icon() {
            return "⚕";
        }

        @Override
        public BossBarProgress progress() {
            return Models.BossBar.bloodPoolBar.getBarProgress();
        }

        @Override
        public boolean isActive() {
            return Models.BossBar.bloodPoolBar.isActive();
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            // Do not call super
        }
    }

    protected static class ManaBarOverlay extends OverflowableBarOverlay {
        @RegisterConfig("overlay.wynntils.manaBar.manaTexture")
        public final Config<ManaTexture> manaTexture = new Config<>(ManaTexture.A);

        protected ManaBarOverlay() {
            this(
                    new OverlayPosition(
                            -29,
                            52,
                            VerticalAlignment.BOTTOM,
                            HorizontalAlignment.CENTER,
                            OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                    new OverlaySize(81, 21));
        }

        protected ManaBarOverlay(OverlayPosition overlayPosition, OverlaySize overlaySize) {
            super(overlayPosition, overlaySize, CommonColors.LIGHT_BLUE);
        }

        @Override
        public float textureHeight() {
            return manaTexture.get().getHeight();
        }

        @Override
        public BossBarProgress progress() {
            CappedValue mana = Models.CharacterStats.getMana();
            return new BossBarProgress(mana, (float) mana.getProgress());
        }

        @Override
        public String icon() {
            return "✺";
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            Models.CharacterStats.hideMana(!this.shouldDisplayOriginal.get());
        }

        @Override
        protected Texture getTexture() {
            return Texture.MANA_BAR;
        }

        @Override
        protected Texture getOverflowTexture() {
            return Texture.MANA_BAR_OVERFLOW;
        }

        @Override
        protected int getTextureY1() {
            return manaTexture.get().getTextureY1();
        }

        @Override
        protected int getTextureY2() {
            return manaTexture.get().getTextureY2();
        }
    }

    public static class ManaBankBarOverlay extends ManaBarOverlay {
        protected ManaBankBarOverlay() {
            super(
                    new OverlayPosition(
                            -30,
                            -150,
                            VerticalAlignment.BOTTOM,
                            HorizontalAlignment.CENTER,
                            OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                    new OverlaySize(81, 21));
        }

        @Override
        public String icon() {
            return "☄";
        }

        @Override
        public BossBarProgress progress() {
            return Models.BossBar.manaBankBar.getBarProgress();
        }

        @Override
        public boolean isActive() {
            return Models.BossBar.manaBankBar.isActive();
        }

        @Override
        protected void renderBar(
                PoseStack poseStack,
                MultiBufferSource bufferSource,
                float renderY,
                float renderHeight,
                float progress) {
            int textureY1 = getTextureY1();
            int textureY2 = getTextureY2();

            Texture texture = getTexture();

            float x1 = this.getRenderX();
            float x2 = this.getRenderX() + this.getWidth();

            int half = (textureY1 + textureY2) / 2 + (textureY2 - textureY1) % 2;
            BufferedRenderUtils.drawProgressBarBackground(
                    poseStack, bufferSource, texture, x1, renderY, x2, renderY + renderHeight, 0, textureY1, 81, half);
            if (progress == 1f) {
                BufferedRenderUtils.drawProgressBarForeground(
                        poseStack,
                        bufferSource,
                        getOverflowTexture(),
                        x1,
                        renderY,
                        x2,
                        renderY + renderHeight,
                        0,
                        half,
                        81,
                        textureY2 + (textureY2 - textureY1) % 2,
                        1f);
            } else {
                BufferedRenderUtils.drawProgressBarForeground(
                        poseStack,
                        bufferSource,
                        texture,
                        x1,
                        renderY,
                        x2,
                        renderY + renderHeight,
                        0,
                        half,
                        81,
                        textureY2 + (textureY2 - textureY1) % 2,
                        progress);
            }
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            // Do not call super
        }
    }

    protected static class AwakenedProgressBarOverlay extends BaseBarOverlay {
        protected AwakenedProgressBarOverlay() {
            super(
                    new OverlayPosition(
                            -70,
                            -150,
                            VerticalAlignment.BOTTOM,
                            HorizontalAlignment.CENTER,
                            OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                    new OverlaySize(81, 21),
                    CommonColors.WHITE);
        }

        @Override
        public BossBarProgress progress() {
            return Models.BossBar.awakenedBar.getBarProgress();
        }

        @Override
        public String icon() {
            return "۞";
        }

        @Override
        public boolean isActive() {
            return Models.BossBar.awakenedBar.isActive();
        }
    }

    protected static class FocusBarOverlay extends BaseBarOverlay {
        protected FocusBarOverlay() {
            super(
                    new OverlayPosition(
                            -30,
                            -150,
                            VerticalAlignment.BOTTOM,
                            HorizontalAlignment.CENTER,
                            OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                    new OverlaySize(81, 21),
                    CommonColors.YELLOW);
        }

        @Override
        public BossBarProgress progress() {
            return Models.BossBar.focusBar.getBarProgress();
        }

        @Override
        public String icon() {
            return "➶";
        }

        @Override
        public boolean isActive() {
            return Models.BossBar.focusBar.isActive();
        }
    }

    protected static class CorruptedBarOverlay extends BaseBarOverlay {
        protected CorruptedBarOverlay() {
            super(
                    new OverlayPosition(
                            -70,
                            -150,
                            VerticalAlignment.BOTTOM,
                            HorizontalAlignment.CENTER,
                            OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                    new OverlaySize(81, 21),
                    CommonColors.PURPLE);
        }

        @Override
        public BossBarProgress progress() {
            return Models.BossBar.corruptedBar.getBarProgress();
        }

        @Override
        public String icon() {
            return "☠";
        }

        @Override
        public boolean isActive() {
            return Models.BossBar.corruptedBar.isActive();
        }
    }
}
