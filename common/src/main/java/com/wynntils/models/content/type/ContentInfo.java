/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content.type;

import com.wynntils.core.text.StyledText;
import java.util.List;

public record ContentInfo(
        ContentType type,
        String name,
        ContentStatus status,
        String specialInfo,
        StyledText description,
        int level,
        ContentDistance distance,
        ContentDifficulty difficulty,
        ContentLength length,
        List<String> rewards,
        List<StyledText> requirements,
        ContentTrackingState trackingState) {}
