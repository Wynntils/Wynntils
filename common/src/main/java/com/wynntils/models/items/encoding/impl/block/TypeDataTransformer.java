/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.TypeData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

public class TypeDataTransformer implements DataTransformer<TypeData> {
    @Override
    public ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, TypeData data) {
        return switch (version) {
            case VERSION_1 -> ErrorOr.of(new UnsignedByte[0]);
        };
    }
}
