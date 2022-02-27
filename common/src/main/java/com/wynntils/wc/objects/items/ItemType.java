/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.items;

import com.google.gson.annotations.SerializedName;

public enum ItemType {
    @SerializedName("SPEAR")
    Spear,
    @SerializedName("WAND")
    Wand,
    @SerializedName("DAGGER")
    Dagger,
    @SerializedName("BOW")
    Bow,
    @SerializedName("RELIK")
    Relik,
    @SerializedName("RING")
    Ring,
    @SerializedName("BRACELET")
    Bracelet,
    @SerializedName("NECKLACE")
    Necklace,
    @SerializedName("HELMET")
    Helmet,
    @SerializedName("CHESTPLATE")
    Chestplate,
    @SerializedName("LEGGINGS")
    Leggings,
    @SerializedName("BOOTS")
    Boots
}
