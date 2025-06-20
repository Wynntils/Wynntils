/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.quests;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.activities.type.ActivityDifficulty;
import com.wynntils.models.activities.type.ActivityLength;
import com.wynntils.models.activities.type.ActivityRequirements;
import com.wynntils.models.activities.type.ActivityRewardType;
import com.wynntils.models.activities.type.ActivityStatus;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record QuestInfo(
        String name,
        String specialInfo,
        ActivityDifficulty difficulty,
        ActivityStatus status,
        ActivityLength length,
        int level,
        StyledText nextTask,
        ActivityRequirements additionalRequirements,
        boolean isMiniQuest,
        Map<ActivityRewardType, List<StyledText>> rewards) {
    private static final int NEXT_TASK_MAX_WIDTH = 200;

    public boolean trackable() {
        return status == ActivityStatus.AVAILABLE || status == ActivityStatus.STARTED;
    }

    public Optional<Location> nextLocation() {
        return StyledTextUtils.extractLocation(nextTask);
    }

    public int sortLevel() {
        return !isMiniQuest || additionalRequirements.level().a() != 0
                ? level
                : additionalRequirements.professionLevels().getFirst().a().b();
    }
}
