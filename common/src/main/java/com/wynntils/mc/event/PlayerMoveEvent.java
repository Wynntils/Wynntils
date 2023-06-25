/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Event;

/**
 * Fires when player moves. Does not include rotation or crouching.
 * Does not fire when player teleports.
 */
public class PlayerMoveEvent extends Event {}
