/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.event;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** Fired before a keybind or menu action opens the content book. Background queries do not fire this event. */
public class ContentBookOpenEvent extends Event implements ICancellableEvent {}
