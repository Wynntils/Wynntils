/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.character.type.CharacterGamemode;
import com.wynntils.models.character.type.ClassType;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class CharacterCreationItem extends GuiItem {
    private final ClassType classType;
    private final boolean reskinned;
    private final Set<CharacterGamemode> gamemodes;

    public CharacterCreationItem(ClassType classType, boolean reskinned, Set<CharacterGamemode> gamemodes) {
        this.classType = classType;
        this.reskinned = reskinned;
        this.gamemodes = gamemodes == null || gamemodes.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(gamemodes));
    }

    public ClassType getClassType() {
        return classType;
    }

    public boolean isReskinned() {
        return reskinned;
    }

    public Set<CharacterGamemode> getGamemodes() {
        return gamemodes;
    }

    @Override
    public String toString() {
        return "CharacterCreationItem{" + ", classType="
                + classType + ", reskinned="
                + reskinned + ", gamemodes="
                + gamemodes + '}';
    }
}
