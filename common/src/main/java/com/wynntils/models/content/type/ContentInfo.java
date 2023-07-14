/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content.type;

import com.wynntils.core.text.StyledText;
import java.util.List;
import java.util.Optional;

public record ContentInfo(
        ContentType type,
        String name,
        ContentStatus status,
        Optional<String> specialInfo,
        Optional<StyledText> description,
        Optional<ContentLength> length,
        Optional<String> lengthInfo,
        Optional<ContentDistance> distance,
        Optional<String> distanceInfo,
        Optional<ContentDifficulty> difficulty,
        ContentRequirements requirements,
        List<String> rewards,
        ContentTrackingState trackingState) {}
