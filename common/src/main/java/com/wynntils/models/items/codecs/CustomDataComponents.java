/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.codecs;

import com.wynntils.models.items.WynnItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;

@SuppressWarnings("ExtendsUtilityClass")
public final class CustomDataComponents extends DataComponents {
    public static final DataComponentType<WynnItem> WYNN_ITEM =
            register("wynn_item", builder -> builder.persistent(WynnItem.WYNN_ITEM_CODEC));

    private CustomDataComponents() {}

    public static void init() {
        // noop, used for calling clinit
    }
}
