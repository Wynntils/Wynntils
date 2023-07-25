/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.type;

import com.wynntils.utils.render.Texture;
import java.util.Arrays;

public enum ServiceKind {
    ALCHEMISM_STATION("Alchemism Station", Texture.ALCHEMIST_STATION),
    ARMOUR_MERCHANT("Armour Merchant", Texture.ARMOR_MERCHANT),
    ARMOURING_STATION("Armouring Station", Texture.ARMORING_STATION),
    BLACKSMITH("Blacksmith", Texture.BLACKSMITH),
    BOOTH_SHOP("Booth Shop", Texture.BOOTH_SHOP),
    COOKING_STATION("Cooking Station", Texture.COOKING_STATION),
    DUNGEON_SCROLL_MERCHANT("Dungeon Scroll Merchant", Texture.DUNGEON_SCROLL_MERCHANT),
    EMERALD_MERCHANT("Emerald Merchant", Texture.EMERALD_MERCHANT),
    FAST_TRAVEL("Fast Travel", Texture.FAST_TRAVEL),
    HOUSING_BALLOON("Housing Balloon", Texture.HOUSING_BALLOON),
    ITEM_IDENTIFIER("Item Identifier", Texture.ITEM_IDENTIFIER),
    JEWELING_STATION("Jeweling Station", Texture.JEWELING_STATION),
    LIQUID_MERCHANT("Liquid Merchant", Texture.LIQUID_MERCHANT),
    PARTY_FINDER("Party Finder", Texture.PARTY_FINDER),
    POTION_MERCHANT("Potion Merchant", Texture.POTION_MERCHANT),
    POWDER_MASTER("Powder Master", Texture.POWDER_MASTER),
    SCRIBING_STATION("Scribing Station", Texture.SCRIBING_STATION),
    SCROLL_MERCHANT("Scroll Merchant", Texture.SCROLL_MERCHANT),
    SEASKIPPER("Seaskipper", Texture.SEASKIPPER),
    TAILORING_STATION("Tailoring Station", Texture.TAILORING_STATION),
    TOOL_MERCHANT("Tool Merchant", Texture.TOOL_MERCHANT),
    TRADE_MARKET("Trade Market", Texture.TRADE_MARKET),
    WEAPON_MERCHANT("Weapon Merchant", Texture.WEAPON_MERCHANT),
    WEAPONSMITHING_STATION("Weaponsmithing Station", Texture.WEAPONSMITHING_STATION),
    WOODWORKING_STATION("Woodworking Station", Texture.WOODWORKING_STATION);

    private final String name;
    private final Texture texture;

    ServiceKind(String name, Texture texture) {
        this.name = name;
        this.texture = texture;
    }

    public String getName() {
        return name;
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
