/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.handlers.bossbar.event.BossBarAddedEvent;
import com.wynntils.models.worlds.bossbars.StreamerModeBar;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.neoforged.bus.api.SubscribeEvent;

public class StreamerModeOverlay extends TextOverlay {
    private static final String TEMPLATE =
            "{concat(\"§#ff3131ff\";to_fancy_text(\"Streamer Mode Enabled\");\"\n§l§\";to_hex_string(shine_shader);to_fancy_text(\"PLAY.WYNNCRAFT.COM\"))}";

    @Persisted
    private final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    public StreamerModeOverlay() {
        super(
                new OverlayPosition(
                        15,
                        -5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.TOP_RIGHT),
                120,
                10);

        fontScale.store(0.8f);
    }

    @SubscribeEvent
    public void onBossBarAdd(BossBarAddedEvent event) {
        if (!Managers.Overlay.isEnabled(this)) return;
        if (!event.getTrackedBar().getClass().equals(StreamerModeBar.class)) return;

        if (!shouldDisplayOriginal.get()) {
            event.setCanceled(true);
        }
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected String getPreviewTemplate() {
        return getTemplate();
    }

    @Override
    public boolean isVisible() {
        return Models.WorldState.isInStream();
    }
}
