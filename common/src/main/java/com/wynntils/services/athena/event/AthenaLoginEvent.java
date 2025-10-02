/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.athena.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.EventThread;

@EventThread(EventThread.Type.WORKER)
public final class AthenaLoginEvent extends BaseEvent {}
