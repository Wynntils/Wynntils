/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class TowerAuraVignetteFeature extends Feature {
    private static final float MAX_INTENSITY = 0.4f;

    @Persisted
    public final Config<Boolean> vignetteOnAura = new Config<>(true);

    @Persisted
    public final Config<CustomColor> vignetteColor = new Config<>(CommonColors.ORANGE);

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
}
