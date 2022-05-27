/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.profiles.item;

import com.wynntils.wc.objects.ClassType;

public enum ItemType {
    SPEAR(ClassType.Warrior),
    WAND(ClassType.Mage),
    DAGGER(ClassType.Assassin),
    BOW(ClassType.Archer),
    RELIK(ClassType.Shaman),
    RING(null),
    BRACELET(null),
    NECKLACE(null),
    HELMET(null),
    CHESTPLATE(null),
    LEGGINGS(null),
    BOOTS(null);

    private final ClassType classReq;

    ItemType(ClassType classReq) {
        this.classReq = classReq;
    }

    public ClassType getClassReq() {
        return classReq;
    }
}
