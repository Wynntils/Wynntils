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
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.wynn.InventoryUtils;
import net.minecraft.client.renderer.MultiBufferSource;

@ConfigCategory(Category.OVERLAYS)
public class PowderSpecialBarOverlayFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay powderSpecialBarOverlay = new PowderSpecialBarOverlay();

    public static class PowderSpecialBarOverlay extends Overlay {
        @ConfigInfo
        public Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

        @ConfigInfo
        public Config<Boolean> flip = new Config<>(false);

        @ConfigInfo
        public Config<Boolean> onlyIfWeaponHeld = new Config<>(true);

        @ConfigInfo
        public Config<Boolean> hideIfNoCharge = new Config<>(true);

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
        public void render(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            float powderSpecialCharge = Models.Character.getPowderSpecialCharge();
            Powder powderSpecialType = Models.Character.getPowderSpecialType();
            if (this.onlyIfWeaponHeld.get()
                    && !InventoryUtils.isWeapon(McUtils.inventory().getSelected())) return;
            if (this.hideIfNoCharge.get() && (powderSpecialCharge == 0 || powderSpecialType == null)) return;

            renderWithSpecificSpecial(poseStack, bufferSource, powderSpecialCharge, powderSpecialType);
        }

        @Override
        public void renderPreview(
                PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
            renderWithSpecificSpecial(poseStack, bufferSource, 40, Powder.THUNDER);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}

        private void renderWithSpecificSpecial(
                PoseStack poseStack,
                MultiBufferSource.BufferSource bufferSource,
                float powderSpecialCharge,
                Powder powderSpecialType) {
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

            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            bufferSource,
                            text,
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            renderY,
                            0,
                            color,
                            this.getRenderHorizontalAlignment(),
                            this.textShadow.get());

            BufferedRenderUtils.drawColoredProgressBar(
                    poseStack,
                    bufferSource,
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
                    (this.flip.get() ? -1f : 1f) * powderSpecialCharge / 100f);
        }
    }
}
