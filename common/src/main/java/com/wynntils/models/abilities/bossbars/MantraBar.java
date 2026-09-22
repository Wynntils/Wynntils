/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.models.abilities.type.ShamanMaskType;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

public final class MantraBar extends TrackedBar {
    private static final Pattern MANTRA_BAR_PATTERN = Pattern.compile(
            "§#f4557dff\uE024 Lunatic §(?<lunaticCap>.)\\+(?<lunatic>\\d+)%§8 \\| §#99e9ffff\uE022 Heretic §(?<hereticCap>.)\\+(?<heretic>\\d+)%§8 \\| §#ffc251ff\uE023 Fanatic §(?<fanaticCap>.)\\+(?<fanatic>\\d+)%");

    private Map<ShamanMaskType, Integer> maskOverload = new HashMap<>();
    private Map<ShamanMaskType, Boolean> overloadCappedMap = new HashMap<>();

    public MantraBar() {
        super(MANTRA_BAR_PATTERN);
    }

    @Override
    public void onUpdateName(Matcher match) {
        maskOverload.put(ShamanMaskType.LUNATIC, Integer.parseInt(match.group("lunatic")));
        maskOverload.put(ShamanMaskType.HERETIC, Integer.parseInt(match.group("heretic")));
        maskOverload.put(ShamanMaskType.FANATIC, Integer.parseInt(match.group("fanatic")));

        overloadCappedMap.put(
                ShamanMaskType.LUNATIC,
                match.group("lunaticCap").equals(String.valueOf(ChatFormatting.GREEN.getChar())));
        overloadCappedMap.put(
                ShamanMaskType.HERETIC,
                match.group("hereticCap").equals(String.valueOf(ChatFormatting.GREEN.getChar())));
        overloadCappedMap.put(
                ShamanMaskType.FANATIC,
                match.group("fanaticCap").equals(String.valueOf(ChatFormatting.GREEN.getChar())));
    }

    @Override
    public void onUpdateProgress(float progress) {
        updateValue((int) (progress * 100), 100);
    }

    public Integer getMaskOverload(ShamanMaskType maskType) {
        return maskOverload.getOrDefault(maskType, 0);
    }

    public boolean isOverloadCapped(ShamanMaskType maskType) {
        return overloadCappedMap.getOrDefault(maskType, false);
    }
}
