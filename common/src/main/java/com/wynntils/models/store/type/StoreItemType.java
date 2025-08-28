/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.store.type;

public enum StoreItemType {
    RANK("\uF018"),
    CRATES("\uF012"),
    SHARES("\uF01E"),
    BOMBS("\uF00F"),
    PETS("\uF015"),
    SILVERBULL("\uF01B"),
    TOKENS("\uF019");

    private final String titleCharacter;

    StoreItemType(String titleCharacter) {
        this.titleCharacter = titleCharacter;
    }

    public String getTitleCharacter() {
        return titleCharacter;
    }
}
