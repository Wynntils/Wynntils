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
import com.wynntils.core.config.ConfigInfo;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.RenderState;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.overlays.sizes.OverlaySize;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.event.BossBarAddedEvent;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.mc.event.RenderEvent;
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
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class CustomBarsOverlayFeature extends UserFeature {
    @SubscribeEvent
    public void onBossBarAdd(BossBarAddedEvent event) {
        BaseBarOverlay overlay = getOverlayFromTrackedBar(event.getTrackedBar());
        if (overlay == null) return;

        if (overlay.isEnabled() && !overlay.shouldDisplayOriginal.get()) {
            event.setCanceled(true);
        }
    }

    private BaseBarOverlay getOverlayFromTrackedBar(TrackedBar trackedBar) {
        return barToOverlayMap.get(trackedBar.getClass());
    }

    @OverlayInfo(renderType = RenderEvent.ElementType.HealthBar, renderAt = RenderState.Replace)
    private final HealthBarOverlay healthBarOverlay = new HealthBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final BloodPoolBarOverlay bloodPoolBarOverlay = new BloodPoolBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.FoodBar, renderAt = RenderState.Replace)
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

    public abstract static class BaseBarOverlay extends Overlay {
        @ConfigInfo(key = "feature.wynntils.customBarsOverlay.overlay.baseBar.textShadow")
        public final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

        @ConfigInfo(key = "feature.wynntils.customBarsOverlay.overlay.baseBar.flip")
        public final Config<Boolean> flip = new Config<>(false);

        @ConfigInfo(key = "feature.wynntils.customBarsOverlay.overlay.baseBar.shouldDisplayOriginal")
        public final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

        // hacky override of custom color
        @ConfigInfo(key = "feature.wynntils.customBarsOverlay.overlay.baseBar.textColor")
        public final Config<CustomColor> textColor = new Config<>(null);

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

        @Override
        public void render(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            if (!Models.WorldState.onWorld() || !isActive()) return;

            float barHeight = textureHeight() * (this.getWidth() / 81);
            float renderY = getModifiedRenderY(barHeight + 10);

            BossBarProgress barProgress = progress();

            String text = String.format(
                    "%s %s %s",
                    barProgress.value().current(), icon(), barProgress.value().max());
            renderText(poseStack, bufferSource, renderY, text);

            float progress = (flip.get() ? -1 : 1) * barProgress.progress();
            renderBar(poseStack, bufferSource, renderY + 10, barHeight, progress);
        }

        protected float getModifiedRenderY(float renderedHeight) {
            return switch (this.getRenderVerticalAlignment()) {
                case Top -> this.getRenderY();
                case Middle -> this.getRenderY() + (this.getHeight() - renderedHeight) / 2;
                case Bottom -> this.getRenderY() + this.getHeight() - renderedHeight;
            };
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}

        protected void renderBar(
                PoseStack poseStack,
                MultiBufferSource.BufferSource bufferSource,
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

        protected void renderText(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float renderY, String text) {
            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            bufferSource,
                            text,
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            renderY,
                            0,
                            this.textColor.get(),
                            this.getRenderHorizontalAlignment(),
                            this.textShadow.get());
        }
    }

    protected static class HealthBarOverlay extends BaseBarOverlay {
        @ConfigInfo(key = "feature.wynntils.customBarsOverlay.overlay.healthBar.healthTexture")
        public final Config<HealthTexture> healthTexture = new Config<>(HealthTexture.a);

        protected HealthBarOverlay() {
            this(
                    new OverlayPosition(
                            -30,
                            -52,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21));
        }

        protected HealthBarOverlay(OverlayPosition overlayPosition, GuiScaledOverlaySize guiScaledOverlaySize) {
            super(overlayPosition, guiScaledOverlaySize, CommonColors.RED);
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
            Models.Character.hideHealth(this.isEnabled() && !this.shouldDisplayOriginal.get());
        }

        @Override
        public BossBarProgress progress() {
            CappedValue health = Models.Character.getHealth();
            return new BossBarProgress(health, (float) health.getProgress());
        }

        @Override
        protected void renderBar(
                PoseStack poseStack,
                MultiBufferSource.BufferSource bufferSource,
                float renderY,
                float renderHeight,
                float progress) {
            if (progress > 1) { // overflowing health
                float x1 = this.getRenderX();
                float x2 = this.getRenderX() + this.getWidth();
                int textureY1 = healthTexture.get().getTextureY1();
                int textureY2 = healthTexture.get().getTextureY2();

                int half = (textureY1 + textureY2) / 2 + (textureY2 - textureY1) % 2;
                BufferedRenderUtils.drawProgressBarBackground(
                        poseStack,
                        bufferSource,
                        Texture.HEALTH_BAR,
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
                        Texture.HEALTH_BAR,
                        x1,
                        renderY,
                        x2,
                        renderY + renderHeight,
                        0,
                        half,
                        81,
                        textureY2 + (textureY2 - textureY1) % 2,
                        1f / progress);
                BufferedRenderUtils.drawProgressBarForeground(
                        poseStack,
                        bufferSource,
                        Texture.HEALTH_BAR_OVERFLOW,
                        x1,
                        renderY,
                        x2,
                        renderY + renderHeight,
                        0,
                        half,
                        81,
                        textureY2 + (textureY2 - textureY1) % 2,
                        1f / progress - 1);

                return;
            }

            BufferedRenderUtils.drawProgressBar(
                    poseStack,
                    bufferSource,
                    Texture.HEALTH_BAR,
                    this.getRenderX(),
                    renderY,
                    this.getRenderX() + this.getWidth(),
                    renderY + renderHeight,
                    0,
                    healthTexture.get().getTextureY1(),
                    81,
                    healthTexture.get().getTextureY2(),
                    progress);
        }
    }

    public static class BloodPoolBarOverlay extends HealthBarOverlay {
        protected BloodPoolBarOverlay() {
            super(
                    new OverlayPosition(
                            -30,
                            -150,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21));
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

    protected static class ManaBarOverlay extends BaseBarOverlay {
        @ConfigInfo(key = "feature.wynntils.customBarsOverlay.overlay.manaBar.manaTexture")
        public final Config<ManaTexture> manaTexture = new Config<>(ManaTexture.a);

        protected ManaBarOverlay() {
            this(
                    new OverlayPosition(
                            -30,
                            52,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21));
        }

        protected ManaBarOverlay(OverlayPosition overlayPosition, GuiScaledOverlaySize guiScaledOverlaySize) {
            super(overlayPosition, guiScaledOverlaySize, CommonColors.LIGHT_BLUE);
        }

        @Override
        public float textureHeight() {
            return manaTexture.get().getHeight();
        }

        @Override
        public BossBarProgress progress() {
            CappedValue mana = Models.Character.getMana();
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
            Models.Character.hideMana(this.isEnabled() && !this.shouldDisplayOriginal.get());
        }

        @Override
        protected void renderBar(
                PoseStack poseStack,
                MultiBufferSource.BufferSource bufferSource,
                float renderY,
                float renderHeight,
                float progress) {
            if (progress > 1) { // overflowing mana
                float x1 = this.getRenderX();
                float x2 = this.getRenderX() + this.getWidth();
                int textureY1 = manaTexture.get().getTextureY1();
                int textureY2 = manaTexture.get().getTextureY2();

                int half = (textureY1 + textureY2) / 2 + (textureY2 - textureY1) % 2;
                BufferedRenderUtils.drawProgressBarBackground(
                        poseStack,
                        bufferSource,
                        Texture.MANA_BAR,
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
                        Texture.MANA_BAR,
                        x1,
                        renderY,
                        x2,
                        renderY + renderHeight,
                        0,
                        half,
                        81,
                        textureY2 + (textureY2 - textureY1) % 2,
                        1f / progress);
                BufferedRenderUtils.drawProgressBarForeground(
                        poseStack,
                        bufferSource,
                        Texture.MANA_BAR_OVERFLOW,
                        x1,
                        renderY,
                        x2,
                        renderY + renderHeight,
                        0,
                        half,
                        81,
                        textureY2 + (textureY2 - textureY1) % 2,
                        1f / progress - 1);

                return;
            }

            BufferedRenderUtils.drawProgressBar(
                    poseStack,
                    bufferSource,
                    Texture.MANA_BAR,
                    this.getRenderX(),
                    renderY,
                    this.getRenderX() + this.getWidth(),
                    renderY + renderHeight,
                    0,
                    manaTexture.get().getTextureY1(),
                    81,
                    manaTexture.get().getTextureY2(),
                    progress);
        }
    }

    public static class ManaBankBarOverlay extends ManaBarOverlay {
        protected ManaBankBarOverlay() {
            super(
                    new OverlayPosition(
                            -30,
                            -150,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21));
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
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21),
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
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21),
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
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21),
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

    public enum ManaTexture {
        Wynn(0, 17, 8),
        Brune(83, 100, 8),
        Aether(116, 131, 7),
        Skull(143, 147, 8),
        Inverse(100, 115, 7),
        Skyrim(148, 163, 8),
        Rune(164, 179, 8),
        a(18, 33, 7),
        b(34, 51, 8),
        c(52, 67, 7),
        d(83, 100, 8);
        private final int textureY1, textureY2, height;

        ManaTexture(int textureY1, int textureY2, int height) {
            this.textureY1 = textureY1;
            this.textureY2 = textureY2;
            this.height = height;
        }

        public int getTextureY1() {
            return textureY1;
        }

        public int getTextureY2() {
            return textureY2;
        }

        public int getHeight() {
            return height;
        }
    }

    public enum HealthTexture {
        Wynn(0, 17, 8),
        Grune(84, 99, 7),
        Aether(100, 115, 7),
        Skull(116, 131, 8),
        Skyrim(132, 147, 8),
        Rune(148, 163, 8),
        a(18, 33, 7),
        b(34, 51, 8),
        c(52, 67, 7),
        d(68, 83, 7);
        private final int textureY1, textureY2, height;

        HealthTexture(int textureY1, int textureY2, int height) {
            this.textureY1 = textureY1;
            this.textureY2 = textureY2;
            this.height = height;
        }

        public int getTextureY1() {
            return textureY1;
        }

        public int getTextureY2() {
            return textureY2;
        }

        public int getHeight() {
            return height;
        }
    }
}
