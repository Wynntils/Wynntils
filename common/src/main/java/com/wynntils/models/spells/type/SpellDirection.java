/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

import java.util.Arrays;

public enum SpellDirection {
    RIGHT("\uE101", "\uE104"),
    LEFT("\uE100", "\uE103");

    private final String fullIcon;
    private final String smallIcon;

    public static final SpellDirection[] NO_SPELL = new SpellDirection[0];

    SpellDirection(String fullIcon, String smallIcon) {
        this.fullIcon = fullIcon;
        this.smallIcon = smallIcon;
    }

    // Icons used in the font\hud\gameplay\default\bottom_middle font
    public String getFullIcon() {
        return fullIcon;
    }

    public String getSmallIcon() {
        return smallIcon;
    }

    public static SpellDirection[] invertArray(SpellDirection[] initial) {
        return Arrays.stream(initial).map((x) -> (x == RIGHT) ? LEFT : RIGHT).toArray(SpellDirection[]::new);
    }
}
