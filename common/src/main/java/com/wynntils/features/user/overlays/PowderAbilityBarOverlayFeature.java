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
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.wynn.model.ActionBarModel;
import com.wynntils.wynn.objects.Powder;
import com.wynntils.wynn.utils.WynnUtils;

public class PowderAbilityBarOverlayFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay powderAbilityBarOverlay = new PowderAbilityBarOverlay();

    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        dependencies.add(ActionBarModel.class);
    }

    public static class PowderAbilityBarOverlay extends Overlay {
        @Config
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config
        public boolean flip = false;

        public PowderAbilityBarOverlay() {
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
            float powderSpecialCharge = ActionBarModel.getPowderSpecialCharge();
            if (!WynnUtils.onServer() || powderSpecialCharge == -1) return;

            renderWithSpecificSpecial(poseStack, powderSpecialCharge, ActionBarModel.getPowderSpecialType());
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

            CustomColor color = powderSpecialType.getColor();

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            powderSpecialType.getColoredSymbol() + " " + (int) powderSpecialCharge + "%",
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
