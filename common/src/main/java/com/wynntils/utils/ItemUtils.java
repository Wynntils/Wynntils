package com.wynntils.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ItemUtils {
    /**
     * Get the lore NBT tag from an item, else return null
     */
    public static ListTag getLoreTag(ItemStack item) {
        if (item.isEmpty()) return null;
        CompoundTag display = item.getTagElement("display");

        if (display == null || display.getType() != CompoundTag.TYPE || !display.contains("Lore")) return null;
        Tag loreBase = display.get("Lore");

        if (loreBase.getType() != ListTag.TYPE) return null;
        return (ListTag) loreBase;
    }

    /**
     * Get the lore NBT tag from an item, else return null
     */
    public static ListTag getOrCreateLoreTag(ItemStack item) {
        if (item.isEmpty()) return null;

        Tag display = getOrCreateTag(item.getOrCreateTag(), "display", CompoundTag::new);
        if (display.getType() != CompoundTag.TYPE) return null;

        Tag lore = getOrCreateTag((CompoundTag) display, "lore", ListTag::new);
        if (lore.getType() != ListTag.TYPE) return null;
        return (ListTag) lore;
    }

    /**
     * Get the lore NBT tag from an item, else return null
     */
    public static Tag getOrCreateTag(CompoundTag tag, String key, Supplier<Tag> create) {
        return tag.contains(key) ? tag.get(key) : tag.put(key, create.get());
    }

    /**
     * Get the lore from an item
     *
     * @return an {@link List} containing all item lore
     */
    public static List<String> getLore(ItemStack item) {
        ListTag loreTag = getLoreTag(item);

        List<String> lore = new ArrayList<>();
        if (loreTag == null) return lore;

        for (int i = 0; i < loreTag.size(); ++i) {
            lore.add(loreTag.getString(i));
        }

        return lore;
    }

    /**
     * Replace the lore on an item's NBT tag.
     *
     * @param stack The {@link ItemStack} to have its
     * @param tag
     */
    public static void replaceLore(ItemStack stack, ListTag tag) {
        CompoundTag nbt = stack.getOrCreateTag();
        CompoundTag display = (CompoundTag) getOrCreateTag(nbt, "display", CompoundTag::new);
        display.put("Lore", tag);
        nbt.put("display", display);
        stack.setTag(nbt);
    }

    /**
     * Override of {@link #replaceLore(ItemStack, ListTag)}
     *
     * @param lore A {@link List} to be turned into a {@link ListTag}
     */

    public static void replaceLore(ItemStack stack, List<String> lore) {
        ListTag tag = new ListTag();
        lore.forEach(s -> tag.add(StringTag.valueOf(s)));
        replaceLore(stack, tag);
    }
}
