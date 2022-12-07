/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.parsers;

import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wynn.item.EmeraldPouchItemStack;
import com.wynntils.wynn.item.GearItemStack;
import com.wynntils.wynn.item.IngredientItemStack;
import com.wynntils.wynn.item.PowderItemStack;
import com.wynntils.wynn.model.ItemProfilesManager;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Tests if an item is a certain wynncraft item */
public final class WynnItemMatchers {
    private static final Pattern SERVER_ITEM_PATTERN = Pattern.compile("§[baec]§lWorld (\\d+)(§3 \\(Recommended\\))?");
    private static final Pattern CONSUMABLE_PATTERN = Pattern.compile("(.+)\\[([0-9]+)/([0-9]+)]");
    private static final Pattern COSMETIC_PATTERN =
            Pattern.compile("(Common|Rare|Epic|Godly|\\|\\|\\| Black Market \\|\\|\\|) Reward");
    private static final Pattern ITEM_RARITY_PATTERN =
            Pattern.compile("(Normal|Set|Unique|Rare|Legendary|Fabled|Mythic)( Raid)? (Item|Reward).*");
    private static final Pattern DURABILITY_PATTERN = Pattern.compile("\\[(\\d+)/(\\d+) Durability\\]");
    private static final Pattern POWDER_PATTERN =
            Pattern.compile("§[2ebcf8].? ?(Earth|Thunder|Water|Fire|Air) Powder ([IV]{1,3})");
    private static final Pattern EMERALD_POUCH_TIER_PATTERN = Pattern.compile("Emerald Pouch \\[Tier ([IVX]{1,4})\\]");
    private static final Pattern SKILL_POINT_NAME_PATTERN = Pattern.compile("^§dUpgrade your §[2ebcf]. \\w+?§d skill$");

    private static final Pattern SKILL_ICON_PATTERN =
            Pattern.compile(".*?§([2ebcf])([✤✦❉✹❋]) (Strength|Dexterity|Intelligence|Defence|Agility).*?");
    private static final Pattern TELEPORT_SCROLL_PATTERN = Pattern.compile(".*§b(.*) Teleport Scroll");
    private static final Pattern TELEPORT_LOCATION_PATTERN = Pattern.compile("- Teleports to: (.*)");
    private static final Pattern DUNGEON_KEY_PATTERN = Pattern.compile("(?:§.)*(?:Broken )?(?:Corrupted )?(.+) Key");
    private static final Pattern AMPLIFIER_PATTERN = Pattern.compile("§bCorkian Amplifier (I{1,3})");
    private static final Pattern INGREDIENT_OR_MATERIAL_PATTERN = Pattern.compile("(.*) \\[✫✫✫\\]");

    private static final Pattern GATHERING_TOOL_PATTERN =
            Pattern.compile("[ⒸⒷⓀⒿ] Gathering (Axe|Rod|Scythe|Pickaxe) T(\\d+)");

    public static boolean isSoulPoint(ItemStack itemStack) {
        return !itemStack.isEmpty()
                && (itemStack.getItem() == Items.NETHER_STAR || itemStack.getItem() == Items.SNOW)
                && itemStack.getDisplayName().getString().contains("Soul Point");
    }

    public static boolean isIntelligenceSkillPoints(ItemStack itemStack) {
        if (itemStack.getItem() != Items.BOOK) return false;

        Component name = itemStack.getHoverName();
        String unformattedLoreLine = ComponentUtils.getCoded(name);
        return unformattedLoreLine.equals("§dUpgrade your §b❉ Intelligence§d skill");
    }

    public static boolean isServerItem(ItemStack itemStack) {
        return serverItemMatcher(itemStack.getHoverName()).matches();
    }

    public static boolean isHealingPotion(ItemStack itemStack) {
        if (!isConsumable(itemStack)) return false;
        if (itemStack.getHoverName().getString().contains(ChatFormatting.LIGHT_PURPLE + "Potions of Healing")
                || itemStack.getHoverName().getString().contains(ChatFormatting.RED + "Potion of Healing")) return true;

        boolean isCraftedPotion = false;
        boolean hasHealEffect = false;
        ListTag lore = ItemUtils.getLoreTagElseEmpty(itemStack);
        for (Tag tag : lore) {
            String unformattedLoreLine = ComponentUtils.getUnformatted(tag.getAsString());

            if (unformattedLoreLine == null) continue;

            if (unformattedLoreLine.equals("Crafted Potion")) {
                isCraftedPotion = true;
            } else if (unformattedLoreLine.startsWith("- Heal:")) {
                hasHealEffect = true;
            }
        }

        return isCraftedPotion && hasHealEffect;
    }

