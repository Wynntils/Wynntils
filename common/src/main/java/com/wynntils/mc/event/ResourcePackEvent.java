/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.EventThread;
import net.minecraftforge.eventbus.api.Event;

/** Fires on receiving {@link net.minecraft.network.protocol.game.ClientboundResourcePackPacket} */
@EventThread(EventThread.Type.IO)
public class ResourcePackEvent extends Event {}
