/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.overlays.Overlay;
import com.wynntils.core.consumers.features.overlays.OverlayPosition;
import com.wynntils.core.consumers.features.overlays.OverlaySize;
import com.wynntils.core.consumers.features.overlays.TextOverlay;
import com.wynntils.core.consumers.features.overlays.annotations.OverlayInfo;
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
public class AuraTimerOverlayFeature extends Feature {
    private static final float MAX_INTENSITY = 0.4f;

    @RegisterConfig
    public final Config<Boolean> vignetteOnAura = new Config<>(true);

    @RegisterConfig
    public final Config<CustomColor> vignetteColor = new Config<>(CommonColors.ORANGE);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay auraTimerOverlay = new AuraTimerOverlay();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderGui(RenderEvent.Post event) {
        if (!vignetteOnAura.get() || event.getType() != RenderEvent.ElementType.GUI) return;
        long remainingTimeUntilAura = Models.TowerAuraTimer.getRemainingTimeUntilAura();
        if (remainingTimeUntilAura <= 0) return;

        RenderUtils.renderVignetteOverlay(
                event.getPoseStack(),
                vignetteColor.get(),
                MathUtils.map(remainingTimeUntilAura, Models.TowerAuraTimer.getAuraLength(), 0, 0, MAX_INTENSITY));
    }

    public static class AuraTimerOverlay extends TextOverlay {
        private static final String TEMPLATE =
                "{if_string(gte(aura_timer; 0); concat(\"Aura: : \"; string(aura_timer:1); \"s\"); \"\")}";

        @RegisterConfig
        public final Config<CustomColor> textColor = new Config<>(CommonColors.ORANGE);

        protected AuraTimerOverlay() {
            super(
                    new OverlayPosition(
                            0,
                            0,
                            VerticalAlignment.TOP,
                            HorizontalAlignment.CENTER,
                            OverlayPosition.AnchorSection.MIDDLE),
                    new OverlaySize(150, 30),
                    HorizontalAlignment.CENTER,
                    VerticalAlignment.MIDDLE);
            fontScale.updateConfig(2f);
        }

        @Override
        public CustomColor getRenderColor() {
            return textColor.get();
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
