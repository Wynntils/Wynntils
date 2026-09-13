/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.event;

import com.wynntils.models.mount.type.MountType;
import net.neoforged.bus.api.Event;

public abstract class MountEvent extends Event {
    public static class Summon extends MountEvent {
        private MountType mountType;

        public Summon(MountType mountType) {
            this.mountType = mountType;
        }

        public MountType getMountType() {
            return mountType;
        }
    }

    public static class Mount extends MountEvent {
        private MountType mountType;

        public Mount(MountType mountType) {
            this.mountType = mountType;
        }

        public MountType getMountType() {
            return mountType;
        }
    }

    public static class Dismount extends MountEvent {}
}
