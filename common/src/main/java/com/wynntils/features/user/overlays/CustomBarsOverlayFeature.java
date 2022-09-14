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
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.wynn.event.ActionBarMessageUpdateEvent;
import com.wynntils.wynn.model.ActionBarModel;
import com.wynntils.wynn.utils.WynnBossBarUtils;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.OVERLAYS)
public class CustomBarsOverlayFeature extends UserFeature {

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

    public static class HealthBarOverlay extends Overlay {
        @Config(key = "feature.wynntils.customBarsOverlay.overlay.healthBar.healthTexture")
        public HealthTexture healthTexture = HealthTexture.a;

        @Config(key = "feature.wynntils.customBarsOverlay.overlay.healthBar.textShadow")
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config(key = "feature.wynntils.customBarsOverlay.overlay.healthBar.flip")
        public boolean flip = false;

        @Config(key = "feature.wynntils.customBarsOverlay.overlay.healthBar.textColor")
        public CustomColor textColor = CommonColors.RED;

        public HealthBarOverlay() {
            super(
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
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            float renderY = getModifiedRenderY();

            String text = ActionBarModel.getCurrentHealth() + " ❤ " + ActionBarModel.getMaxHealth();
            renderText(poseStack, renderY, text);

            float progress = (flip ? -ActionBarModel.getCurrentHealth() : ActionBarModel.getCurrentHealth())
                    / (float) ActionBarModel.getMaxHealth();
            renderBar(poseStack, renderY, progress);
        }

        protected float getModifiedRenderY() {
            final float renderedHeight = 10 + healthTexture.getHeight() * (this.getWidth() / 81);

            return switch (this.getRenderVerticalAlignment()) {
                case Top -> this.getRenderY();
                case Middle -> this.getRenderY() + (this.getHeight() - renderedHeight) / 2;
                case Bottom -> this.getRenderY() + this.getHeight() - renderedHeight;
            };
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}

        protected void renderBar(PoseStack poseStack, float renderY, float progress) {
            RenderUtils.drawProgressBar(
                    poseStack,
                    Texture.HEALTH_BAR,
                    this.getRenderX(),
                    renderY + 10,
                    this.getRenderX() + this.getWidth(),
                    renderY + 10 + healthTexture.getHeight() * (this.getWidth() / 81),
                    0,
                    healthTexture.getTextureY1(),
                    81,
                    healthTexture.getTextureY2(),
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
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            WynnBossBarUtils.BloodPool bloodPool = WynnBossBarUtils.getBloodPool();
            if (bloodPool.equals(WynnBossBarUtils.NO_BLOOD_POOL)) return;

            float renderY = getModifiedRenderY();

            String text = "Blood Pool: " + bloodPool.percent() + "%";
            renderText(poseStack, renderY, text);

            float progress = (this.flip ? -1 : 1) * bloodPool.progress();
            renderBar(poseStack, renderY, progress);
        }
    }

    public static class AwakenedProgressBarOverlay extends Overlay {
        @Config(key = "feature.wynntils.customBarsOverlay.overlay.awakenedProgressBar.textShadow")
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config(key = "feature.wynntils.customBarsOverlay.overlay.awakenedProgressBar.flip")
        public boolean flip = false;

        @Config(key = "feature.wynntils.customBarsOverlay.overlay.awakenedProgressBar.textColor")
        public CustomColor textColor = CommonColors.WHITE;

        public AwakenedProgressBarOverlay() {
            super(
                    new OverlayPosition(
                            -70,
                            -150,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21));
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            WynnBossBarUtils.AwakenedBar awakenedBar = WynnBossBarUtils.getAwakenedBar();
            if (awakenedBar.equals(WynnBossBarUtils.NO_AWAKENED_PROGRESS)) return;

            Texture universalBarTexture = Texture.UNIVERSAL_BAR;

            final float renderedHeight = universalBarTexture.height() / 2f * (this.getWidth() / 81);

            float renderY =
                    switch (this.getRenderVerticalAlignment()) {
                        case Top -> this.getRenderY();
                        case Middle -> this.getRenderY() + (this.getHeight() - renderedHeight) / 2;
                        case Bottom -> this.getRenderY() + this.getHeight() - renderedHeight;
                    };

            String text = "Awakened: " + awakenedBar.current() + "/200";
            renderText(poseStack, renderY, text);

            float progress = (this.flip ? -1 : 1) * awakenedBar.progress();
            renderColoredBar(poseStack, renderY, this.textColor, progress);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}

        public void renderColoredBar(PoseStack poseStack, float renderY, CustomColor color, float progress) {
            Texture universalBarTexture = Texture.UNIVERSAL_BAR;
            final float renderedHeight = universalBarTexture.height() / 2f * (this.getWidth() / 81);

            RenderUtils.drawColoredProgressBar(
                    poseStack,
                    universalBarTexture,
                    color,
                    this.getRenderX(),
                    renderY + 10,
                    this.getRenderX() + this.getWidth(),
                    renderY + 10 + renderedHeight,
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

    public static class FocusBarOverlay extends Overlay {
        @Config(key = "feature.wynntils.customBarsOverlay.overlay.focusBar.textShadow")
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config(key = "feature.wynntils.customBarsOverlay.overlay.focusBar.flip")
        public boolean flip = false;

        @Config(key = "feature.wynntils.customBarsOverlay.overlay.focusBar.textColor")
        public CustomColor textColor = CommonColors.YELLOW;

        public FocusBarOverlay() {
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
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            WynnBossBarUtils.Focus focus = WynnBossBarUtils.getFocusBar();
            if (focus.equals(WynnBossBarUtils.NO_FOCUS)) return;

            Texture universalBarTexture = Texture.UNIVERSAL_BAR;

            final float renderedHeight = universalBarTexture.height() / 2f * (this.getWidth() / 81);

            float renderY =
                    switch (this.getRenderVerticalAlignment()) {
                        case Top -> this.getRenderY();
                        case Middle -> this.getRenderY() + (this.getHeight() - renderedHeight) / 2;
                        case Bottom -> this.getRenderY() + this.getHeight() - renderedHeight;
                    };

            String text = "Focus: " + focus.current() + "/" + focus.max();
            renderText(poseStack, renderY, text);

            float progress = (this.flip ? -1 : 1) * focus.progress();
            renderColoredBar(poseStack, renderY, this.textColor, progress);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}

        public void renderColoredBar(PoseStack poseStack, float renderY, CustomColor color, float progress) {
            Texture universalBarTexture = Texture.UNIVERSAL_BAR;
            final float renderedHeight = universalBarTexture.height() / 2f * (this.getWidth() / 81);

            RenderUtils.drawColoredProgressBar(
                    poseStack,
                    universalBarTexture,
                    color,
                    this.getRenderX(),
                    renderY + 10,
                    this.getRenderX() + this.getWidth(),
                    renderY + 10 + renderedHeight,
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

    public static class ManaBarOverlay extends Overlay {
        @Config(key = "feature.wynntils.customBarsOverlay.overlay.manaBar.manaTexture")
        public ManaTexture manaTexture = ManaTexture.a;

        @Config(key = "feature.wynntils.customBarsOverlay.overlay.manaBar.textShadow")
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config(key = "feature.wynntils.customBarsOverlay.overlay.manaBar.flip")
        public boolean flip = false;

        @Config(key = "feature.wynntils.customBarsOverlay.overlay.manaBar.textColor")
        public CustomColor textColor = CommonColors.LIGHT_BLUE;

        public ManaBarOverlay() {
            super(
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
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            float renderY = getModifiedRenderY();

            String text = ActionBarModel.getCurrentMana() + " ✺ " + ActionBarModel.getMaxMana();
            renderText(poseStack, renderY, text);

            float progress = (flip ? -ActionBarModel.getCurrentMana() : ActionBarModel.getCurrentMana())
                    / (float) ActionBarModel.getMaxMana();
            renderBar(poseStack, renderY, progress);
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

        protected void renderBar(PoseStack poseStack, float renderY, float progress) {
            RenderUtils.drawProgressBar(
                    poseStack,
                    Texture.MANA_BAR,
                    this.getRenderX(),
                    renderY + 10,
                    this.getRenderX() + this.getWidth(),
                    renderY + 10 + manaTexture.getHeight() * (this.getWidth() / 81),
                    0,
                    manaTexture.getTextureY1(),
                    81,
                    manaTexture.getTextureY2(),
                    progress);
        }

        protected float getModifiedRenderY() {
            final float renderedHeight = 10 + manaTexture.getHeight() * (this.getWidth() / 81);

            return switch (this.getRenderVerticalAlignment()) {
                case Top -> this.getRenderY();
                case Middle -> this.getRenderY() + (this.getHeight() - renderedHeight) / 2;
                case Bottom -> this.getRenderY() + this.getHeight() - renderedHeight;
            };
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
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
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            WynnBossBarUtils.ManaBank manaBank = WynnBossBarUtils.getManaBank();
            if (manaBank.equals(WynnBossBarUtils.NO_MANA_BANK)) return;

            float renderY = getModifiedRenderY();

            String text = "Mana Bank: " + manaBank.percent() + " ✺ " + manaBank.maxPercent();
            renderText(poseStack, renderY, text);

            float progress = (flip ? -1 : 1) * manaBank.progress();
            renderBar(poseStack, renderY, progress);
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
