/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

public class Label {
    private String name;
    private int x;
    private int z;
    private int layer;
    private String level;

    public Label(String name, int x, int z, int layer, String level) {
        this.name = name;
        this.x = x;
        this.z = z;
        this.layer = layer;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    /**
     * The relative importance of this place
     */
    public LabelLayer getLayer() {
        return LabelLayer.values()[layer - 1];
    }

    /**
     * The recommended minimum combat level for visiting this place
     */
    public String getLevel() {
        if (level == null) return "";

        return level;
    }

    public enum LabelLayer {
        PROVINCE,
        CITY,
        TOWN_OR_PLACE
    }
}
