/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.framework.events;

/** The context dispatched when something happens as a hook */
public abstract class Event {
    boolean canceled = false;

    public void setCanceled(boolean canceled) {
        if (isCanceled()) this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public abstract boolean isCancellable();
}
