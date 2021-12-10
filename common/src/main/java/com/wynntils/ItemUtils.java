package com.wynntils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemUtils {
    /**
     * Get the lore NBT tag from an item
     */
    public static ListTag getLoreTag(ItemStack item) {
        if (item.isEmpty()) return null;
        CompoundTag display = item.getTagElement("display");
        if (display == null || !display.contains("Lore")) return null;

        Tag loreBase = display.get("Lore");
        ListTag lore;
        if (loreBase.getId() != 9) return null;

        lore = (ListTag) loreBase;
        if (lore.getType() != ListTag.TYPE) return null;

        return lore;
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
     * @param stack
     * @param lore
     */
    public static void replaceLore(ItemStack stack, List<String> lore) {
        CompoundTag nbt = stack.getTag();
        CompoundTag display = nbt.getCompound("display");
        ListTag tag = new ListTag();
        lore.forEach(s -> tag.add(StringTag.valueOf(s)));
        display.put("Lore", tag);
        nbt.put("display", display);
        stack.setTag(nbt);
    }
}
