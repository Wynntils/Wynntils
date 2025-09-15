/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

public class ServerUptimeInfoOverlay extends TextOverlay {
    @Persisted
    private final Config<Boolean> showWorldInStream = new Config<>(false);

    public ServerUptimeInfoOverlay() {
        super(
                new OverlayPosition(
                        160 + McUtils.mc().font.lineHeight,
                        6,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.TOP_LEFT),
                new OverlaySize(130, 60),
                HorizontalAlignment.LEFT,
                VerticalAlignment.MIDDLE);
    }

    @Override
    protected String getTemplate() {
        String currentWorldStr = !showWorldInStream.get() && Models.WorldState.isInStream() ? "-" : "{current_world}";

        return "§7Your World: §b(" + currentWorldStr
                + ") §e{current_world_uptime}\n§7Newest World: §b({newest_world}) §e{world_uptime(newest_world)}";
    }

    @Override
    protected String getPreviewTemplate() {
        return "§7Your World: §b(WC1) §e6:32\n§7Newest World: §b(WC12) §e0:21";
    }

    @Override
    protected boolean hideWhenNoGui() {
        return false;
    }

    @Override
    public boolean isVisible() {
        return McUtils.mc().gui.getTabList().visible || Models.WorldState.getCurrentState() == WorldState.HUB;
    }
}
