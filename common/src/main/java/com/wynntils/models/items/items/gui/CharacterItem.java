/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.character.type.CharacterGamemode;
import com.wynntils.models.character.type.ClassType;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class CharacterItem extends GuiItem {
    private final String className;
    private final int level;
    private final ClassType classType;
    private final boolean reskinned;
    private final Set<CharacterGamemode> gamemodes;
    private final boolean fromCreation;

    public CharacterItem(
            String className,
            int level,
            ClassType classType,
            boolean reskinned,
            Set<CharacterGamemode> gamemodes,
            boolean fromCreation) {
        this.className = className;
        this.level = level;
        this.classType = classType;
        this.reskinned = reskinned;
        this.gamemodes = gamemodes == null || gamemodes.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(gamemodes));
        this.fromCreation = fromCreation;
    }

    public String getClassName() {
        return className;
    }

    public int getLevel() {
        return level;
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

    public boolean isFromCreation() {
        return fromCreation;
    }

    @Override
    public String toString() {
        return "CharacterItem{" + "className='"
                + className + '\'' + ", level="
                + level + ", classType="
                + classType + ", reskinned="
                + reskinned + ", gamemodes="
                + gamemodes + ", fromCreation="
                + fromCreation + '}';
    }
}
