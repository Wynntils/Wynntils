/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.render;

import net.minecraft.resources.ResourceLocation;

public enum Texture {
    BUBBLE_BAR("bars_bubbles.png", 256, 256),
    EMERALD_COUNT_BACKGROUND("emerald_count_background.png", 24, 24),
    EXPERIENCE_BAR("bars_exp.png", 256, 256),
    FAVORITE("favorite.png", 18, 18),
    GEAR_ICONS("gear_icons.png", 64, 128),
    GEAR_VIEWER_BACKGROUND("gear_viewer_background.png", 103, 92),
    HEALTH_BAR("bars_health.png", 256, 256),
    HIGHLIGHT("highlight.png", 256, 256),
    ITEM_LOCK("item_lock.png", 16, 16),
    LOOTRUN_LINE("path_arrow.png", 16, 16),
    MANA_BAR("bars_mana.png", 256, 256),
    OVERLAY_SELECTION_GUI("overlay_selection_gui.png", 195, 256),
    UNIVERSAL_BAR("universal_bar.png", 81, 16),
    WYNNCRAFT_ICON("wynncraft_icon.png", 64, 64),

    // Settings Screen
    SETTING_BACKGROUND("book.png", 400, 220),
    SETTING_SCROLL_AREA("book_scrollarea.png", 168, 175),
    SETTING_SCROLL_BUTTON("book_scroll_button.png", 5, 18),

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
    SORT_ALPHABETICALLY("sort_alphabetical.png", 23, 46),
    SORT_DISTANCE("sort_distance.png", 23, 46),
    SORT_LEVEL("sort_level.png", 23, 46),

    // Quest State Icons
    QUEST_CANNOT_START("quest_cannot_start_icon.png", 7, 7),
    QUEST_CAN_START("quest_can_start_icon.png", 11, 7),
    QUEST_FINISHED("quest_finished_icon.png", 11, 7),
    QUEST_STARTED("quest_started_icon.png", 7, 7),

    // Icons
    GUIDES_ICON("guides_icon.png", 18, 34),
    MAP_ICON("map_icon.png", 21, 38),
    LOOTRUN_ICON("lootrun_icon.png", 16, 28),
    OVERLAYS_ICON("overlays_icon.png", 19, 38),
    QUEST_BOOK_ICON("quests_icon.png", 24, 34),
    SETTINGS_ICON("settings_icon.png", 17, 34),

    // Map related
    CIRCLE_MASK("map/circle_mask.png", 256, 256),
    FULLSCREEN_MAP_BORDER("map/map_border/full_map_border.png", 510, 254),
    GILDED_MAP_TEXTURES("map/map_border/gilded_map_textures.png", 262, 524),
    MAP_POINTERS("map/map_pointers.png", 256, 256),
    PAPER_MAP_TEXTURES("map/map_border/paper_map_textures.png", 256, 512),
    WYNN_MAP_TEXTURES("map/map_border/wynn_map_textures.png", 126, 256),

    // Map Icons
    ALCHEMIST_STATION("map/map_icons/alchemist_station.png", 16, 19),
    ARMOR_MERCHANT("map/map_icons/armor_merchant.png", 17, 17),
    ARMORING_STATION("map/map_icons/armoring_station.png", 17, 17),
    BLACKSMITH("map/map_icons/blacksmith.png", 18, 18),
    BOOTH_SHOP("map/map_icons/booth_shop.png", 19, 17),
    COOKING_STATION("map/map_icons/cooking_station.png", 18, 16),
    DUNGEON_SCROLL_MERCHANT("map/map_icons/dungeon_scroll_merchant.png", 18, 17),
    EMERALD_MERCHANT("map/map_icons/emerald_merchant.png", 17, 18),
    HOUSING_BALLOON("map/map_icons/housing_balloon.png", 13, 23),
    ITEM_IDENTIFIER("map/map_icons/item_identifier.png", 18, 17),
    JEWELING_STATION("map/map_icons/jeweling_station.png", 17, 16),
    LIQUID_MERCHANT("map/map_icons/liquid_merchant.png", 15, 17),
    LOST_SPIRIT("map/map_icons/lost_spirit.png", 16, 16),
    PARTY_FINDER("map/map_icons/party_finder.png", 18, 18),
    POTION_MERCHANT("map/map_icons/potion_merchant.png", 16, 18),
    POWDER_MASTER("map/map_icons/powder_master.png", 17, 17),
    SCRIBING_STATION("map/map_icons/scribing_station.png", 18, 17),
    SCROLL_MERCHANT("map/map_icons/scroll_merchant.png", 18, 17),
    TAILORING_STATION("map/map_icons/tailoring_station.png", 18, 15),
    TOOL_MERCHANT("map/map_icons/tool_merchant.png", 17, 15),
    TRADE_MARKET("map/map_icons/trade_market.png", 18, 18),
    WAYPOINT("map/map_icons/waypoint.png", 14, 18),
    WEAPON_MERCHANT("map/map_icons/weapon_merchant.png", 18, 15),
    WEAPONSMITHING_STATION("map/map_icons/weaponsmithing_station.png", 18, 15),
    WOODWORKING_STATION("map/map_icons/woodworking_station.png", 17, 15),

    // Character Selection UI
    ADD_BUTTON("character_selection_ui/add_button.png", 14, 28),
    BACKGROUND_SPLASH("character_selection_ui/background_splash.png", 1920, 1027),
    CHALLENGES_ICON("character_selection_ui/challenges.png", 9, 14),
    CHARACTER_BUTTON("character_selection_ui/character_button.png", 104, 64),
    CHARACTER_INFO("character_selection_ui/character_info.png", 123, 38),
    EDIT_BUTTON("character_selection_ui/edit_button.png", 6, 16),
    LIST_BACKGROUND("character_selection_ui/list_background.png", 118, 254),
    PLAY_BUTTON("character_selection_ui/play_button.png", 79, 76),
    QUESTS_ICON("character_selection_ui/quests.png", 16, 16),
    REMOVE_BUTTON("character_selection_ui/remove_button.png", 14, 28),
    CHARACTER_SELECTION_SCROLL_BUTTON("character_selection_ui/scroll_button.png", 7, 17),
    SOUL_POINT_ICON("character_selection_ui/soul_point.png", 10, 16),
    XP_BAR("character_selection_ui/xp_bar.png", 100, 12);

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
