/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.UnidentifiedItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemStackTransformer;

public final class UnidentifiedItemStackModel extends Model {
    private static final ItemStackTransformer UNIDENTIFIED_TRANSFORMER =
            new ItemStackTransformer(WynnItemMatchers::isUnidentified, UnidentifiedItemStack::new);

    @Override
    public void init() {
        Managers.ItemStackTransform.registerTransformer(UNIDENTIFIED_TRANSFORMER);
    }

    @Override
    public void disable() {
        Managers.ItemStackTransform.unregisterTransformer(UNIDENTIFIED_TRANSFORMER);
    }
}
