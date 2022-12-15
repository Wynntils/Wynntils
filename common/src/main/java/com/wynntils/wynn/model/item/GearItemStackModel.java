/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.GearItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemStackTransformer;

public final class GearItemStackModel extends Model {
    private static final ItemStackTransformer GEAR_TRANSFORMER =
            new ItemStackTransformer(WynnItemMatchers::isKnownGear, GearItemStack::new);

    @Override
    public void init() {
        Managers.ItemStackTransform.registerTransformer(GEAR_TRANSFORMER);
    }

    @Override
    public void disable() {
        Managers.ItemStackTransform.unregisterTransformer(GEAR_TRANSFORMER);
    }
}
