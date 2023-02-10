/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.SynchedEntityData;

public final class PacketUtils {

    public static String getNameFromMetadata(List<SynchedEntityData.DataValue<?>> data) {
        // The rename stuff we're looking for is eventually something like
        // Optional[literal{§c§l3:46}[style={}]]
        // or
        // Optional[literal{§c26s}[style={}]]
        for (SynchedEntityData.DataValue<?> packedItem : data) {
            if (!(packedItem.value() instanceof Optional<?> packetData)
                    || packetData.isEmpty()
                    || (!(packetData.get() instanceof MutableComponent content))) continue;
            return content.getString();
        }
        return null;
    }
}
