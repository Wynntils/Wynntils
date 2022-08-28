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

    @OverlayInfo(renderType = RenderEvent.ElementType.FoodBar, renderAt = OverlayInfo.RenderState.Replace)
    private final Overlay manaBarOverlay = new ManaBarOverlay();

    public static class HealthBarOverlay extends Overlay {
        @Config
        public HealthTexture healthTexture = HealthTexture.a;

        @Config
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config
        public boolean flip = false;

        @Config
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

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            final float renderedHeight = 10 + healthTexture.getHeight() * (this.getWidth() / 81);

            float renderY =
                    switch (this.getRenderVerticalAlignment()) {
                        case Top -> this.getRenderY();
                        case Middle -> this.getRenderY() + (this.getHeight() - renderedHeight) / 2;
                        case Bottom -> this.getRenderY() + this.getHeight() - renderedHeight;
                    };

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            ActionBarModel.getCurrentHealth() + " ❤ " + ActionBarModel.getMaxHealth(),
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            renderY,
                            0,
                            this.textColor,
                            FontRenderer.TextAlignment.fromHorizontalAlignment(this.getRenderHorizontalAlignment()),
                            this.textShadow);
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
                    (flip ? -ActionBarModel.getCurrentHealth() : ActionBarModel.getCurrentHealth())
                            / (float) ActionBarModel.getMaxHealth());
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }

    public static class ManaBarOverlay extends Overlay {
        @Config
        public ManaTexture manaTexture = ManaTexture.a;

        @Config
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config
        public boolean flip = false;

        @Config
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

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            final float renderedHeight = 10 + manaTexture.getHeight() * (this.getWidth() / 81);

            float renderY =
                    switch (this.getRenderVerticalAlignment()) {
                        case Top -> this.getRenderY();
                        case Middle -> this.getRenderY() + (this.getHeight() - renderedHeight) / 2;
                        case Bottom -> this.getRenderY() + this.getHeight() - renderedHeight;
                    };

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            ActionBarModel.getCurrentMana() + " ✺ " + ActionBarModel.getMaxMana(),
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            renderY,
                            0,
                            this.textColor,
                            FontRenderer.TextAlignment.fromHorizontalAlignment(this.getRenderHorizontalAlignment()),
                            this.textShadow);
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
                    (flip ? -ActionBarModel.getCurrentMana() : ActionBarModel.getCurrentMana())
                            / (float) ActionBarModel.getMaxMana());
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
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
