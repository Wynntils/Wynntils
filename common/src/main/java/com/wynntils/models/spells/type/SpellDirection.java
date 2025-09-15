/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

import com.wynntils.utils.mc.MouseUtils;
import java.util.Arrays;

public enum SpellDirection {
    RIGHT("\uE101", "\uE104", MouseUtils::sendRightClickInput),
    LEFT("\uE100", "\uE103", MouseUtils::sendLeftClickInput);

    private final String fullIcon;
    private final String smallIcon;
    private final Runnable sendPacketRunnable;

    public static final SpellDirection[] NO_SPELL = new SpellDirection[0];

    SpellDirection(String fullIcon, String smallIcon, Runnable sendPacketRunnable) {
        this.fullIcon = fullIcon;
        this.smallIcon = smallIcon;
        this.sendPacketRunnable = sendPacketRunnable;
    }

    // Icons used in the font\hud\gameplay\default\bottom_middle font
    public String getFullIcon() {
        return fullIcon;
    }

    public String getSmallIcon() {
        return smallIcon;
    }

    public Runnable getSendPacketRunnable() {
        return sendPacketRunnable;
    }

    public static SpellDirection[] invertArray(SpellDirection[] initial) {
        return Arrays.stream(initial).map((x) -> (x == RIGHT) ? LEFT : RIGHT).toArray(SpellDirection[]::new);
    }
}
