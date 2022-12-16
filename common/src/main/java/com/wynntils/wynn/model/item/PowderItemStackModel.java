/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.PowderItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemStackTransformer;

public final class PowderItemStackModel extends Model {
    private static final ItemStackTransformer POWDER_TRANSFORMER =
            new ItemStackTransformer(WynnItemMatchers::isPowder, PowderItemStack::new);

    @Override
    public void init() {
        Managers.ItemStackTransform.registerTransformer(POWDER_TRANSFORMER);
    }

    @Override
    public void disable() {
        Managers.ItemStackTransform.unregisterTransformer(POWDER_TRANSFORMER);
    }
}
