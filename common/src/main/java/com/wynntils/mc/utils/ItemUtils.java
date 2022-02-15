/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils;

import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemUtils {
    /**
     * Get the lore from an item, note that it may not be fully parsed. To do so, check out {@link ComponentUtils}
     *
     * @return an {@link List} containing all item lore
     */
    public static List<String> getLore(ItemStack item) {
        ListTag loreTag = getLoreTag(item);

        List<String> lore = new LinkedList<>();
        if (loreTag == null) return lore;

        for (int i = 0; i < loreTag.size(); ++i) {
            lore.add(ComponentUtils.getFormatted(loreTag.getString(i)));
        }

        return lore;
    }

    /** Get the lore NBT tag from an item, else return empty */
    public static ListTag getLoreTagElseEmpty(ItemStack item) {
        if (item.isEmpty()) return new ListTag();
        CompoundTag display = item.getTagElement("display");

        if (display == null || display.getType() != CompoundTag.TYPE || !display.contains("Lore"))
            return new ListTag();
        Tag loreBase = display.get("Lore");

        if (loreBase.getType() != ListTag.TYPE) return new ListTag();
        return (ListTag) loreBase;
    }

    /** Get the lore NBT tag from an item, else return null */
    public static ListTag getLoreTag(ItemStack item) {
        if (item.isEmpty()) return null;
        CompoundTag display = item.getTagElement("display");

        if (display == null || display.getType() != CompoundTag.TYPE || !display.contains("Lore"))
            return null;
        Tag loreBase = display.get("Lore");

        if (loreBase.getType() != ListTag.TYPE) return null;
        return (ListTag) loreBase;
    }

    /** Get the lore NBT tag from an item, else return null */
    public static ListTag getOrCreateLoreTag(ItemStack item) {
        if (item.isEmpty()) return null;

        Tag display = getOrCreateTag(item.getOrCreateTag(), "display", CompoundTag::new);
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
     * @param stack The {@link ItemStack} to have its
     * @param tag The {@link ListTag} to replace with
     */
    public static void replaceLore(ItemStack stack, ListTag tag) {
        CompoundTag nbt = stack.getOrCreateTag();
        CompoundTag display = (CompoundTag) getOrCreateTag(nbt, "display", CompoundTag::new);
        display.put("Lore", tag);
        nbt.put("display", display);
        stack.setTag(nbt);
    }

    /** Adds a boolean to an item's nbt as a marker */
    public static void addMarker(ItemStack stack, String id) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(id, true);
    }

    /** Checks if a boolean to an item's nbt as a marker */
    public static boolean hasMarker(ItemStack stack, String id) {
        return stack.hasTag() && stack.getTag().contains(id) && stack.getTag().getBoolean(id);
    }

    /**
     * Converts a string to a mutable component form
     *
     * <p>See {@link net.minecraft.network.chat.Component.Serializer#serialize(Component, Type,
     * JsonSerializationContext)}
     */
    public static String toLoreForm(String toConvert) {
        return "\"" + (toConvert).replace("\"", "\\\"") + "\"";
    }
}
