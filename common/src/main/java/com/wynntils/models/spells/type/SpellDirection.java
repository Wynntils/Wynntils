/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

import com.wynntils.utils.mc.MouseUtils;
import java.util.Arrays;

public enum SpellDirection {
    RIGHT(MouseUtils::sendRightClickInput),
    LEFT(MouseUtils::sendLeftClickInput);

    private final Runnable sendPacketRunnable;

    public static final SpellDirection[] NO_SPELL = new SpellDirection[0];

    SpellDirection(Runnable sendPacketRunnable) {
        this.sendPacketRunnable = sendPacketRunnable;
    }

    public Runnable getSendPacketRunnable() {
        return sendPacketRunnable;
    }

    public static SpellDirection[] invertArray(SpellDirection[] initial) {
        return Arrays.stream(initial).map((x) -> (x == RIGHT) ? LEFT : RIGHT).toArray(SpellDirection[]::new);
    }
}