    public static boolean isConsumable(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;

        // consumables are either a potion or a diamond axe for crafteds
        // to ensure an axe item is really a consumable, make sure it has the right name color
        if (itemStack.getItem() != Items.POTION
                && !(itemStack.getItem() == Items.DIAMOND_AXE
                        && itemStack.getHoverName().getString().startsWith(ChatFormatting.DARK_AQUA.toString())))
            return false;

        return consumableNameMatcher(itemStack.getHoverName()).matches();
    }

    public static boolean isUnidentified(ItemStack itemStack) {
        return (itemStack.getItem() == Items.STONE_SHOVEL
                && itemStack.getDamageValue() >= 1
                && itemStack.getDamageValue() <= 6);
    }

    public static boolean isEmeraldPouch(ItemStack itemStack) {
        if (itemStack instanceof EmeraldPouchItemStack) {
            return true;
        }

        // Checks for normal emerald pouch (diamond axe) and emerald pouch pickup texture (gold shovel)
        return (itemStack.getItem() == Items.DIAMOND_AXE || itemStack.getItem() == Items.GOLDEN_SHOVEL)
                && itemStack.getHoverName().getString().startsWith("§aEmerald Pouch§2 [Tier");
    }

    /**
     * Returns true if the passed item has an attack speed
     */
    public static boolean isWeapon(ItemStack itemStack) {
        String lore = ItemUtils.getStringLore(itemStack);
        return lore.contains("Attack Speed") && lore.contains("§7");
    }

    public static boolean isHorse(ItemStack itemStack) {
        return itemStack.getItem() == Items.SADDLE
                && itemStack.getHoverName().getString().contains("Horse");
    }

    /**
     * Returns true if the passed item is a Wynncraft item (armor, weapon, accessory)
     */
    public static boolean isGear(ItemStack itemStack) {
        if (itemStack instanceof GearItemStack) {
            return true;
        }

        for (Component line : ItemUtils.getTooltipLines(itemStack)) {
            if (rarityLineMatcher(line).find()) return true;
        }
        return false;
    }

    /**
     * Determines if a given ItemStack is an instance of a gear item in the API
     */
    public static boolean isKnownGear(ItemStack itemStack) {
        String name = itemStack.getHoverName().getString();
        String strippedName = WynnUtils.normalizeBadString(ComponentUtils.stripFormatting(name));
        if (ItemProfilesManager.getItemsMap() == null
                || !ItemProfilesManager.getItemsMap().containsKey(strippedName)) return false;
        ItemProfile profile = ItemProfilesManager.getItemsMap().get(strippedName);
        return (profile != null
                && name.startsWith(profile.getTier().getChatFormatting().toString()));
    }

    public static boolean isCraftedGear(ItemStack itemStack) {
        String name = itemStack.getHoverName().getString();
        // crafted gear will have a dark aqua name and a % marker for the status of the item
        return (name.startsWith(ChatFormatting.DARK_AQUA.toString()) && name.contains("%"));
    }

    public static boolean isMythic(ItemStack itemStack) {
        // only gear, identified or not, could be a mythic
        if (!(isUnidentified(itemStack) || isGear(itemStack))) return false;

        return itemStack.getHoverName().getString().contains(ChatFormatting.DARK_PURPLE.toString());
    }

    /**
     * Returns true if the passed item has a durability value (crafted items, tools)
     */
    public static boolean isDurabilityItem(ItemStack itemStack) {
        for (Component line : ItemUtils.getTooltipLines(itemStack)) {
            if (durabilityLineMatcher(line).find()) return true;
        }
        return false;
    }

    /**
     * Returns true if the passed item is within the Wynncraft tier system (mythic, legendary, etc.)
     */
    public static boolean isTieredItem(ItemStack itemStack) {
        return isGear(itemStack) || isCraftedGear(itemStack) || isUnidentified(itemStack);
    }

    public static boolean isCosmetic(ItemStack itemStack) {
        for (Component c : ItemUtils.getTooltipLines(itemStack)) {
            if (COSMETIC_PATTERN.matcher(c.getString()).matches()) return true;
        }
        return false;
    }

    public static boolean isDailyRewardsChest(ItemStack itemStack) {
        return itemStack.getHoverName().getString().contains("Daily Reward");
    }

    public static boolean isPowder(ItemStack itemStack) {
        return itemStack instanceof PowderItemStack
                || powderNameMatcher(itemStack.getHoverName()).matches();
    }

