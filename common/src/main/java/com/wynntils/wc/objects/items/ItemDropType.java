/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.items;

import com.google.gson.annotations.SerializedName;

public enum ItemDropType {
    @SerializedName("NEVER")
    Never, // quests or merchants
    @SerializedName("LOOTCHEST")
    Lootchest, // lootchests
    @SerializedName("NORMAL")
    Normal // mobs
}
