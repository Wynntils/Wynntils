/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.wynntils.gui.render.Texture;
import java.util.Arrays;

public enum ServiceKind {
    ALCHEMISM_STATION("Alchemism Station"),
    ARMOUR_MERCHANT("Armour Merchant"),
    ARMOURING_STATION("Armouring Station"),
    BLACKSMITH("Blacksmith"),
    BOOTH_SHOP("Booth Shop"),
    COOKING_STATION("Cooking Station"),
    DUNGEON_SCROLL_MERCHANT("Dungeon Scroll Merchant"),
    EMERALD_MERCHANT("Emerald Merchant"),
    HOUSING_BALLOON("Housing Balloon"),
    ITEM_IDENTIFIER("Item Identifier"),
    JEWELING_STATION("Jeweling Station"),
    LIQUID_MERCHANT("Liquid Merchant"),
    PARTY_FINDER("Party Finder"),
    POTION_MERCHANT("Potion Merchant"),
    POWDER_MASTER("Powder Master"),
    SCRIBING_STATION("Scribing Station"),
    SCROLL_MERCHANT("Scroll Merchant"),
    TAILORING_STATION("Tailoring Station"),
    TOOL_MERCHANT("Tool Merchant"),
    TRADE_MARKET("Trade Market"),
    WEAPON_MERCHANT("Weapon Merchant"),
    WEAPONSMITHING_STATION("Weaponsmithing Station"),
    WOODWORKING_STATION("Woodworking Station");

    private final String name;

    ServiceKind(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Texture getIcon() {
        // FIXME: implement
        return null;
    }

    public static ServiceKind fromString(String str) {
        return Arrays.stream(values())
                .filter(kind -> kind.getName().equals(str))
                .findFirst()
                .orElse(null);
    }
}
