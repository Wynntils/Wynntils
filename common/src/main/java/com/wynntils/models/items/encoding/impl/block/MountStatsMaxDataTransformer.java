/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.MountStatsMaxData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.models.mount.type.MountStat;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MountStatsMaxDataTransformer extends DataTransformer<MountStatsMaxData> {
    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, MountStatsMaxData data) {
        return switch (version) {
            case VERSION_1, VERSION_2, VERSION_3 -> encodeMountStatsMaxData(data);
        };
    }

    @Override
    public ErrorOr<MountStatsMaxData> decodeData(
            ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2, VERSION_3 -> decodeMountStatsMaxData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.MOUNT_STATS_MAX_DATA_TRANSFORMER.getId();
    }

    private ErrorOr<UnsignedByte[]> encodeMountStatsMaxData(MountStatsMaxData data) {
        List<UnsignedByte> bytes = new ArrayList<>();

        // The first byte is whether these are estimated values or not.
        bytes.add(UnsignedByte.of((byte) (data.estimatedMaxStats() ? 1 : 0)));

        // The next byte is the number of stats.
        bytes.add(UnsignedByte.of((byte) data.maxStats().size()));

        // A stat is encoded the following way:
        for (Map.Entry<MountStat, Integer> mountStat : data.maxStats().entrySet()) {
            // The first byte is the id of the stat.
            UnsignedByte id = UnsignedByte.of((byte) mountStat.getKey().getEncodingId());
            bytes.add(id);

            // The next bytes are the max value of the stat, which are assembled into an integer.
            int value = mountStat.getValue();
            if (value < 0) {
                return ErrorOr.error("Mount stat max cannot be negative: " + value);
            }
            bytes.addAll(List.of(UnsignedByteUtils.encodeVariableSizedInteger(value)));
        }

        return ErrorOr.of(bytes.toArray(new UnsignedByte[0]));
    }

    private ErrorOr<MountStatsMaxData> decodeMountStatsMaxData(ArrayReader<UnsignedByte> byteReader) {
        Map<MountStat, Integer> maxStats = new EnumMap<>(MountStat.class);

        // The first byte is whether these are estimated values or not.
        boolean estimatedMaxStats = byteReader.read().value() != 0;

        // The next byte is the number of stats.
        int numberOfStats = byteReader.read().value();

        for (int i = 0; i < numberOfStats; i++) {
            // The first byte is the id of the stat.
            int statId = byteReader.read().value();

            MountStat mountStat = MountStat.fromEncodingId(statId);
            if (mountStat == null) {
                return ErrorOr.error("Cannot decode mount stat max: " + statId);
            }

            // The next bytes are the max stat value bytes, which are assembled into an integer.
            int max = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

            maxStats.put(mountStat, max);
        }

        return ErrorOr.of(new MountStatsMaxData(estimatedMaxStats, maxStats));
    }
}
