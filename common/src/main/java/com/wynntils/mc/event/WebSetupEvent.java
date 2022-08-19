/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.EventThread;
import net.minecraftforge.eventbus.api.Event;

/** Fired on initialization of loading of apiurls in {@link com.wynntils.core.webapi.WebManager} */
@EventThread(EventThread.Type.WORKER)
public class WebSetupEvent extends Event {}
