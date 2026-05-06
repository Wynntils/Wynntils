/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.bossbars;

import static java.lang.Math.round;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.models.abilities.type.MirrorImageClone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

public class MirrorImageBar extends TrackedBar {
    private static final Pattern MIRROR_IMAGE_PATTERN =
            Pattern.compile("^§dMirror Image: (?<clones>((§[a7])?\uE040){0,7})");

    private static final Pattern CLONE_PATTERN = Pattern.compile("(?<color>§[a7])?\uE040");

    private final List<MirrorImageClone> clones = new ArrayList<>();

    private int duration = 0;

    public MirrorImageBar() {
        super(MIRROR_IMAGE_PATTERN);
    }

    public List<MirrorImageClone> getClones() {
        return Collections.unmodifiableList(clones);
    }

    public int getDuration() {
        return duration;
    }

    @Override
    protected void reset() {
        super.reset();

        clones.clear();
    }

    @Override
    public void onUpdateName(Matcher match) {
        clones.clear();
        Matcher cloneMatcher = CLONE_PATTERN.matcher(match.group("clones"));

        // Due to color code truncation in Component to StyledText conversion, not every clone icon has a color code
        // prefix. In case there is none we assume the clone has the same health state as the previous one.
        MirrorImageClone.ActiveState activeState = null;
        int start = 0;
        while (cloneMatcher.find(start)) {
            String colorCode = cloneMatcher.group("color");
            if (colorCode != null) {
                activeState = MirrorImageClone.ActiveState.fromColor(ChatFormatting.getByCode(colorCode.charAt(1)));
            }

            if (activeState == null) {
                WynntilsMod.warn("Error parsing Mirror Image Clones in string " + match.group("clones"));
                clones.clear();
                return;
            }

            clones.add(new MirrorImageClone(activeState));

            start = cloneMatcher.end();
        }
    }

    @Override
    public void onUpdateProgress(float progress) {
        updateValue((int) (progress * 100), 100);
        duration = round(progress * 60);
    }
}
