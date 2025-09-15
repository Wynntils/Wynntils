/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import com.wynntils.core.text.StyledText;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ActivityInfo(
        ActivityType type,
        String name,
        ActivityStatus status,
        Optional<String> specialInfo,
        Optional<StyledText> description,
        Optional<ActivityLength> length,
        Optional<String> lengthInfo,
        Optional<ActivityDistance> distance,
        Optional<String> distanceInfo,
        Optional<ActivityDifficulty> difficulty,
        Optional<WorldEventFastTravelStatus> worldEventFastTravelStatus,
        ActivityRequirements requirements,
        Map<ActivityRewardType, List<StyledText>> rewards,
        ActivityTrackingState trackingState) {}
