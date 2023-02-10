package com.wynntils.utils.mc;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.SynchedEntityData;

import java.util.List;
import java.util.Optional;

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
