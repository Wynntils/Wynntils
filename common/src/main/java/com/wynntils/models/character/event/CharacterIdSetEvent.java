/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.event;

import net.neoforged.bus.api.Event;

public class CharacterIdSetEvent extends Event {
    private final String characterId;

    public CharacterIdSetEvent(String characterId) {
        this.characterId = characterId;
    }

    public String getCharacterId() {
        return characterId;
    }
}
