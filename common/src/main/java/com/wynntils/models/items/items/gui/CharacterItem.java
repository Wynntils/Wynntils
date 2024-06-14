/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.character.type.ClassType;

public class CharacterItem extends GuiItem {
    private final String className;
    private final int level;
    private final ClassType classType;
    private final boolean reskinned;

    public CharacterItem(String className, int level, ClassType classType, boolean reskinned) {
        this.className = className;
        this.level = level;
        this.classType = classType;
        this.reskinned = reskinned;
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

    @Override
    public String toString() {
        return "CharacterItem{" + "className='"
                + className + '\'' + ", level="
                + level + ", classType="
                + classType + ", reskinned="
                + reskinned + '}';
    }
}
