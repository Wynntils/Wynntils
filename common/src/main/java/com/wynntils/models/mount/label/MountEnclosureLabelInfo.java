/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.models.mount.type.MountEnclosure;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class MountEnclosureLabelInfo extends LabelInfo {
    private final MountEnclosure mountEnclosure;

    public MountEnclosureLabelInfo(StyledText label, Location location, Entity entity, MountEnclosure mountEnclosure) {
        super(label, location, entity);
        this.mountEnclosure = mountEnclosure;
    }

    public MountEnclosure getMountEnclosure() {
        return mountEnclosure;
    }
}