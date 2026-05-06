/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.models.containers.Container;
import java.util.List;
import java.util.regex.Pattern;

public class RaidStartContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFE1\uE00C");

    private static final List<Integer> GAMBIT_SLOTS = List.of(1, 3, 5, 7);
    private static final List<Integer> PLAYER_SLOTS = List.of(18, 19, 20, 21);

    public RaidStartContainer() {
        super(TITLE_PATTERN);
    }

    public static List<Integer> getGambitSlots() {
        return GAMBIT_SLOTS;
    }

    public static List<Integer> getPlayerSlots() {
        return PLAYER_SLOTS;
    }
}
