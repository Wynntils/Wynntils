/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.mount;

import com.google.common.base.CaseFormat;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.type.MountStat;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;

public class MountStatProvider extends BaseMountStatProvider<CappedValue> {
    private final MountStat stat;

    public MountStatProvider(MountStat stat) {
        this.stat = stat;
    }

    @Override
    public Optional<CappedValue> getValue(MountItem mountItem) {
        return Optional.ofNullable(mountItem.getMountInfo().stats().get(stat));
    }

    @Override
    public String getName() {
        return "mount" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, stat.name());
    }

    @Override
    public String getDisplayName() {
        return "Mount " + stat.getName();
    }

    @Override
    public String getDescription() {
        return getTranslation("description", stat.getName());
    }
}
