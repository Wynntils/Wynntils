/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.bossbars;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Pattern;

public class StreamerModeBar extends TrackedBar {
    private static final Pattern STREAMER_PATTERN = Pattern.compile("^§cStreamer mode enabled .+$");

    public StreamerModeBar() {
        super(STREAMER_PATTERN);
    }

    @Override
    public void onUpdateProgress(float progress) {
        Models.WorldState.setStreamerMode(true);
    }

    @Override
    protected void reset() {
        super.reset();

        Models.WorldState.setStreamerMode(false);
    }
}
