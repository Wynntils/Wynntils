/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.CancelRequestable;
import net.minecraft.client.resources.sounds.SoundInstance;

public final class SoundPlayedEvent extends BaseEvent implements CancelRequestable {
    private final SoundInstance soundName;

    public SoundPlayedEvent(SoundInstance soundName) {
        this.soundName = soundName;
    }

    public SoundInstance getSoundInstance() {
        return soundName;
    }
}
