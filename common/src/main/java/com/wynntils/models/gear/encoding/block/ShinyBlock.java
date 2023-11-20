/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.encoding.block;

import com.wynntils.models.gear.encoding.data.ShinyData;
import com.wynntils.models.gear.encoding.type.GearEncodingVersion;

public class ShinyBlock implements DataBlock<ShinyData> {
    @Override
    public ShinyData decodeData(GearEncodingVersion version, byte[] bytes, int offset) {
        return null;
    }

    @Override
    public byte[] encodeData(GearEncodingVersion version, ShinyData data) {
        return new byte[0];
    }
}
