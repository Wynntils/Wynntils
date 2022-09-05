/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.render;

import net.minecraft.resources.ResourceLocation;

public enum Texture {
    BUBBLE_BAR("bars_bubbles.png", 256, 256),
    EMERALD_COUNT_BACKGROUND("emerald_count_background.png", 24, 24),
    EXPERIENCE_BAR("bars_exp.png", 256, 256),
    FEATURE_BUTTON("feature_button.png", 100, 12),
    FEATURE_LIST_BACKGROUND("feature_list.png", 118, 254),
    GEAR_ICONS("gear_icons.png", 64, 128),
    HEALTH_BAR("bars_health.png", 256, 256),
    HIGHLIGHT("highlight.png", 256, 256),
    ITEM_LOCK("item_lock.png", 16, 16),
    LOOTRUN_LINE("path_arrow.png", 16, 16),
    MANA_BAR("bars_mana.png", 256, 256),
    OVERLAY_SELECTION_GUI("overlay_selection_gui.png", 195, 256),
    SCROLL_BUTTON("scroll_circle.png", 9, 9),
    SEARCH_BAR("search_bar.png", 90, 10),
    SWITCH_OFF("switch_off.png", 40, 20),
    SWITCH_ON("switch_on.png", 40, 20),
    WYNNCRAFT_ICON("wynncraft_icon.png", 64, 64),

    // Map related
    GILDED_MAP_TEXTURES("map/gilded_map_textures.png", 262, 524),
    PAPER_MAP_TEXTURES("map/paper_map_textures.png", 256, 512),
    WYNN_MAP_TEXTURES("map/wynn_map_textures.png", 256, 256),
    MAP_POINTERS("map/map_pointers.png", 256, 256);

    private final ResourceLocation resource;
    private final int width;
    private final int height;

    Texture(String name, int width, int height) {
        this.resource = new ResourceLocation("wynntils", "textures/" + name);
        this.width = width;
        this.height = height;
    }

    public ResourceLocation resource() {
        return resource;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
