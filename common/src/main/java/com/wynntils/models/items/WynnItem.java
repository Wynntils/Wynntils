/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items;

import com.mojang.serialization.Codec;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.codecs.WynnItemType;
import net.minecraft.world.item.ItemStack;

public abstract class WynnItem implements ItemAnnotation {
    public static final Codec<WynnItem> WYNN_ITEM_CODEC =
            WynnItemType.CODEC.dispatch(WynnItem::getType, WynnItemType::getCodec);

    private final WynnItemData data = new WynnItemData();

    public WynnItemData getData() {
        return data;
    }

    // FIXME: Make abstract!
    public WynnItemType getType() {
        return WynnItemType.FALLBACK;
    }

    @Override
    public String toString() {
        return "WynnItem{}";
    }

    @Override
    public void onUpdate(ItemStack itemStack) {
        data.clearAll();
        data.store(WynnItemData.ITEMSTACK_KEY, itemStack);
    }
}
