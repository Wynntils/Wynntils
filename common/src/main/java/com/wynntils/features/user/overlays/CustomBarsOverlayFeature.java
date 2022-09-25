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
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.CustomBarAddEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.event.ActionBarMessageUpdateEvent;
import com.wynntils.wynn.model.ActionBarModel;
import com.wynntils.wynn.model.bossbar.BossBarModel;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.OVERLAYS)
public class CustomBarsOverlayFeature extends UserFeature {

    @Config
    public boolean shouldDisplayOnActionBar = false;

    @Config
    public boolean shouldDisplayOnBossBar = false;

    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        dependencies.add(ActionBarModel.class);
        dependencies.add(BossBarModel.class);
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

    @SubscribeEvent
    public void onBossBarAdd(CustomBarAddEvent event) {
        Overlay overlay =
                switch (event.getType()) {
                    case BLOODPOOL -> bloodPoolBarOverlay;
                    case MANABANK -> manaBarOverlay;
                    case AWAKENED -> awakenedProgressBarOverlay;
                    case FOCUS -> focusBarOverlay;
                    case CORRUPTED -> corruptedBarOverlay;
                };

        if (overlay.isEnabled() && !shouldDisplayOnBossBar) {
            event.setCanceled(true);
        }
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

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay corruptedBarOverlay = new CorruptedBarOverlay();

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

        public float textureHeight() {
            return Texture.UNIVERSAL_BAR.height() / 2f;
        }

        public abstract BossBarModel.BarProgress progress();

        public abstract String icon();

        public abstract boolean isActive();

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld() || !isActive()) return;

            float barHeight = textureHeight() * (this.getWidth() / 81);
            float renderY = getModifiedRenderY(barHeight + 10);

            BossBarModel.BarProgress barProgress = progress();

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
        public boolean isActive() {
            return true;
        }

        @Override
        public BossBarModel.BarProgress progress() {
            int current = ActionBarModel.getCurrentHealth();
            int max = ActionBarModel.getMaxHealth();
            return new BossBarModel.BarProgress(current, max, current / (float) max);
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
        public BossBarModel.BarProgress progress() {
            return BossBarModel.bloodPoolBar.getBar();
        }

        @Override
        public boolean isActive() {
            return BossBarModel.bloodPoolBar.isActive();
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
        public BossBarModel.BarProgress progress() {
            int current = ActionBarModel.getCurrentMana();
            int max = ActionBarModel.getMaxMana();
            return new BossBarModel.BarProgress(current, max, current / (float) max);
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
        public BossBarModel.BarProgress progress() {
            return BossBarModel.manaBankBar.getBar();
        }

        @Override
        public boolean isActive() {
            return BossBarModel.manaBankBar.isActive();
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
        public BossBarModel.BarProgress progress() {
            return BossBarModel.awakenedBar.getBar();
        }

        @Override
        public String icon() {
            return "۞";
        }

        @Override
        public boolean isActive() {
            return BossBarModel.awakenedBar.isActive();
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
        public BossBarModel.BarProgress progress() {
            return BossBarModel.focusBar.getBar();
        }

        @Override
        public String icon() {
            return "➶";
        }

        @Override
        public boolean isActive() {
            return BossBarModel.focusBar.isActive();
        }
    }

    public static class CorruptedBarOverlay extends BaseBarOverlay {

        protected CorruptedBarOverlay() {
            super(
                    new OverlayPosition(
                            -70,
                            -150,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21));
            textColor = CommonColors.PURPLE;
        }

        @Override
        public BossBarModel.BarProgress progress() {
            return BossBarModel.corruptedBar.getBar();
        }

        @Override
        public String icon() {
            return "☠";
        }

        @Override
        public boolean isActive() {
            return BossBarModel.corruptedBar.isActive();
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
