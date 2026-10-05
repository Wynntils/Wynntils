/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

import com.wynntils.utils.type.Time;
import java.util.List;
import java.util.Objects;

public record MountEnclosureInfo(Mount mount, List<Object> mountItems, Time time) {
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MountEnclosureInfo that = (MountEnclosureInfo) obj;
        return Objects.equals(this.mount, that.mount) && Objects.deepEquals(this.mountItems, that.mountItems) &&
                // It's only considered equals if both Time is equal to NONE or not equal to NONE and the offset is below or equal to 50 seconds
                (this.time == Time.NONE ? that.time == Time.NONE : that.time != Time.NONE && this.time.getOffset(that.time) <= 50);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mount, mountItems, time);
    }
}