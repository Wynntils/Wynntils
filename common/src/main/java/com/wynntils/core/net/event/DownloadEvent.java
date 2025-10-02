/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.event;

import com.wynntils.core.events.BaseEvent;

/**
 * An event that is fired when a download starts, completes, or fails.
 */
public abstract class DownloadEvent extends BaseEvent {
    public static class Started extends DownloadEvent {
        private final boolean partial;

        public Started(boolean partial) {
            this.partial = partial;
        }

        public boolean isPartial() {
            return partial;
        }
    }

    public static class Completed extends DownloadEvent {}

    public static class Failed extends DownloadEvent {}
}
