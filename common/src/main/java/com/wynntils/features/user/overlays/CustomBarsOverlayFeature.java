/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.overlays.sizes.OverlaySize;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.wynn.event.ActionBarMessageUpdateEvent;
import com.wynntils.wynn.model.ActionBarModel;
import com.wynntils.wynn.utils.WynnBossBarUtils;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.OVERLAYS)
public class CustomBarsOverlayFeature extends UserFeature {

    public static CustomBarsOverlayFeature INSTANCE;

    @Config
    public boolean shouldDisplayOnActionBar = false;

    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        dependencies.add(ActionBarModel.class);
    }

    @SubscribeEvent
    public void onActionBarManaUpdate(ActionBarMessageUpdateEvent.ManaText event) {
        if (shouldDisplayOnActionBar || !manaBarOverlay.isEnabled()) return;

        event.setMessage("");
    }

    @SubscribeEvent
    public void onActionBarHealthUpdate(ActionBarMessageUpdateEvent.HealthText event) {
        if (shouldDisplayOnActionBar || !healthBarOverlay.isEnabled()) return;

        event.setMessage("");
    }

    @OverlayInfo(renderType = RenderEvent.ElementType.HealthBar, renderAt = OverlayInfo.RenderState.Replace)
    private final Overlay healthBarOverlay = new HealthBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay bloodPoolBarOverlay = new BloodPoolBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.FoodBar, renderAt = OverlayInfo.RenderState.Replace)
    private final Overlay manaBarOverlay = new ManaBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay manaBankBarOverlay = new ManaBankBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay focusBarOverlay = new FocusBarOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay awakenedProgressBarOverlay = new AwakenedProgressBarOverlay();

    public abstract static class BaseBarOverlay extends Overlay {
        @Config(key = "feature.wynntils.customBarsOverlay.overlay.baseBar.textShadow")
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config(key = "feature.wynntils.customBarsOverlay.overlay.baseBar.flip")
        public boolean flip = false;

        // hacky override of custom color
        @Config(key = "feature.wynntils.customBarsOverlay.overlay.baseBar.textColor")
        public CustomColor textColor = CustomColor.NONE;

        protected BaseBarOverlay(OverlayPosition position, OverlaySize size) {
            super(position, size);
        }

        public abstract float textureHeight();

        public abstract WynnBossBarUtils.BarProgress progress();

        public abstract String icon();

        public abstract WynnBossBarUtils.BarProgress noProgress();

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            float barHeight = textureHeight() * (this.getWidth() / 81);
            float renderY = getModifiedRenderY(barHeight + 10);

            WynnBossBarUtils.BarProgress barProgress = progress();

            if (barProgress.equals(noProgress())) return;

            String text = String.format("%s %s %s", barProgress.current(), icon(), barProgress.max());
            renderText(poseStack, renderY, text);

            float progress = (flip ? -1 : 1) * barProgress.progress();
            renderBar(poseStack, renderY + 10, barHeight, progress);
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

        protected abstract void renderBar(PoseStack poseStack, float renderY, float renderHeight, float progress);

        protected void renderText(PoseStack poseStack, float renderY, String text) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            text,
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            renderY,
                            0,
                            this.textColor,
                            this.getRenderHorizontalAlignment(),
                            this.textShadow);
        }
    }

    public static class HealthBarOverlay extends BaseBarOverlay {
        @Config(key = "feature.wynntils.customBarsOverlay.overlay.healthBar.healthTexture")
        public HealthTexture healthTexture = HealthTexture.a;

        public HealthBarOverlay() {
            this(
                    new OverlayPosition(
                            -30,
                            -52,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21));
        }

        public HealthBarOverlay(OverlayPosition overlayPosition, GuiScaledOverlaySize guiScaledOverlaySize) {
            super(overlayPosition, guiScaledOverlaySize);
            textColor = CommonColors.RED;
        }

        @Override
        public float textureHeight() {
            return healthTexture.getHeight();
        }

        @Override
        public String icon() {
            return "❤";
        }

        @Override
        public WynnBossBarUtils.BarProgress noProgress() {
            return null;
        }

        @Override
        public WynnBossBarUtils.BarProgress progress() {
            int current = ActionBarModel.getCurrentHealth();
            int max = ActionBarModel.getMaxHealth();
            return new WynnBossBarUtils.BarProgress(current, max, current / (float) max);
        }

        protected void renderBar(PoseStack poseStack, float renderY, float renderHeight, float progress) {
            RenderUtils.drawProgressBar(
                    poseStack,
                    Texture.HEALTH_BAR,
                    this.getRenderX(),
                    renderY,
                    this.getRenderX() + this.getWidth(),
                    renderY + renderHeight,
                    0,
                    healthTexture.getTextureY1(),
                    81,
                    healthTexture.getTextureY2(),
                    progress);
        }
    }

    public static class BloodPoolBarOverlay extends HealthBarOverlay {
        public BloodPoolBarOverlay() {
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
        public WynnBossBarUtils.BarProgress progress() {
            return WynnBossBarUtils.getBloodPool();
        }

        @Override
        public WynnBossBarUtils.BarProgress noProgress() {
            return WynnBossBarUtils.NO_BLOOD_POOL;
        }
    }

    public static class ManaBarOverlay extends BaseBarOverlay {
        @Config(key = "feature.wynntils.customBarsOverlay.overlay.manaBar.manaTexture")
        public ManaTexture manaTexture = ManaTexture.a;

        public ManaBarOverlay() {
            this(
                    new OverlayPosition(
                            -30,
                            52,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21));
        }

        public ManaBarOverlay(OverlayPosition overlayPosition, GuiScaledOverlaySize guiScaledOverlaySize) {
            super(overlayPosition, guiScaledOverlaySize);
            textColor = CommonColors.LIGHT_BLUE;
        }

        @Override
        public float textureHeight() {
            return manaTexture.getHeight();
        }

        @Override
        public WynnBossBarUtils.BarProgress progress() {
            int current = ActionBarModel.getCurrentMana();
            int max = ActionBarModel.getMaxMana();
            return new WynnBossBarUtils.BarProgress(current, max, current / (float) max);
        }

        @Override
        public String icon() {
            return "✺";
        }

        @Override
        public WynnBossBarUtils.BarProgress noProgress() {
            return null;
        }

        @Override
        protected void renderBar(PoseStack poseStack, float renderY, float renderHeight, float progress) {
            RenderUtils.drawProgressBar(
                    poseStack,
                    Texture.MANA_BAR,
                    this.getRenderX(),
                    renderY,
                    this.getRenderX() + this.getWidth(),
                    renderY + renderHeight,
                    0,
                    manaTexture.getTextureY1(),
                    81,
                    manaTexture.getTextureY2(),
                    progress);
        }
    }

    public static class ManaBankBarOverlay extends ManaBarOverlay {
        public ManaBankBarOverlay() {
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
        public WynnBossBarUtils.BarProgress progress() {
            return WynnBossBarUtils.getManaBank();
        }

        @Override
        public WynnBossBarUtils.BarProgress noProgress() {
            return WynnBossBarUtils.NO_MANA_BANK;
        }
    }

    public static class AwakenedProgressBarOverlay extends BaseBarOverlay {

        public AwakenedProgressBarOverlay() {
            super(
                    new OverlayPosition(
                            -70,
                            -150,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21));
            textColor = CommonColors.WHITE;
        }

        @Override
        public float textureHeight() {
            return Texture.UNIVERSAL_BAR.height() / 2f;
        }

        @Override
        public WynnBossBarUtils.BarProgress progress() {
            return WynnBossBarUtils.getAwakenedBar();
        }

        @Override
        public String icon() {
            return "۞";
        }

        @Override
        protected void renderBar(PoseStack poseStack, float renderY, float renderHeight, float progress) {
            Texture universalBarTexture = Texture.UNIVERSAL_BAR;
            RenderUtils.drawColoredProgressBar(
                    poseStack,
                    universalBarTexture,
                    textColor,
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

        @Override
        public WynnBossBarUtils.BarProgress noProgress() {
            return WynnBossBarUtils.NO_AWAKENED_PROGRESS;
        }
    }

    public static class FocusBarOverlay extends BaseBarOverlay {
        public FocusBarOverlay() {
            super(
                    new OverlayPosition(
                            -30,
                            -150,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21));
            textColor = CommonColors.YELLOW;
        }

        @Override
        public float textureHeight() {
            return Texture.UNIVERSAL_BAR.height() / 2f;
        }

        @Override
        public WynnBossBarUtils.BarProgress progress() {
            return WynnBossBarUtils.getFocusBar();
        }

        @Override
        public String icon() {
            return "➶";
        }

        @Override
        protected void renderBar(PoseStack poseStack, float renderY, float renderHeight, float progress) {
            Texture universalBarTexture = Texture.UNIVERSAL_BAR;

            RenderUtils.drawColoredProgressBar(
                    poseStack,
                    universalBarTexture,
                    this.textColor,
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

        @Override
        public WynnBossBarUtils.BarProgress noProgress() {
            return WynnBossBarUtils.NO_FOCUS;
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
}
