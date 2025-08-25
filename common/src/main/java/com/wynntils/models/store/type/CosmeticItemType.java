/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.store.type;

public enum CosmeticItemType {
    HELMET_COSMETIC("\uF011"),
    CHESTPLATE_COSMETIC("\uF014"),
    LEGGINGS_COSMETIC("\uF017"),
    BOOTS_COSMETIC("\uF01A"),
    WEAPON_SKIN("\uF023"),
    GLINT("\uF01D"),
    EFFECT("\uF013"),
    DISGUISE("\uF021"),
    MOUNT_SKIN("\uF020"),
    CRATE("\uF012"),
    PET("\uF015"),
    CONSUMABLE("\uF010"),
    EMOTE("\uF016");

    private final String titleCharacter;

    CosmeticItemType(String titleCharacter) {
        this.titleCharacter = titleCharacter;
    }

    public String getTitleCharacter() {
        return titleCharacter;
    }
}
