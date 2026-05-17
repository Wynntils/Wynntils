/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.worlds.type.BombInfo;
import com.wynntils.models.worlds.type.BombSortOrder;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.type.Time;

import java.util.List;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class BombFunctions {

    private static BombInfo getBombInfo(int index, boolean group, String sortOrderValue) {
        BombSortOrder sortOrder = BombSortOrder.fromString(sortOrderValue);
        List<BombInfo> bombInfo = Models.Bomb.getBombBellStream(group, sortOrder).toList();

        return (!bombInfo.isEmpty() && index >= 0 && index < bombInfo.size()) ? bombInfo.get(index) : null;
    }

    @TemplateFunction(name = "bomb_formatted_info")
    public static String bombFormattedStringFunction(int index, boolean group, String sortOrderValue) {
        BombInfo info = getBombInfo(index, group, sortOrderValue);
        return info != null ? info.asString() : "";
    }

    @TemplateFunction(name = "bomb_type")
    public static String bombTypeFunction(int index, boolean group, String sortOrderValue) {
        BombInfo info = getBombInfo(index, group, sortOrderValue);
        return info != null ? info.bomb().getDisplayName() : "";
    }

    @TemplateFunction(name = "bomb_world")
    public static String bombWorldFunction(int index, boolean group, String sortOrderValue) {
        BombInfo info = getBombInfo(index, group, sortOrderValue);
        return info != null ? info.server() : "";
    }

    @TemplateFunction(name = "bomb_start_time")
    public static Time bombStartTimeFunction(int index, boolean group, String sortOrderValue) {
        BombInfo info = getBombInfo(index, group, sortOrderValue);
        return info != null ? Time.of(info.startTime()) : Time.NONE;
    }

    @TemplateFunction(name = "bomb_length")
    public static double bombLengthFunction(int index, boolean group, String sortOrderValue) {
        BombInfo info = getBombInfo(index, group, sortOrderValue);
        return info != null ? info.length() : -1;
    }

    @TemplateFunction(name = "bomb_end_time")
    public static Time bombEndTimeFunction(int index, boolean group, String sortOrderValue) {
        BombInfo info = getBombInfo(index, group, sortOrderValue);
        return info != null ? Time.of(info.endTime()) : Time.NONE;
    }

    @TemplateFunction(name = "bomb_owner")
    public static String bombOwnerFunction(int index, boolean group, String sortOrderValue) {
        BombInfo info = getBombInfo(index, group, sortOrderValue);
        return info != null ? info.user() : "";
    }

    @TemplateFunction(name = "bomb_remaining_time")
    public static Time bombRemaingTimeFunction(int index, boolean group, String sortOrderValue) {
        BombInfo info = getBombInfo(index, group, sortOrderValue);
        return info != null ? Time.of(info.getRemainingLong()) : Time.NONE;
    }
}
