/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

public class RuneItem extends GameItem {
    private final RuneType type;

    public RuneItem(RuneType type) {
        this.type = type;
    }

    public RuneType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "RuneItem{" + "type=" + type + '}';
    }

    public enum RuneType {
        AZ,
        NII,
        UTH,
        TOL
    }
}
