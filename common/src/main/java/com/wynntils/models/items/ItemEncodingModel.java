/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items;

import com.wynntils.core.components.Model;
import com.wynntils.models.items.encoding.ItemTransformerRegistry;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.type.ErrorOr;
import java.util.List;

public class ItemEncodingModel extends Model {
    private final ItemTransformerRegistry itemTransformerRegistry = new ItemTransformerRegistry();

    public ItemEncodingModel() {
        super(List.of());
    }

    public ErrorOr<EncodedByteBuffer> encodeItem(WynnItem wynnItem) {
        return itemTransformerRegistry.encodeItem(wynnItem);
    }
}
