/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.mount;

import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.utils.EnumUtils;
import java.util.Optional;

public class MountTypeStatProvider extends BaseMountStatProvider<String> {
    @Override
    public Optional<String> getValue(MountItem mountItem) {
        return Optional.of(EnumUtils.toNiceString(mountItem.getMountType()));
    }
}
