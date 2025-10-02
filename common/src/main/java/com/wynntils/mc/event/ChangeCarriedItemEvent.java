/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;

/**
 * This event is only fired when the selected hotbar item changes. If for example, the player
 * has hotbar 5 selected and presses 5 again, this event will not be fired.
 */
public final class ChangeCarriedItemEvent extends BaseEvent {}
