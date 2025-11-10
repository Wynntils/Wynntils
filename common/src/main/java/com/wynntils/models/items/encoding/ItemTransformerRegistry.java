/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding;

import com.wynntils.core.WynntilsMod;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.encoding.data.EndData;
import com.wynntils.models.items.encoding.data.NameData;
import com.wynntils.models.items.encoding.data.StartData;
import com.wynntils.models.items.encoding.data.TypeData;
import com.wynntils.models.items.encoding.impl.item.CharmItemTransformer;
import com.wynntils.models.items.encoding.impl.item.CraftedConsumableItemTransformer;
import com.wynntils.models.items.encoding.impl.item.CraftedGearItemTransformer;
import com.wynntils.models.items.encoding.impl.item.GearItemTransformer;
import com.wynntils.models.items.encoding.impl.item.TomeItemTransformer;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.encoding.type.ItemDataMap;
import com.wynntils.models.items.encoding.type.ItemTransformer;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.models.items.encoding.type.ItemType;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class holds all the {@link ItemTransformer} instances,
 * for all gear types and encoding versions.
 * <p>
 * The process of item encoding:
 * A compatible {@link com.wynntils.models.items.WynnItem} is passed to the {@link ItemTransformer},
 * which encodes the item into a list of {@link ItemData} objects.
 * Each {@link ItemData} object is then encoded into a byte array,
 * using the appropriate {@link DataTransformer} to do the encoding to {@link EncodedByteBuffer}.
 * <p>
 * The process of item decoding:
 * The {@link EncodedByteBuffer} is decoded into a list of {@link ItemData} objects, using the appropriate {@link DataTransformer}.
 * The {@link ItemTransformer} then decodes the {@link ItemData} objects into a {@link com.wynntils.models.items.WynnItem}.
 */
public final class ItemTransformerRegistry {
    private final DataTransformerRegistry dataTransformerRegistry = new DataTransformerRegistry();

    private final ItemTransformerMap itemTransformers = new ItemTransformerMap();

    public ItemTransformerRegistry() {
        registerAllTransformers();
    }

    public ErrorOr<EncodedByteBuffer> encodeItem(WynnItem wynnItem, EncodingSettings encodingSettings) {
        ItemTransformer<WynnItem> transformer = getTransformer(wynnItem);
        if (transformer == null) {
            return ErrorOr.error(
                    "No item transformer found for " + wynnItem.getClass().getSimpleName());
        }

        try {
            return encodeItem(wynnItem, encodingSettings, transformer);
        } catch (Exception e) {
            WynntilsMod.error("Failed to encode item!", e);
            return ErrorOr.error("Failed to encode item!");
        }
    }

    public ErrorOr<WynnItem> decodeItem(EncodedByteBuffer encodedByteBuffer, String itemName) {
        ErrorOr<List<ItemData>> errorOrItemData = dataTransformerRegistry.decodeData(encodedByteBuffer);
        if (errorOrItemData.hasError()) {
            return ErrorOr.error(errorOrItemData.getError());
        }

        List<ItemData> itemData = errorOrItemData.getValue();
        Optional<TypeData> typeDataOpt = itemData.stream()
                .filter(data -> data instanceof TypeData)
                .map(data -> (TypeData) data)
                .findFirst();
        if (typeDataOpt.isEmpty()) {
            return ErrorOr.error("No type data found in item data!");
        }

        TypeData typeData = typeDataOpt.get();
        ItemTransformer<WynnItem> transformer = itemTransformers.get(typeData.itemType());

        // Don't use the name block for crafted gear and consumables
        // This is used for crafted gear and consumables, so that "bad" names can't be injected into the item
        if (typeData.itemType() == ItemType.CRAFTED_GEAR || typeData.itemType() == ItemType.CRAFTED_CONSUMABLE) {
            itemData.removeIf(data -> data instanceof NameData);

            // Override the name block if we have a clear-chat name
            if (itemName != null) {
                itemData.add(NameData.sanitized(itemName));
            }
        }

        try {
            return decodeItem(itemData, transformer);
        } catch (Exception e) {
            WynntilsMod.error("Failed to decode item!", e);
            return ErrorOr.error("Failed to decode item!");
        }
    }

