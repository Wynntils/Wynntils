/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items;

import com.wynntils.core.components.Model;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.models.items.encoding.ItemTransformerRegistry;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.type.ErrorOr;
import java.util.List;
import java.util.regex.Pattern;

public final class ItemEncodingModel extends Model {
    @Persisted
    public final Storage<Boolean> extendedIdentificationEncoding = new Storage<>(false);

    @Persisted
    public final Storage<Boolean> shareItemName = new Storage<>(true);

    // Encoded data consists of characters from Unicode Supplementary Private Use Area-A and B
    // (U+F0000..U+FFFFD and U+100000..U+10FFFD)
    private static final String RANGE_A =
            "[" + new String(Character.toChars(0xF0000)) + "-" + new String(Character.toChars(0xFFFFD)) + "]";
    private static final String RANGE_B =
            "[" + new String(Character.toChars(0x100000)) + "-" + new String(Character.toChars(0x10FFFD)) + "]";
    private static final Pattern ENCODED_DATA_PATTERN =
            Pattern.compile("(?<data>(" + RANGE_A + "|" + RANGE_B + ")+)( \"(?<name>.+)\")?");

    private final ItemTransformerRegistry itemTransformerRegistry = new ItemTransformerRegistry();

    public ItemEncodingModel() {
        super(List.of());
    }

    public ErrorOr<EncodedByteBuffer> encodeItem(WynnItem wynnItem, EncodingSettings encodingSettings) {
        return itemTransformerRegistry.encodeItem(wynnItem, encodingSettings);
    }

    public ErrorOr<WynnItem> decodeItem(EncodedByteBuffer encodedByteBuffer, String itemName) {
        return itemTransformerRegistry.decodeItem(encodedByteBuffer, itemName);
    }

    public boolean canEncodeItem(WynnItem wynnItem) {
        return itemTransformerRegistry.canEncodeItem(wynnItem);
    }

    public Pattern getEncodedDataPattern() {
        return ENCODED_DATA_PATTERN;
    }

    public String makeItemString(WynnItem wynnItem, EncodedByteBuffer encodedItem) {
        String itemName = "";

        // Gear items are named, but their names are encoded in the data
        if (shareItemName.get()
                && !(wynnItem instanceof GearItem)
                && wynnItem instanceof CraftedItemProperty craftedItemProperty) {
            itemName = " \"" + craftedItemProperty.getName() + "\"";
        }

        return encodedItem.toUtf16String() + itemName;
    }
}
