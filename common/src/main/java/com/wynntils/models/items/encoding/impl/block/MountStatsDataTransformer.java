/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.MountStatsData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.models.mount.type.MountStat;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MountStatsDataTransformer extends DataTransformer<MountStatsData> {
    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, MountStatsData data) {
        return switch (version) {
            case VERSION_1, VERSION_2, VERSION_3 -> encodeMountStatsData(data);
        };
    }

    @Override
    public ErrorOr<MountStatsData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2, VERSION_3 -> decodeMountStatsData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.MOUNT_STATS_DATA_TRANSFORMER.getId();
    }

    private ErrorOr<UnsignedByte[]> encodeMountStatsData(MountStatsData data) {
        List<UnsignedByte> bytes = new ArrayList<>();
        // The first byte is the number of stats.
        bytes.add(UnsignedByte.of((byte) data.stats().size()));

        // A stat is encoded the following way:
        for (Map.Entry<MountStat, CappedValue> mountStat : data.stats().entrySet()) {
            // The first byte is the id of the stat.
            UnsignedByte id = UnsignedByte.of((byte) mountStat.getKey().getEncodingId());
            bytes.add(id);

            // The next bytes are the current value of the stat, which are assembled into an integer.
            int current = mountStat.getValue().current();
            if (current < 0) {
                return ErrorOr.error("Mount stat cannot be negative: " + current);
            }
            bytes.addAll(List.of(UnsignedByteUtils.encodeVariableSizedInteger(current)));

            // The next bytes are the maximum value of the stat, which are assembled into an integer.
            int max = mountStat.getValue().max();
            if (max < 0) {
                return ErrorOr.error("Mount stat cannot be negative: " + max);
            }
            bytes.addAll(List.of(UnsignedByteUtils.encodeVariableSizedInteger(max)));
        }

        return ErrorOr.of(bytes.toArray(new UnsignedByte[0]));
    }

    private ErrorOr<MountStatsData> decodeMountStatsData(ArrayReader<UnsignedByte> byteReader) {
        Map<MountStat, CappedValue> stats = new EnumMap<>(MountStat.class);

        // The first byte is the number of stats.
        int numberOfStats = byteReader.read().value();

        for (int i = 0; i < numberOfStats; i++) {
            // The first byte is the id of the stat.
            int statId = byteReader.read().value();

            MountStat mountStat = MountStat.fromEncodingId(statId);
            if (mountStat == null) {
                return ErrorOr.error("Cannot decode mount stat: " + statId);
            }

            // The next bytes are the current stat value bytes, which are assembled into an integer.
            int currentValue = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

            // The next bytes are the maximum stat value bytes, which are assembled into an integer.
            int maxValue = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

            stats.put(mountStat, new CappedValue(currentValue, maxValue));
        }

        return ErrorOr.of(new MountStatsData(stats));
    }
}
