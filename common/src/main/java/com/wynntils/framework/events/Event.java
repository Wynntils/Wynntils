package com.wynntils.framework.events;

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
