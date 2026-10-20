/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.timetrials.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.models.mount.type.MountType;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class TimeTrialLabelInfo extends LabelInfo {
    private final String trialName;
    private final int recommendedLevel;
    private final MountType availableMountType;

    public TimeTrialLabelInfo(
            StyledText label,
            Location location,
            Entity entity,
            String trialName,
            int recommendedLevel,
            MountType availableMountType) {
        super(label, location, entity);

        this.trialName = trialName;
        this.recommendedLevel = recommendedLevel;
        this.availableMountType = availableMountType;
    }

    public String getTrialName() {
        return trialName;
    }

    public int getRecommendedLevel() {
        return recommendedLevel;
    }

    public MountType getAvailableMountType() {
        return availableMountType;
    }

    @Override
    public String toString() {
        return "TimeTrialLabelInfo{" + "availableMountType="
                + availableMountType + ", label="
                + label + ", name='"
                + name + '\'' + ", location="
                + location + ", entity="
                + entity + ", recommendedLevel="
                + recommendedLevel + ", trialName='"
                + trialName + '\'' + '}';
    }
}
