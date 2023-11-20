/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.encoding.block;

import com.wynntils.models.gear.encoding.type.GearEncodingVersion;

// Block/byte[] <-> Data
public interface DataBlock<T> {
    T decodeData(GearEncodingVersion version, byte[] bytes, int offset);

    byte[] encodeData(GearEncodingVersion version, T data);
}
