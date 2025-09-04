/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.codecs;

import com.wynntils.core.WynntilsMod;
import com.wynntils.models.items.ItemModel;
import com.wynntils.models.items.WynnItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("ExtendsUtilityClass")
public final class CustomDataComponents extends DataComponents {
    //    public static final DataComponentType<WynnItem> WYNN_ITEM =
    //            register(WynntilsMod.MOD_ID + "wynn_item", builder -> builder.persistent(WynnItem.WYNN_ITEM_CODEC));
    public static final DataComponentType<WynnItem> WYNN_ITEM = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath(WynntilsMod.MOD_ID, "wynn_item"),
            DataComponentType.<WynnItem>builder()
                    .networkSynchronized(StreamCodec.unit(ItemModel.FallbackItem.INSTANCE))
                    .build());

    private CustomDataComponents() {}

    public static void init() {
        // noop, used for calling clinit
    }
}
