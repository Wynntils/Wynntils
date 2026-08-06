/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.item;

import com.wynntils.core.components.Models;
import com.wynntils.models.items.encoding.data.MountColorData;
import com.wynntils.models.items.encoding.data.MountEnergyData;
import com.wynntils.models.items.encoding.data.MountPotentialData;
import com.wynntils.models.items.encoding.data.MountStatsData;
import com.wynntils.models.items.encoding.data.MountStatsMaxData;
import com.wynntils.models.items.encoding.data.MountTypeData;
import com.wynntils.models.items.encoding.data.NameData;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.encoding.type.ItemDataMap;
import com.wynntils.models.items.encoding.type.ItemTransformer;
import com.wynntils.models.items.encoding.type.ItemType;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.type.ColorType;
import com.wynntils.models.mount.type.MountColorInfo;
import com.wynntils.models.mount.type.MountInfo;
import com.wynntils.models.mount.type.MountStat;
import com.wynntils.models.mount.type.MountType;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MountItemTransformer extends ItemTransformer<MountItem> {
    private static final int MAX_POTENTIAL = 80000;
    private static final int MAX_STAT_VALUE = 10000;

    @Override
    public ErrorOr<MountItem> decodeItem(ItemDataMap itemDataMap) {
        MountType mountType;
        boolean isSummonItem;
        String name;
        int potential;
        MountColorInfo primaryColorInfo = MountColorInfo.UNKNOWN;
        MountColorInfo secondaryColorInfo = MountColorInfo.UNKNOWN;
        CappedValue currentEnergy;
        Map<MountStat, CappedValue> stats;
        boolean estimatedMaxStats;
        Map<MountStat, Integer> maxStats;
        MountInfo mountInfo;

        // Required blocks
        MountTypeData mountTypeData = itemDataMap.get(MountTypeData.class);
        if (mountTypeData == null) {
            return ErrorOr.error("Mount item does not have mount type data!");
        }
        mountType = mountTypeData.mountType();

        MountPotentialData mountPotentialData = itemDataMap.get(MountPotentialData.class);
        if (mountPotentialData == null) {
            return ErrorOr.error("Mount item does not have mount potential data!");
        }
        potential = mountPotentialData.potential();

        MountColorData mountColorData = itemDataMap.get(MountColorData.class);
        if (mountColorData != null) {
            primaryColorInfo = mountColorData.primaryColorInfo();
            secondaryColorInfo = mountColorData.secondaryColorInfo();
        }

        MountEnergyData mountEnergyData = itemDataMap.get(MountEnergyData.class);
        if (mountEnergyData == null) {
            return ErrorOr.error("Mount item does not have mount energy data!");
        }
        currentEnergy = mountEnergyData.energy();

        MountStatsData mountStatsData = itemDataMap.get(MountStatsData.class);
        if (mountStatsData == null) {
            return ErrorOr.error("Mount item does not have mount stats data!");
        }
        stats = mountStatsData.stats();

        MountStatsMaxData mountStatsMaxData = itemDataMap.get(MountStatsMaxData.class);
        if (mountStatsMaxData == null) {
            return ErrorOr.error("Mount item does not have mount stats max data!");
        }
        estimatedMaxStats = mountStatsMaxData.estimatedMaxStats();
        maxStats = mountStatsMaxData.maxStats();

        // Optional blocks
        // Warning: The name data from Mount items is deliberately removed from the item data map to prevent
        //           input sanitization issues.
        //           The name data present here is from plain-text string shared after the encoded item.
        NameData nameData = itemDataMap.get(NameData.class);
        if (nameData != null && nameData.name().isPresent()) {
            name = nameData.name().get();
        } else {
            name = EnumUtils.toNiceString(mountType);
        }

        // Do some verification

        // First check the potential is not negative and does not exceed the maximum potential.
        if (potential < 0) {
            return ErrorOr.error("Mount item potential cannot be negative!");
        } else if (potential > MAX_POTENTIAL) {
            return ErrorOr.error("Mount item potential cannot exceed 80,000!");
        }

        // Next check each of the stats are not negative and do not exceed the limit or maximum value.
        for (Map.Entry<MountStat, CappedValue> statEntry : stats.entrySet()) {
            if (statEntry.getValue().current() < 0 || statEntry.getValue().max() < 0) {
                return ErrorOr.error("Mount item stat cannot be negative!");
            } else if (statEntry.getValue().current() > MAX_STAT_VALUE
                    || statEntry.getValue().max() > MAX_STAT_VALUE) {
                return ErrorOr.error("Mount item stat cannot exceed 10,000!");
            }

            // Ensure the max stats make sense.
            if (statEntry.getValue().current() > statEntry.getValue().max()
                    || statEntry.getValue().max() > maxStats.get(statEntry.getKey())) {
                return ErrorOr.error("Mount item stat cannot exceed its max value!");
            } else if (maxStats.get(statEntry.getKey()) < 0) {
                return ErrorOr.error("Mount item max stat cannot be negative!");
            } else if (maxStats.get(statEntry.getKey()) > MAX_STAT_VALUE) {
                return ErrorOr.error("Mount item max stat cannot exceed 10,000!");
            }
        }

        // Check that the correct jump height/altitude stat is set.
        if (mountType == MountType.WYVERN
                && (stats.containsKey(MountStat.JUMP_HEIGHT) || maxStats.containsKey(MountStat.JUMP_HEIGHT))) {
            return ErrorOr.error("Wyvern cannot have jump height stat!");
        } else if (mountType != MountType.WYVERN
                && (stats.containsKey(MountStat.ALTITUDE) || maxStats.containsKey(MountStat.ALTITUDE))) {
            return ErrorOr.error("Non-wyvern mounts cannot have altitude stat!");
        }

        // Ensure that all 8 stats are present. -1 as jump height and altitude are not always present on each mount.
        if (stats.size() != MountStat.values().length - 1 || maxStats.size() != MountStat.values().length - 1) {
            return ErrorOr.error("Mount item does not contain all stats!");
        }

        // Check that the mount supports the primary and secondary colors.
        if (primaryColorInfo != MountColorInfo.UNKNOWN
                && !Models.Mount.isValidColor(primaryColorInfo, mountType, ColorType.PRIMARY)) {
            return ErrorOr.error(
                    mountType.name() + " does not support " + primaryColorInfo.displayName() + " as a primary color!");
        } else if (secondaryColorInfo != MountColorInfo.UNKNOWN
                && !Models.Mount.isValidColor(secondaryColorInfo, mountType, ColorType.SECONDARY)) {
            return ErrorOr.error(mountType.name() + " does not support " + secondaryColorInfo.displayName()
                    + " as a secondary color!");
        }

        // Finally check the potential and max stats are equal in the case of not estimated and below 1000.
        // For over 1000 ensure it fits in the potential +/-100 range, when using estimated max stats, allow +/-50.
        int maxSum = maxStats.values().stream().mapToInt(Integer::intValue).sum();
        int leeway = estimatedMaxStats ? 50 : 0;

        if (potential < 1000 && maxSum - potential > leeway) {
            return ErrorOr.error("Mount item potential does not equal max stats!");
        } else if (potential >= 1000 && (maxSum < potential - leeway || maxSum > potential + 99 + leeway)) {
            return ErrorOr.error("Mount item max stats exceed potential!");
        }

        mountInfo = new MountInfo(
                potential, primaryColorInfo, secondaryColorInfo, currentEnergy, stats, estimatedMaxStats, maxStats);

        return ErrorOr.of(new MountItem(name, mountType, mountInfo, false));
    }

    @Override
    protected List<ItemData> encodeItem(MountItem item, EncodingSettings encodingSettings) {
        List<ItemData> dataList = new ArrayList<>();

        // Required blocks
        dataList.add(MountTypeData.from(item));
        dataList.add(MountPotentialData.from(item));
        dataList.add(MountColorData.from(item));
        dataList.add(MountEnergyData.from(item));
        dataList.add(MountStatsData.from(item));
        dataList.add(MountStatsMaxData.from(item));

        return dataList;
    }

    @Override
    public ItemType getType() {
        return ItemType.MOUNT;
    }
}