    public static boolean isSkillTyped(ItemStack itemStack) {
        return skillIconMatcher(itemStack.getHoverName()).matches();
    }

    public static boolean isSkillPoint(ItemStack itemStack) {
        return skillPointNameMatcher(itemStack.getHoverName()).matches();
    }

    public static boolean isTeleportScroll(ItemStack itemStack) {
        return teleportScrollNameMatcher(itemStack.getHoverName()).matches();
    }

    public static boolean isDungeonKey(ItemStack itemStack) {
        if (!dungeonKeyNameMatcher(itemStack.getHoverName()).matches()) return false;

        for (Component line : ItemUtils.getTooltipLines(itemStack)) {
            // check lore to avoid matching misc. key items
            if (line.getString().contains("Dungeon Info")) return true;
            if (line.getString().contains("Corrupted Dungeon Key")) return true;
        }

        return false;
    }

    public static boolean isAmplifier(ItemStack itemStack) {
        return amplifierNameMatcher(itemStack.getHoverName()).matches();
    }

    public static boolean isIngredient(ItemStack itemStack) {
        if (itemStack instanceof IngredientItemStack) {
            return true;
        }

        if (!ingredientOrMaterialMatcher(itemStack.getHoverName()).matches()) {
            return false;
        }

        for (Component line : ItemUtils.getTooltipLines(itemStack)) {
            if (ComponentUtils.getCoded(line).contains("§8Crafting Ingredient")) return true;
        }

        return false;
    }

    public static boolean isMaterial(ItemStack itemStack) {
        if (!ingredientOrMaterialMatcher(itemStack.getHoverName()).matches()) {
            return false;
        }

        for (Component line : ItemUtils.getTooltipLines(itemStack)) {
            if (ComponentUtils.getCoded(line).contains("§7Crafting Material")) return true;
        }

        return false;
    }

    public static boolean isGatheringTool(ItemStack itemStack) {
        return gatheringToolMatcher(itemStack.getHoverName()).matches();
    }

    public static Matcher serverItemMatcher(Component text) {
        return SERVER_ITEM_PATTERN.matcher(text.getString());
    }

    public static Matcher rarityLineMatcher(Component text) {
        return ITEM_RARITY_PATTERN.matcher(text.getString());
    }

    public static Matcher durabilityLineMatcher(Component text) {
        return DURABILITY_PATTERN.matcher(text.getString());
    }

    public static Matcher powderNameMatcher(Component text) {
        return POWDER_PATTERN.matcher(WynnUtils.normalizeBadString(text.getString()));
    }

    public static Matcher emeraldPouchTierMatcher(Component text) {
        return EMERALD_POUCH_TIER_PATTERN.matcher(WynnUtils.normalizeBadString(ComponentUtils.getUnformatted(text)));
    }

    public static Matcher skillIconMatcher(Component text) {
        return SKILL_ICON_PATTERN.matcher(ComponentUtils.getCoded(text));
    }

    public static Matcher skillPointNameMatcher(Component text) {
        return SKILL_POINT_NAME_PATTERN.matcher(ComponentUtils.getCoded(text));
    }

    public static Matcher teleportScrollNameMatcher(Component text) {
        return TELEPORT_SCROLL_PATTERN.matcher(WynnUtils.normalizeBadString(ComponentUtils.getCoded(text)));
    }

    public static Matcher teleportScrollLocationMatcher(Component text) {
        return TELEPORT_LOCATION_PATTERN.matcher(WynnUtils.normalizeBadString(text.getString()));
    }

    public static Matcher dungeonKeyNameMatcher(Component text) {
        return DUNGEON_KEY_PATTERN.matcher(WynnUtils.normalizeBadString(text.getString()));
    }

    public static Matcher amplifierNameMatcher(Component text) {
        return AMPLIFIER_PATTERN.matcher(WynnUtils.normalizeBadString(text.getString()));
    }

    public static Matcher consumableNameMatcher(Component text) {
        return CONSUMABLE_PATTERN.matcher(WynnUtils.normalizeBadString(text.getString()));
    }

    public static Matcher ingredientOrMaterialMatcher(Component text) {
        return INGREDIENT_OR_MATERIAL_PATTERN.matcher(
                WynnUtils.normalizeBadString(ComponentUtils.getUnformatted(text)));
    }

    public static Matcher gatheringToolMatcher(Component text) {
        return GATHERING_TOOL_PATTERN.matcher(WynnUtils.normalizeBadString(ComponentUtils.getUnformatted(text)));
    }
}
