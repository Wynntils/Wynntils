/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemrecord.type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.wynntils.core.components.Models;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.type.ErrorOr;
import java.lang.reflect.Type;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record SavedItem(String base64, Set<String> categories, ItemStack itemStack) implements Comparable<SavedItem> {
    public static SavedItem create(WynnItem wynnItem, Set<String> categories, ItemStack itemStack) {
        EncodingSettings encodingSettings = new EncodingSettings(
                Models.ItemEncoding.extendedIdentificationEncoding.get(), Models.ItemEncoding.shareItemName.get());
        ErrorOr<EncodedByteBuffer> errorOrEncodedByteBuffer =
                Models.ItemEncoding.encodeItem(wynnItem, encodingSettings);

        if (errorOrEncodedByteBuffer.hasError()) {
            throw new IllegalArgumentException(
                    "Tried to construct a SavedItem with unencodable WynnItem: " + errorOrEncodedByteBuffer.getError());
        }

        return new SavedItem(errorOrEncodedByteBuffer.getValue().toBase64String(), categories, itemStack);
    }

    /**
     * @return The wynnItem represented by this SavedItem
     * Note that this can't be done during deserialization because the models might not have finished loading yet
     */
    public WynnItem wynnItem() {
        ErrorOr<WynnItem> errorOrWynnItem = Models.ItemEncoding.decodeItem(EncodedByteBuffer.fromBase64String(base64));

        if (errorOrWynnItem.hasError()) {
            throw new IllegalStateException(
                    "Tried to decode a SavedItem with unencodable WynnItem: " + errorOrWynnItem.getError());
        }

        return errorOrWynnItem.getValue();
    }

    @Override
    public int compareTo(SavedItem other) {
        return this.base64.compareTo(other.base64);
    }

    public static class SavedItemSerializer implements JsonSerializer<SavedItem>, JsonDeserializer<SavedItem> {
        @Override
        public SavedItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            // Get base64 from jsonObject
            String base64 = jsonObject.get("base64").getAsString();

            // Get categories from jsonObject
            Set<String> categories = context.deserialize(jsonObject.get("categories"), Set.class);

            // Get itemStackInfo from jsonObject
            ItemStackInfo itemStackInfo = context.deserialize(jsonObject.get("itemStackInfo"), ItemStackInfo.class);

            // Create itemStack from itemStackInfo
            ItemStack itemStack = new ItemStack(Item.byId(itemStackInfo.itemId), 1);
            itemStack.getOrCreateTag().putInt("Damage", itemStackInfo.damage);
            itemStack.getOrCreateTag().putInt("HideFlags", itemStackInfo.hideFlags);
            itemStack.getOrCreateTag().putBoolean("Unbreakable", itemStackInfo.unbreakable);
            if (itemStackInfo.color != -1) {
                itemStack.getOrCreateTag().getCompound("display").putInt("color", itemStackInfo.color);
            }

            return new SavedItem(base64, categories, itemStack);
        }

        @Override
        public JsonElement serialize(SavedItem src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();

            // Add base64 to jsonObject
            jsonObject.addProperty("base64", src.base64());

            // Add categories to jsonObject
            jsonObject.add("categories", context.serialize(src.categories()));

            ItemStack itemStack = src.itemStack();

            // Leather armor can be dyed, we need to store the color
            int color = itemStack.getTag().getCompound("display").contains("color")
                    ? itemStack.getTag().getCompound("display").getInt("color")
                    : -1;

            ItemStackInfo itemStackInfo = new ItemStackInfo(
                    Item.getId(itemStack.getItem()),
                    itemStack.getTag().getInt("Damage"),
                    itemStack.getTag().getInt("HideFlags"),
                    itemStack.getTag().getBoolean("Unbreakable"),
                    color);

            // Add itemStackInfo to jsonObject
            jsonObject.add("itemStackInfo", context.serialize(itemStackInfo));

            return jsonObject;
        }
    }

    private record ItemStackInfo(int itemId, int damage, int hideFlags, boolean unbreakable, int color) {}
}
