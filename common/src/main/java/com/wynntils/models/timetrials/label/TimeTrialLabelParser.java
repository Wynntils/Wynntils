/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.timetrials.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.models.mount.type.MountType;
import com.wynntils.utils.mc.type.Location;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class TimeTrialLabelParser implements LabelParser<TimeTrialLabelInfo> {
    // Test in TimeTrialLabelParser_TIME_TRIAL_LABEL_PATTERN
    private static final Pattern TIME_TRIAL_LABEL_PATTERN = Pattern.compile(
            "^§#1ebf97ff\uE060\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE038\uDAFF\uDFFF\uE03C\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE038\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE062\uDAFF\uDFC8§f\uE013\uE008\uE00C\uE004 \uE013\uE011\uE008\uE000\uE00B\uDB00\uDC02§#1ebf97ff\n(?<trialName>.+)\n\n§7Recommended Level: §f(?<recommendedLevel>\\d+)\n§7Available Mounts: §f(?<mountType>.+)s\n§7Approx. Length: §f\\d+-\\d+ Minutes$",
            Pattern.DOTALL);

    public TimeTrialLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = TIME_TRIAL_LABEL_PATTERN.matcher(label.getString());
        if (!matcher.matches()) return null;

        String timeTrialName = matcher.group("trialName");
        int recommendedLevel = Integer.parseInt(matcher.group("recommendedLevel"));
        MountType availableMountType = MountType.fromName(matcher.group("mountType"));

        return new TimeTrialLabelInfo(label, location, entity, timeTrialName, recommendedLevel, availableMountType);
    }
}