    public boolean canEncodeItem(WynnItem wynnItem) {
        return itemTransformers.get(wynnItem.getClass()) != null;
    }

    private ErrorOr<EncodedByteBuffer> encodeItem(
            WynnItem wynnItem, EncodingSettings encodingSettings, ItemTransformer<WynnItem> transformer) {
        List<ItemData> encodedData = new ArrayList<>();

        ItemTransformingVersion versionToEncodeWith = getEncodingVersionAccordingToItem(wynnItem);

        encodedData.add(new StartData(versionToEncodeWith));
        encodedData.addAll(transformer.encode(wynnItem, encodingSettings));
        encodedData.add(new EndData());

        return dataTransformerRegistry.encodeData(versionToEncodeWith, encodedData);
    }

    // FIXME: This could be much more sophisticated in the future,
    //        e.g. by requesting the minimum versions required from each transformer instead.
    private static ItemTransformingVersion getEncodingVersionAccordingToItem(WynnItem wynnItem) {
        ItemTransformingVersion versionToEncodeWith = ItemTransformingVersion.VERSION_1;
        if (wynnItem instanceof GearItem gearItem) {
            boolean shinyStatPresentWithRerolls = gearItem.getItemInstance()
                    .map(GearInstance::shinyStat)
                    .flatMap(shinyStat -> shinyStat.map(stat -> stat.shinyRerolls() != 0))
                    .orElse(false);
            if (shinyStatPresentWithRerolls) {
                versionToEncodeWith = ItemTransformingVersion.VERSION_2;
            }
        }
        return versionToEncodeWith;
    }

    private ErrorOr<WynnItem> decodeItem(List<ItemData> itemData, ItemTransformer<WynnItem> transformer) {
        return transformer.decodeItem(new ItemDataMap(itemData));
    }

    private ItemTransformer<WynnItem> getTransformer(WynnItem wynnItem) {
        return (ItemTransformer<WynnItem>) itemTransformers.get(wynnItem.getClass());
    }

    private <T extends WynnItem> void registerItemTransformer(Class<T> itemClass, ItemTransformer<T> itemTransformer) {
        itemTransformers.put(itemClass, itemTransformer);
    }

    private void registerAllTransformers() {
        registerItemTransformer(GearItem.class, new GearItemTransformer());
        registerItemTransformer(TomeItem.class, new TomeItemTransformer());
        registerItemTransformer(CharmItem.class, new CharmItemTransformer());
        registerItemTransformer(CraftedGearItem.class, new CraftedGearItemTransformer());
        registerItemTransformer(CraftedConsumableItem.class, new CraftedConsumableItemTransformer());
    }

    private static final class ItemTransformerMap {
        private final Map<Class<? extends WynnItem>, ItemTransformer<? extends WynnItem>> itemTransformers =
                new HashMap<>();
        private final Map<ItemType, ItemTransformer<? extends WynnItem>> typeTransformers = new HashMap<>();

        public void put(Class<? extends WynnItem> itemClass, ItemTransformer<? extends WynnItem> itemTransformer) {
            if (itemTransformers.put(itemClass, itemTransformer) != null) {
                throw new IllegalArgumentException(
                        "Item transformer already registered for " + itemClass.getSimpleName());
            }
            if (typeTransformers.put(itemTransformer.getType(), itemTransformer) != null) {
                throw new IllegalArgumentException(
                        "Item transformer already registered for " + itemTransformer.getType());
            }
        }

        public <T extends WynnItem> ItemTransformer<T> get(Class<T> itemClass) {
            return (ItemTransformer<T>) itemTransformers.get(itemClass);
        }

        public <T extends WynnItem> ItemTransformer<T> get(ItemType type) {
            return (ItemTransformer<T>) typeTransformers.get(type);
        }
    }
}
