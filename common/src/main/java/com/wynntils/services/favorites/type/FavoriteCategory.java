/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.favorites.type;

import com.wynntils.utils.colors.CustomColor;
import java.util.Set;
import java.util.TreeSet;

public final class FavoriteCategory {
    public static final String DEFAULT_NAME = "Favorites";

    private static final CustomColor DISABLED_COLOR = new CustomColor(0, 0, 0, 0);

    private String name;
    private Set<String> items = new TreeSet<>();

    public static final int NEVER_CLOSE = -1;

    // The number of attempts needed to close a loot chest, 0 to not block closing, or NEVER_CLOSE
    private int lootChestCloseOverride = 3;

    private FavoriteIcon icon = FavoriteIcon.STAR;
    // Position and size in pixels, relative to the top left corner of the 16x16 slot
    private int iconX = 10;
    private int iconY = 0;
    private int iconSize = 9;
    private CustomColor iconColor = new CustomColor(255, 224, 46);
    private CustomColor backgroundColor = DISABLED_COLOR;
    private CustomColor overlayColor = DISABLED_COLOR;
    private CustomColor borderColor = DISABLED_COLOR;

    private FavoriteCategory() {}

    public FavoriteCategory(String name) {
        this.name = name;
    }

    public FavoriteCategory copy() {
        FavoriteCategory copy = new FavoriteCategory(name);
        copy.items.addAll(items);
        copy.lootChestCloseOverride = lootChestCloseOverride;
        copy.icon = icon;
        copy.iconX = iconX;
        copy.iconY = iconY;
        copy.iconSize = iconSize;
        copy.iconColor = iconColor;
        copy.backgroundColor = backgroundColor;
        copy.overlayColor = overlayColor;
        copy.borderColor = borderColor;
        return copy;
    }

    public boolean isValid() {
        return name != null
                && !name.isBlank()
                && items != null
                && icon != null
                && iconColor != null
                && backgroundColor != null
                && overlayColor != null
                && borderColor != null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getItems() {
        return items;
    }

    public int getLootChestCloseOverride() {
        return lootChestCloseOverride;
    }

    public void setLootChestCloseOverride(int lootChestCloseOverride) {
        this.lootChestCloseOverride = lootChestCloseOverride;
    }

    public FavoriteIcon getIcon() {
        return icon;
    }

    public void setIcon(FavoriteIcon icon) {
        this.icon = icon;
    }

    public int getIconX() {
        return iconX;
    }

    public void setIconX(int iconX) {
        this.iconX = iconX;
    }

    public int getIconY() {
        return iconY;
    }

    public void setIconY(int iconY) {
        this.iconY = iconY;
    }

    public int getIconSize() {
        return iconSize;
    }

    public void setIconSize(int iconSize) {
        this.iconSize = iconSize;
    }

    public CustomColor getIconColor() {
        return iconColor;
    }

    public void setIconColor(CustomColor iconColor) {
        this.iconColor = iconColor;
    }

    public CustomColor getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(CustomColor backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public CustomColor getOverlayColor() {
        return overlayColor;
    }

    public void setOverlayColor(CustomColor overlayColor) {
        this.overlayColor = overlayColor;
    }

    public CustomColor getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(CustomColor borderColor) {
        this.borderColor = borderColor;
    }
}
