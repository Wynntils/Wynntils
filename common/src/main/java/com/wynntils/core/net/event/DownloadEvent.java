/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.event;

import net.neoforged.bus.api.Event;

/**
 * An event that is fired when a download starts, completes, or fails.
 */
public abstract class DownloadEvent extends Event {
    public static class Started extends DownloadEvent {}

    public static class Completed extends DownloadEvent {}

    public static class Failed extends DownloadEvent {}
}
