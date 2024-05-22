/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.models.abilities.type.OphanimOrb;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OphanimBar extends TrackedBar {
    // Test in OphanimBar_OPHANIM_PATTERN
    private static final Pattern OPHANIM_PATTERN =
            Pattern.compile("^§710s Healed: §f(?<healed>\\d+)% §[3468]\\[(?<orbs>((§[bce7])?⏺){0,6})(§[3468])?\\]$");

    private static final Pattern ORB_PATTERN = Pattern.compile("(?<color>§[bce7])?⏺");

    private int healed = 0;
    private final List<OphanimOrb> orbs = new ArrayList<>();

    public OphanimBar() {
        super(OPHANIM_PATTERN);
    }

    public int getHealed() {
        return healed;
    }

    public List<OphanimOrb> getOrbs() {
        return Collections.unmodifiableList(orbs);
    }

    @Override
    public void onUpdateName(Matcher match) {
        healed = Integer.parseInt(match.group("healed"));

        orbs.clear();
        Matcher orbMatcher = ORB_PATTERN.matcher(match.group("orbs"));

        OphanimOrb.HealthState healthState = null;
        int start = 0;
        while (orbMatcher.find(start)) {
            String colorCode = orbMatcher.group("color");
            if (colorCode != null) {
                healthState = OphanimOrb.HealthState.fromColorCode(colorCode);
            }

            orbs.add(new OphanimOrb(healthState));

            start = orbMatcher.end();
        }
    }

    @Override
    public void onUpdateProgress(float progress) {
        updateValue((int) (progress * 100), 100);
    }
}
