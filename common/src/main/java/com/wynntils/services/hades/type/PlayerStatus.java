/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.hades.type;

import com.wynntils.utils.type.CappedValue;

public record PlayerStatus(
        float x,
        float y,
        float z,
        CappedValue health,
        CappedValue mana,
        String helmet,
        String chestplate,
        String leggings,
        String boots,
        String ringOne,
        String ringTwo,
        String bracelet,
        String necklace,
        String heldItem) {
    public PlayerStatus(float x, float y, float z, CappedValue health, CappedValue mana) {
        this(x, y, z, health, mana, "", "", "", "", "", "", "", "", "");
    }
}
