/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.TextShadow;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.objects.Powder;
import com.wynntils.wynn.utils.InventoryUtils;
import java.util.List;

@FeatureInfo(category = FeatureCategory.OVERLAYS)
public class PowderSpecialBarOverlayFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay powderSpecialBarOverlay = new PowderSpecialBarOverlay();

    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.ActionBar);
    }

    public static class PowderSpecialBarOverlay extends Overlay {
        @Config
        public TextShadow textShadow = TextShadow.OUTLINE;

        @Config
        public boolean flip = false;

        @Config
        public boolean onlyIfWeaponHeld = true;

        @Config
        public boolean hideIfNoCharge = true;

        protected PowderSpecialBarOverlay() {
            super(
                    new OverlayPosition(
                            -30,
                            150,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(81, 21));
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            float powderSpecialCharge = Models.ActionBar.getPowderSpecialCharge();
            Powder powderSpecialType = Models.ActionBar.getPowderSpecialType();
            if (this.onlyIfWeaponHeld
                    && !InventoryUtils.isWeapon(McUtils.inventory().getSelected())) return;
            if (this.hideIfNoCharge && (powderSpecialCharge == 0 || powderSpecialType == null)) return;

            renderWithSpecificSpecial(poseStack, powderSpecialCharge, powderSpecialType);
        }

        @Override
        public void renderPreview(PoseStack poseStack, float partialTicks, Window window) {
            renderWithSpecificSpecial(poseStack, 40, Powder.THUNDER);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}

        private void renderWithSpecificSpecial(
                PoseStack poseStack, float powderSpecialCharge, Powder powderSpecialType) {
            Texture universalBarTexture = Texture.UNIVERSAL_BAR;

            final float renderedHeight = universalBarTexture.height() / 2f * (this.getWidth() / 81);

            float renderY =
                    switch (this.getRenderVerticalAlignment()) {
                        case Top -> this.getRenderY();
                        case Middle -> this.getRenderY() + (this.getHeight() - renderedHeight) / 2;
                        case Bottom -> this.getRenderY() + this.getHeight() - renderedHeight;
                    };

            CustomColor color;
            String text;
            if (powderSpecialType == null) {
                color = CommonColors.GRAY;
                text = "Unknown";
            } else {
                color = powderSpecialType.getColor();
                text = powderSpecialType.getColoredSymbol() + " " + (int) powderSpecialCharge + "%";
            }

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            text,
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            renderY,
                            0,
                            color,
                            this.getRenderHorizontalAlignment(),
                            this.textShadow);

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
                    (this.flip ? -1f : 1f) * powderSpecialCharge / 100f);
        }
    }
}
