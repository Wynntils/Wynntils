/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.war.event.GuildWarTowerEffectEvent;
import com.wynntils.overlays.TowerAuraTimerOverlay;
import com.wynntils.overlays.TowerVolleyTimerOverlay;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class TowerEffectOverlayFeature extends Feature {
    private static final SoundEvent AURA_SOUND = SoundEvents.ANVIL_LAND;
    private static final SoundEvent VOLLEY_SOUND = SoundEvents.BLAZE_SHOOT;

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay auraTimerOverlay = new TowerAuraTimerOverlay();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay volleyTimerOverlay = new TowerVolleyTimerOverlay();

    // Sound configs
    @Persisted
    private final Config<Boolean> soundEffectAura = new Config<>(true);

    @Persisted
    private final Config<Boolean> soundEffectVolley = new Config<>(false);

    @Persisted
    private final Config<Float> soundVolume = new Config<>(1.0f);

    @Persisted
    private final Config<Float> soundPitch = new Config<>(1.0f);

    // Vigentte configs
    @Persisted
    private final Config<Boolean> vignetteOnAura = new Config<>(true);

    @Persisted
    private final Config<Boolean> vignetteOnVolley = new Config<>(true);

    @Persisted
    private final Config<CustomColor> auraVignetteColor = new Config<>(CommonColors.ORANGE);

    @Persisted
    private final Config<CustomColor> volleyVignetteColor = new Config<>(CommonColors.MAGENTA);

    @Persisted
    private final Config<Float> auraVignetteIntensity = new Config<>(0.4f);

    @Persisted
    private final Config<Float> volleyVignetteIntensity = new Config<>(0.4f);

    @SubscribeEvent
    public void onTowerAura(GuildWarTowerEffectEvent.AuraSpawned event) {
        if (!soundEffectAura.get()) return;

        McUtils.playSoundAmbient(AURA_SOUND, soundVolume.get(), soundPitch.get());
    }

    @SubscribeEvent
    public void onTowerVolley(GuildWarTowerEffectEvent.VolleySpawned event) {
        if (!soundEffectVolley.get()) return;

        McUtils.playSoundAmbient(VOLLEY_SOUND, soundVolume.get(), soundPitch.get());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderGui(RenderEvent.Post event) {
        if (event.getType() != RenderEvent.ElementType.GUI) return;

        if (vignetteOnAura.get()) {
            renderAuraVignette(event.getGuiGraphics());
        }

        if (vignetteOnVolley.get()) {
            renderVolleyVignette(event.getGuiGraphics());
        }
    }

    private void renderAuraVignette(GuiGraphics guiGraphics) {
        long remainingTimeUntilAura = Models.GuildWarTower.getRemainingTimeUntilAura();
        if (remainingTimeUntilAura <= 0) return;

        RenderUtils.renderVignetteOverlay(
                guiGraphics,
                auraVignetteColor.get(),
                MathUtils.map(
                        remainingTimeUntilAura,
                        Models.GuildWarTower.getEffectLength(),
                        0,
                        0,
                        auraVignetteIntensity.get()));
    }

    private void renderVolleyVignette(GuiGraphics guiGraphics) {
        long remainingTimeUntilVolley = Models.GuildWarTower.getRemainingTimeUntilVolley();
        if (remainingTimeUntilVolley <= 0) return;

        RenderUtils.renderVignetteOverlay(
                guiGraphics,
                volleyVignetteColor.get(),
                MathUtils.map(
                        remainingTimeUntilVolley,
                        Models.GuildWarTower.getEffectLength(),
                        0,
                        0,
                        volleyVignetteIntensity.get()));
    }
}
