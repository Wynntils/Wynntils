/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.worldevents;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.activities.type.ActivityDifficulty;
import com.wynntils.models.activities.type.ActivityDistance;
import com.wynntils.models.activities.type.ActivityLength;
import com.wynntils.models.activities.type.ActivityRewardType;
import com.wynntils.models.activities.type.ActivityStatus;
import java.util.List;
import java.util.Map;

public record WorldEventInfo(
        String name,
        String region,
        String description,
        ActivityStatus status,
        int recommendedLevel,
        ActivityDistance distance,
        ActivityLength length,
        ActivityDifficulty difficulty,
        Map<ActivityRewardType, List<StyledText>> rewards) {}
