/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.StringUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class LoreUtils {
    /**
     * Get the lore from an item, note that it may not be fully parsed. To do so, check out {@link
     * ComponentUtils}
     *
     * @return an {@link List} containing all item lore
     */
    public static LinkedList<StyledText> getLore(ItemStack itemStack) {
        ListTag loreTag = getLoreTag(itemStack);

        LinkedList<StyledText> lore = new LinkedList<>();
        if (loreTag == null) return lore;

        for (int i = 0; i < loreTag.size(); ++i) {
            lore.add(StyledText.fromJson(loreTag.getString(i)));
        }

        return lore;
    }

    /**
     * Returns the lore for the given line, or the empty string if there is no
     * such line.
     */
    public static StyledText getLoreLine(ItemStack itemStack, int line) {
        ListTag loreTag = getLoreTag(itemStack);
        return loreTag == null ? StyledText.EMPTY : StyledText.fromJson(loreTag.getString(line));
    }

    /**
     * Check if the lore matches the given pattern, starting at the given line
     * and checking 5 more lines. (The reason for this is that the Trade Market
     * inserts additional lines at the top of the lore.)
     */
    public static Matcher matchLoreLine(ItemStack itemStack, int startLineNum, Pattern pattern) {
        Matcher matcher = null;
        for (int i = startLineNum; i <= startLineNum + 5; i++) {
            StyledText line = getLoreLine(itemStack, i);
            matcher = line.getMatcher(pattern);
            if (matcher.matches()) return matcher;
        }

        // Return the last non-matching matcher
        return matcher;
    }

    /**
     * Concatinates the lore of the given itemStack into a single StyledText.
     * To get the raw string, use {@link StyledText#getString()}.
     */
    public static StyledText getStringLore(ItemStack itemStack) {
        return StyledText.concat(getLore(itemStack));
    }

    /** Get the lore NBT tag from an item, else return empty */
    public static ListTag getLoreTagElseEmpty(ItemStack itemStack) {
        if (itemStack.isEmpty()) return new ListTag();
        CompoundTag display = itemStack.getTagElement("display");

        if (display == null || display.getType() != CompoundTag.TYPE || !display.contains("Lore")) return new ListTag();
        Tag loreBase = display.get("Lore");

        if (loreBase.getType() != ListTag.TYPE) return new ListTag();
        return (ListTag) loreBase;
    }

    /** Get the lore NBT tag from an item, else return null */
    public static ListTag getLoreTag(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        CompoundTag display = itemStack.getTagElement("display");

        if (display == null || display.getType() != CompoundTag.TYPE || !display.contains("Lore")) return null;
        Tag loreBase = display.get("Lore");

        if (loreBase.getType() != ListTag.TYPE) return null;
        return (ListTag) loreBase;
    }

    /** Get the lore NBT tag from an item, else return null */
    public static ListTag getOrCreateLoreTag(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;

        Tag display = getOrCreateTag(itemStack.getOrCreateTag(), "display", CompoundTag::new);
        if (display.getType() != CompoundTag.TYPE) return null;

        Tag lore = getOrCreateTag((CompoundTag) display, "lore", ListTag::new);
        if (lore.getType() != ListTag.TYPE) return null;
        return (ListTag) lore;
    }

    /** Get the lore NBT tag from an item, else return null */
    public static Tag getOrCreateTag(CompoundTag tag, String key, Supplier<Tag> create) {
        return tag.contains(key) ? tag.get(key) : tag.put(key, create.get());
    }

    /**
     * Replace the lore on an item's NBT tag.
     *
     * @param itemStack The {@link ItemStack} to have its
     * @param tag The {@link ListTag} to replace with
     */
    public static void replaceLore(ItemStack itemStack, ListTag tag) {
        CompoundTag nbt = itemStack.getOrCreateTag();
        CompoundTag display = (CompoundTag) getOrCreateTag(nbt, "display", CompoundTag::new);
        display.put("Lore", tag);
        nbt.put("display", display);
        itemStack.setTag(nbt);
    }

    /** Converts a string to a usable lore tag */
    public static StringTag toLoreStringTag(String toConvert) {
        return StringTag.valueOf(toLoreString(toConvert));
    }

    /**
     * Converts a string to a usable lore string
     *
     * <p>See {@link net.minecraft.network.chat.Component.Serializer#deserialize(JsonElement, Type,
     * JsonDeserializationContext)}
     */
    public static String toLoreString(String toConvert) {
        return "\"" + (toConvert).replace("\"", "\\\"") + "\"";
    }

    /**
     * Converts a component to a mutable component form by making it a json string
     *
     * <p>See {@link net.minecraft.network.chat.Component.Serializer#deserialize(JsonElement, Type,
     * JsonDeserializationContext)}
     */
    public static StringTag toLoreStringTag(Component toConvert) {
        // When italic is not set manually, it is null, but Minecraft still makes the text italic.
        // To prevent setting it to false manually every time, we can do this to force it being
        // non-italic by default.
        if (!toConvert.getStyle().isItalic()) {
            MutableComponent mutableComponent = (MutableComponent) toConvert;
            mutableComponent.setStyle(mutableComponent.getStyle().withItalic(false));
            return StringTag.valueOf(Component.Serializer.toJson(mutableComponent));
        }

        return StringTag.valueOf(Component.Serializer.toJson(toConvert));
    }

    public static List<Component> getTooltipLines(ItemStack itemStack) {
        TooltipFlag flag = McUtils.options().advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
        return itemStack.getTooltipLines(McUtils.player(), flag);
    }

    public static List<Component> appendTooltip(
            ItemStack itemStack, List<Component> baseTooltip, List<Component> tooltipAddon) {
        if (McUtils.options().advancedItemTooltips) {
            // These are lines as generated by vanilla if you have advanced tooltips on
            Component damagedLine = Component.translatable(
                    "item.durability", itemStack.getMaxDamage() - itemStack.getDamageValue(), itemStack.getMaxDamage());
            Component typeLine = Component.literal(
                            BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString())
                    .withStyle(ChatFormatting.DARK_GRAY);

            int advancedStartLine = -1;
            // If we have advanced tooltip lines, they are at the bottom 3 lines
            for (int i = baseTooltip.size() - 1; i >= Math.max(0, baseTooltip.size() - 4); i--) {
                Component line = baseTooltip.get(i);
                if (line.equals(typeLine)) {
                    advancedStartLine = i;
                    break;
                }
            }
            if (advancedStartLine > 1) {
                // The damage line is optional, but precedes the item type line if present
                if (baseTooltip.get(advancedStartLine - 1).equals(damagedLine)) {
                    advancedStartLine--;
                }
            }
            if (advancedStartLine != -1) {
                // We know where to inject our addon, so let's do it
                List<Component> newTooltip = new ArrayList<>();
                for (int i = 0; i < baseTooltip.size(); i++) {
                    if (i == advancedStartLine) {
                        newTooltip.addAll(tooltipAddon);
                    }
                    newTooltip.add(baseTooltip.get(i));
                }
                return newTooltip;
            }

            // Otherwise we failed to locate the advanced lines, fall through to below
        }

        // Otherwise we can just add it to the end
        List<Component> newTooltip = new ArrayList<>(baseTooltip);
        newTooltip.addAll(tooltipAddon);
        return newTooltip;
    }

    public static boolean isLoreEquals(ListTag existingLore, ListTag newLore) {
        if (existingLore == null && newLore == null) return true;
        if (existingLore == null || newLore == null) return false;

        // Both are non-null, compare content
        String existingLoreString = existingLore.getAsString();
        String newLoreString = newLore.getAsString();

        return existingLoreString.equals(newLoreString);
    }

    /**
     * This checks if the lore of the second item contains the entirety of the first item's lore, or vice versa.
     * It might have additional lines added, but these are not checked.
     */
    public static boolean loreSoftMatches(ItemStack firstItem, ItemStack secondItem, int tolerance) {
        List<StyledText> firstLines = getLore(firstItem);
        List<StyledText> secondLines = getLore(secondItem);
        int firstLinesLen = firstLines.size();
        int secondLinesLen = secondLines.size();

        // Only allow a maximum number of additional lines in the longer tooltip
        if (Math.abs(firstLinesLen - secondLinesLen) > tolerance) return false;

        int linesToCheck = Math.min(firstLinesLen, secondLinesLen);
        // Prevent soft matching on tooltips that are very small
        if (linesToCheck < 3 && firstLinesLen != secondLinesLen) return false;

        for (int i = 0; i < linesToCheck; i++) {
            if (!firstLines.get(i).equals(secondLines.get(i))) return false;
        }

        // Every lore line matches from the first to the second (or second to the first), so we have a match
        return true;
    }

    /**
     * This is used to extract the lore from an ingame item that is held by another player.
     * This lore has a completely different format from the normal lore shown to the player
     */
    public static JsonObject getJsonFromIngameLore(ItemStack itemStack) {
        String rawLore = StringUtils.substringBeforeLast(
                        getStringLore(itemStack).getString(), "}") + "}"; // remove extra unnecessary info
        try {
            return JsonParser.parseString(rawLore).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new JsonObject(); // invalid or empty itemData on item
        }
    }
}
