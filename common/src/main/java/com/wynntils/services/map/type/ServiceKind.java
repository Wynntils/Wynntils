/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.type;

import com.wynntils.utils.render.Texture;
import java.util.Arrays;

public enum ServiceKind {
    ALCHEMISM_STATION("Alchemism Station", Texture.ALCHEMIST_STATION, "wynntils:service:profession:alchemism"),
    ARMOUR_MERCHANT("Armour Merchant", Texture.ARMOR_MERCHANT, "wynntils:service:merchant:armor"),
    ARMOURING_STATION("Armouring Station", Texture.ARMORING_STATION, "wynntils:service:profession:armoring"),
    BLACKSMITH("Blacksmith", Texture.BLACKSMITH, "wynntils:service:blacksmith"),
    BOOTH_SHOP("Booth Shop", Texture.BOOTH_SHOP, "wynntils:service:booth-shop"),
    COOKING_STATION("Cooking Station", Texture.COOKING_STATION, "wynntils:service:profession:cooking"),
    DUNGEON_SCROLL_MERCHANT(
            "Dungeon Scroll Merchant", Texture.DUNGEON_SCROLL_MERCHANT, "wynntils:service:merchant:dungeon-scroll"),
    EMERALD_MERCHANT("Emerald Merchant", Texture.EMERALD_MERCHANT, "wynntils:service:merchant:emerald"),
    FAST_TRAVEL("Fast Travel", Texture.FAST_TRAVEL, "wynntils:service:fast-travel"),
    HOUSING_BALLOON("Housing Balloon", Texture.HOUSING_BALLOON, "wynntils:service:housing-balloon"),
    ITEM_IDENTIFIER("Item Identifier", Texture.ITEM_IDENTIFIER, "wynntils:service:identifier"),
    JEWELING_STATION("Jeweling Station", Texture.JEWELING_STATION, "wynntils:service:profession:jeweling"),
    LIQUID_MERCHANT("Liquid Merchant", Texture.LIQUID_MERCHANT, "wynntils:service:merchant:liquid-emerald"),
    PARTY_FINDER("Party Finder", Texture.PARTY_FINDER, "wynntils:service:party-finder"),
    POTION_MERCHANT("Potion Merchant", Texture.POTION_MERCHANT, "wynntils:service:merchant:potion"),
    POWDER_MASTER("Powder Master", Texture.POWDER_MASTER, "wynntils:service:powder-master"),
    SCRIBING_STATION("Scribing Station", Texture.SCRIBING_STATION, "wynntils:service:profession:scribing"),
    SCROLL_MERCHANT("Scroll Merchant", Texture.SCROLL_MERCHANT, "wynntils:service:merchant:scroll"),
    SEASKIPPER("Seaskipper", Texture.SEASKIPPER, "wynntils:service:seaskipper"),
    TAILORING_STATION("Tailoring Station", Texture.TAILORING_STATION, "wynntils:service:profession:tailoring"),
    TOOL_MERCHANT("Tool Merchant", Texture.TOOL_MERCHANT, "wynntils:service:merchant:tool"),
    TRADE_MARKET("Trade Market", Texture.TRADE_MARKET, "wynntils:service:trade-market"),
    WEAPON_MERCHANT("Weapon Merchant", Texture.WEAPON_MERCHANT, "wynntils:service:merchant:weapon"),
    WEAPONSMITHING_STATION(
            "Weaponsmithing Station", Texture.WEAPONSMITHING_STATION, "wynntils:service:profession:weaponsmithing"),
    WOODWORKING_STATION("Woodworking Station", Texture.WOODWORKING_STATION, "wynntils:service:profession:woodworking");

    private final String name;
    private final Texture texture;
    private final String categoryId;

    ServiceKind(String name, Texture texture, String categoryId) {
        this.name = name;
        this.texture = texture;
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public Texture getIcon() {
        return texture;
    }

    public static ServiceKind fromString(String str) {
        return Arrays.stream(values())
                .filter(kind -> kind.getName().equals(str))
                .findFirst()
                .orElse(null);
    }
}
