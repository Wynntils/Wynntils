/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.ServerItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemStackTransformer;

public class ServerItemStackModel extends Model {
    private static final ItemStackTransformer SERVER_TRANSFORMER =
            new ItemStackTransformer(WynnItemMatchers::isServerItem, ServerItemStack::new);

    public static void init() {
        Managers.ItemStackTransform.registerTransformer(SERVER_TRANSFORMER);
    }

    public static void disable() {
        Managers.ItemStackTransform.unregisterTransformer(SERVER_TRANSFORMER);
    }
}
