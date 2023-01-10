/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.event;

import com.wynntils.wynn.objects.ShamanTotem;

public class TotemRemovedEvent extends TotemEvent {
    private final ShamanTotem totem;

    public TotemRemovedEvent(int totemNumber, ShamanTotem totem) {
        super(totemNumber);
        this.totem = totem;
    }

    public ShamanTotem getTotem() {
        return totem;
    }
}
