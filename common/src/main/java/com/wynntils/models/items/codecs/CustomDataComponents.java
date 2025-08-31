/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.codecs;

import com.mojang.serialization.Codec;
import com.wynntils.models.items.WynnItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;

@SuppressWarnings("ExtendsUtilityClass")
public final class CustomDataComponents extends DataComponents {
    private static final Codec<WynnItem> WYNN_ITEM_CODEC =
            WynnItemType.CODEC.dispatch(WynnItem::getType, WynnItemType::getCodec);
    public static final DataComponentType<WynnItem> WYNN_ITEM =
            register("wynn_item", builder -> builder.persistent(WYNN_ITEM_CODEC));

    private CustomDataComponents() {}

    public static void init() {
        // noop, used for calling clinit
    }
}
