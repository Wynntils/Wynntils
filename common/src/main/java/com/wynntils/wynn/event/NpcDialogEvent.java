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
    private final boolean autoProgressing;

    public NpcDialogEvent(String codedDialog, boolean autoProgressing) {
        this.codedDialog = codedDialog;
        this.autoProgressing = autoProgressing;
    }

    public String getCodedDialog() {
        return codedDialog;
    }

    public boolean isAutoProgressing() {
        return autoProgressing;
    }
}
