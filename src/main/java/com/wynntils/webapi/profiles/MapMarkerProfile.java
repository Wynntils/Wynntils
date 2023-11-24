/*
 *  * Copyright © Wynntils - 2018 - 2022.
 */

package com.wynntils.webapi.profiles;

import com.wynntils.Reference;
import com.wynntils.core.utils.StringUtils;

import java.util.*;

public class MapMarkerProfile extends LocationProfile {

    private static final Map<String, String> MAPMARKERNAME_TRANSLATION;

    static {
        Map<String, String> mmn_t = new HashMap<>();
        mmn_t.put("Content_Dungeon", "Dungeons");
        mmn_t.put("Content_CorruptedDungeon", "Corrupted Dungeons");
        mmn_t.put("Content_BossAltar", "Boss Altar");
        mmn_t.put("Content_Raid", "Raid");
        mmn_t.put("Merchant_Accessory", "Accessory Merchant");
        mmn_t.put("Merchant_Armour", "Armour Merchant");
        mmn_t.put("Merchant_Dungeon", "Dungeon Merchant");
        mmn_t.put("Merchant_Horse", "Horse Merchant");
        mmn_t.put("Merchant_KeyForge", "Key Forge Merchant");
        mmn_t.put("Merchant_Liquid", "LE Merchant");
        mmn_t.put("Merchant_Potion", "Potion Merchant");
        mmn_t.put("Merchant_Powder", "Powder Merchant");
        mmn_t.put("Merchant_Scroll", "Scroll Merchant");
        mmn_t.put("Merchant_Seasail", "Seasail Merchant");
        mmn_t.put("Merchant_Weapon", "Weapon Merchant");
        mmn_t.put("NPC_Blacksmith", "Blacksmith");
        mmn_t.put("NPC_GuildMaster", "Guild Master");
        mmn_t.put("NPC_ItemIdentifier", "Item Identifier");
        mmn_t.put("NPC_PowderMaster", "Powder Master");
        mmn_t.put("Special_FastTravel", "Fast Travel");
        mmn_t.put("tnt", "TNT Merchant");
        mmn_t.put("painting", "Art Merchant");
        mmn_t.put("Ore_Refinery", "Ore Refinery");
        mmn_t.put("Fish_Refinery", "Fish Refinery");
        mmn_t.put("Wood_Refinery", "Wood Refinery");
        mmn_t.put("Crop_Refinery", "Crop Refinery");
        mmn_t.put("NPC_TradeMarket", "Marketplace");
        mmn_t.put("Content_Quest", "Quests");
        mmn_t.put("Content_Miniquest", "Mini-Quests");
        mmn_t.put("Special_Rune", "Runes");
        mmn_t.put("Special_RootsOfCorruption", "Nether Portal");
        mmn_t.put("Content_UltimateDiscovery", "Ultimate Discovery");
        mmn_t.put("Content_Cave", "Caves");
        mmn_t.put("Content_GrindSpot", "Grind Spots");
        mmn_t.put("Merchant_Other", "Other Merchants");
        mmn_t.put("Special_LightRealm", "Light's Secret");
        mmn_t.put("Merchant_Emerald", "Emerald Merchant");
        mmn_t.put("Profession_Weaponsmithing", "Weaponsmithing Station");
        mmn_t.put("Profession_Armouring", "Armouring Station");
        mmn_t.put("Profession_Alchemism", "Alchemism Station");
        mmn_t.put("Profession_Jeweling", "Jeweling Station");
        mmn_t.put("Profession_Tailoring", "Tailoring Station");
        mmn_t.put("Profession_Scribing", "Scribing Station");
        mmn_t.put("Profession_Cooking", "Cooking Station");
        mmn_t.put("Profession_Woodworking", "Woodworking Station");
        mmn_t.put("Merchant_Tool", "Tool Merchant");
        mmn_t.put("Special_SeaskipperFastTravel", "Seaskipper Fast Travel");
        mmn_t.put("Special_HousingAirBalloon", "Housing Air Balloon");

        MAPMARKERNAME_TRANSLATION = Collections.unmodifiableMap(mmn_t);
    }

    private static final Map<String, String> MAPMARKERNAME_REVERSE_TRANSLATION;

    static {
        Map<String, String> mmn_r_t = new HashMap<>(MAPMARKERNAME_TRANSLATION.size());
        MAPMARKERNAME_TRANSLATION.forEach((k, v) -> mmn_r_t.put(v, k));
        MAPMARKERNAME_REVERSE_TRANSLATION = Collections.unmodifiableMap(mmn_r_t);
    }

    private static final Set<String> IGNORED_MARKERS;

    static {
        Set<String> ignored = new HashSet<>();
        Collections.singletonList("Content_CorruptedDungeon").forEach(s -> {
            ignored.add(s);
            ignored.add(MAPMARKERNAME_TRANSLATION.get(s));
        });
        IGNORED_MARKERS = Collections.unmodifiableSet(ignored);
    }

    int y;
    String icon;

    public MapMarkerProfile(String name, int x, int y, int z, String icon) {
        super(name, x, z);
        this.y = y;
        this.icon = icon;
        ensureNormalized();
    }

    public boolean isIgnored() {
        return IGNORED_MARKERS.contains(this.name);
    }

    public int getY() {
        return y;
    }

    public String getIcon() {
        return icon;
    }

    public void ensureNormalized() {
        if (name != null) name = StringUtils.normalizeBadString(name);
        icon = icon.replace(".png", "");
    }

    @Override
    public String getTranslatedName() {
        return MAPMARKERNAME_TRANSLATION.get(icon);
    }

    public static String getReverseTranslation(String icon) {
        String reverse = MAPMARKERNAME_REVERSE_TRANSLATION.getOrDefault(icon, icon);
        if (!MAPMARKERNAME_TRANSLATION.containsKey(reverse)) {
            throw new RuntimeException("getReverseTranslation(\"" + icon + "\"): invalid name");
        }
        return reverse;
    }

    /*
     * Debug function run in developmentEnvironment to verify consistency
     */
    public static void validateIcons(Map<String, Boolean> enabledIcons) {
        for (String icon : MAPMARKERNAME_TRANSLATION.values()) {
            if (IGNORED_MARKERS.contains(icon)) continue;
            if (!enabledIcons.containsKey(icon)) Reference.LOGGER.warn("Missing option for icon \"" + icon + "\"");
        }
        for (String icon : enabledIcons.keySet()) {
            if (IGNORED_MARKERS.contains(icon)) continue;
            if (!MAPMARKERNAME_REVERSE_TRANSLATION.containsKey(icon)) Reference.LOGGER.warn("Missing translation for \"" + icon + "\"");
        }
    }
}
