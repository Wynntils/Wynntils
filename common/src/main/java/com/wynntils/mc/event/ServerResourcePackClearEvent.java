/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.EventThread;
import net.minecraftforge.eventbus.api.Event;

@EventThread(EventThread.Type.ANY)
public class ServerResourcePackClearEvent extends Event {}
