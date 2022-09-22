/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class NpcDialogEvent extends Event {
    private final String codedDialog;
    private final boolean needsConfirmation;

    public NpcDialogEvent(String codedDialog, boolean needsConfirmation) {
        this.codedDialog = codedDialog;
        this.needsConfirmation = needsConfirmation;
    }

    public String getCodedDialog() {
        return codedDialog;
    }

    public boolean isNeedsConfirmation() {
        return needsConfirmation;
    }
}
