/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.athena.event;

import com.wynntils.core.events.EventThread;
import net.minecraftforge.eventbus.api.Event;

@EventThread(EventThread.Type.WORKER)
public class AthenaLoginEvent extends Event {}
