/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.profiles.item;

import com.google.gson.annotations.SerializedName;
import com.wynntils.wc.objects.ClassType;

public enum ItemType {
    @SerializedName("SPEAR")
    Spear(ClassType.Warrior),
    @SerializedName("WAND")
    Wand(ClassType.Mage),
    @SerializedName("DAGGER")
    Dagger(ClassType.Assassin),
    @SerializedName("BOW")
    Bow(ClassType.Archer),
    @SerializedName("RELIK")
    Relik(ClassType.Shaman),
    @SerializedName("RING")
    Ring(null),
    @SerializedName("BRACELET")
    Bracelet(null),
    @SerializedName("NECKLACE")
    Necklace(null),
    @SerializedName("HELMET")
    Helmet(null),
    @SerializedName("CHESTPLATE")
    Chestplate(null),
    @SerializedName("LEGGINGS")
    Leggings(null),
    @SerializedName("BOOTS")
    Boots(null);

    private final ClassType classReq;

    ItemType(ClassType classReq) {
        this.classReq = classReq;
    }

    public ClassType getClassReq() {
        return classReq;
    }
}
