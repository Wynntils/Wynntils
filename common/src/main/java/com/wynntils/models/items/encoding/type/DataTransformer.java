/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;

/**
 * Interface for transforming data into bytes.
 * @param <T> The type of data to transform.
 */
public interface DataTransformer<T> {
    ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, T data);
}
