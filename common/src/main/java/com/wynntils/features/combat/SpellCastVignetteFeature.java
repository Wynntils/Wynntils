/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class SpellCastVignetteFeature extends Feature {
    private static final int SHOW_VIGNETTE_TICKS = 40;

    @RegisterConfig
    public final Config<Boolean> renderVignette = new Config<>(true);

    @RegisterConfig
    public final Config<Integer> vignetteFadeTime = new Config<>(12);

    @RegisterConfig
    public final Config<Float> vignetteIntensity = new Config<>(0.75f);

    @RegisterConfig
    public final Config<CustomColor> vignetteColor = new Config<>(new CustomColor(0, 71, 201));

    private int vignetteTimer;
    private float intensity;

    @SubscribeEvent
    public void onSpellCast(SpellEvent.Cast event) {
        // An relativeCost of 1.0 means we just used all mana we have left
        float relativeCost =
                (float) event.getManaCost() / Models.CharacterStats.getMana().current();
        intensity = vignetteIntensity.get() * relativeCost;
        vignetteTimer = SHOW_VIGNETTE_TICKS;
    }

    @SubscribeEvent
    public void onSpellFailed(SpellEvent.Failed event) {
        intensity = 0f;
        vignetteTimer = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (vignetteTimer <= 0) return;

        vignetteTimer--;
    }

    @SubscribeEvent
    public void onRender(RenderEvent.Post event) {
        if (!renderVignette.get() || intensity <= 0f) return;

        int shownTicks = SHOW_VIGNETTE_TICKS - vignetteTimer;
        int fade = vignetteFadeTime.get() - shownTicks;
        if (fade > 0) {
            float alpha = intensity * ((float) fade / vignetteFadeTime.get());
            RenderUtils.renderVignetteOverlay(event.getPoseStack(), vignetteColor.get(), alpha);
        }
    }
}
