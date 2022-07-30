/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.event;

import java.util.List;
import net.minecraftforge.eventbus.api.Event;

public class NpcDialogEvent extends Event {
    private final List<String> codedDialogLines;

    public NpcDialogEvent(List<String> codedDialogLines) {
        this.codedDialogLines = codedDialogLines;
    }

    public List<String> getCodedDialogLines() {
        return codedDialogLines;
    }
}
