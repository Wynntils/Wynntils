/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigInfo;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.TextOverlay;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class AuraTimerOverlayFeature extends UserFeature {
    private static final float MAX_INTENSITY = 0.4f;

    @ConfigInfo
    public Config<Boolean> vignetteOnAura = new Config<>(true);

    @ConfigInfo
    public Config<CustomColor> vignetteColor = new Config<>(CommonColors.ORANGE);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay auraTimerOverlay = new AuraTimerOverlay();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderGui(RenderEvent.Post event) {
        if (!vignetteOnAura || event.getType() != RenderEvent.ElementType.GUI) return;
        long remainingTimeUntilAura = Models.TowerAuraTimer.getRemainingTimeUntilAura();
        if (remainingTimeUntilAura <= 0) return;

        RenderUtils.renderVignetteOverlay(
                event.getPoseStack(),
                vignetteColor,
                MathUtils.map(remainingTimeUntilAura, Models.TowerAuraTimer.getAuraLength(), 0, 0, MAX_INTENSITY));
    }

    public static class AuraTimerOverlay extends TextOverlay {
        private static final String TEMPLATE =
                "{IF_STRING(GTE(AURA_TIMER; 0); CONCAT(\"Aura: : \"; STRING(AURA_TIMER:1); \"s\"); \"\")}";

        @ConfigInfo
        public Config<CustomColor> textColor = new Config<>(CommonColors.ORANGE);

        protected AuraTimerOverlay() {
            super(
                    new OverlayPosition(
                            0,
                            0,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.Middle),
                    new GuiScaledOverlaySize(150, 30),
                    HorizontalAlignment.Center,
                    VerticalAlignment.Middle);
        }

        @Override
        public CustomColor getRenderColor() {
            return textColor;
        }

        @Override
        public float getTextScale() {
            return this.getHeight() / 15f;
        }

        @Override
        public String getTemplate() {
            return TEMPLATE;
        }

        @Override
        public String getPreviewTemplate() {
            return "Aura: 3.2s";
        }
    }
}
