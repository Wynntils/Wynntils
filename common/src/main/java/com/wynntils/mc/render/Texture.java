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
    UNIVERSAL_BAR("universal_bar.png", 81, 16),
    WYNNCRAFT_ICON("wynncraft_icon.png", 64, 64),

    // Quest book related
    BACK_ARROW("back_arrow.png", 32, 9),
    BACKWARD_ARROW("backward_arrow.png", 36, 10),
    DIALOGUE_BUTTON("dialogue_button.png", 14, 11),
    FORWARD_ARROW("forward_arrow.png", 36, 10),
    QUEST_BOOK_BACKGROUND("quest_book.png", 339, 220),
    QUESTS_BUTTON("quests_button.png", 16, 16),
    QUEST_BOOK_SEARCH("quest_book_search.png", 133, 23),
    QUEST_BOOK_TITLE("quest_book_title.png", 168, 33),
    RELOAD_BUTTON("reload_button.png", 40, 20),

    // Quest State Icons
    QUEST_CANNOT_START("quest_cannot_start_icon.png", 7, 7),
    QUEST_CAN_START("quest_can_start_icon.png", 11, 7),
    QUEST_FINISHED("quest_finished_icon.png", 11, 7),
    QUEST_STARTED("quest_started_icon.png", 7, 7),

    // Icons
    MAP_ICON("map_icon.png", 21, 38),
    OVERLAYS_ICON("overlays_icon.png", 19, 38),
    QUEST_BOOK_ICON("quests_icon.png", 24, 34),
    SETTINGS_ICON("settings_icon.png", 17, 34),

    // Map related
    GILDED_MAP_TEXTURES("map/gilded_map_textures.png", 262, 524),
    PAPER_MAP_TEXTURES("map/paper_map_textures.png", 256, 512),
    WYNN_MAP_TEXTURES("map/wynn_map_textures.png", 256, 256),
    FULLSCREEN_MAP_BORDER("map/full_map_border.png", 510, 254),
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
