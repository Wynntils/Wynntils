/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.bossbars;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.bossbar.TrackedBar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkipCutsceneBar extends TrackedBar {
    private static final Pattern CUTSCENE_SKIP_PATTERN =
            Pattern.compile("§7Press §f\uE005 SWAP HANDS§7 to skip( §8- §f\\d+§7/§f\\d+)?");

    public SkipCutsceneBar() {
        super(CUTSCENE_SKIP_PATTERN);
    }

    @Override
    public void onUpdateName(Matcher match) {
        boolean groupCutscene = match.group(1) != null;
        Models.WorldState.cutsceneStarted(groupCutscene);
    }

    @Override
    protected void reset() {
        super.reset();

        Models.WorldState.cutsceneEnded();
    }
}
