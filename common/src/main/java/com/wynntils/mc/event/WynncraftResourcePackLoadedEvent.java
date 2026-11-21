/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.neoforged.bus.api.Event;

/**
 * Fired when a resource reload has finished and the Wynncraft resource pack is active.
 * <p>This fires for both the pack preloaded by Wynntils and a pack sent by the server,
 * and fires again after every later reload that still has the pack active.
 */
public class WynncraftResourcePackLoadedEvent extends Event {}
