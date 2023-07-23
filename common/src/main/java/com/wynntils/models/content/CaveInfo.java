/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.content.type.ContentDifficulty;
import com.wynntils.models.content.type.ContentDistance;
import com.wynntils.models.content.type.ContentLength;
import com.wynntils.models.content.type.ContentStatus;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public class CaveInfo {
    private final String name;
    private final ContentStatus status;
    private final String description;
    private final int recommendedLevel;
    private final ContentDistance distance;
    private final ContentLength length;
    private final ContentDifficulty difficulty;
    private final List<String> rewards;
    private List<Component> displayLore = null;

    public CaveInfo(
            String name,
            ContentStatus status,
            String description,
            int recommendedLevel,
            ContentDistance distance,
            ContentLength length,
            ContentDifficulty difficulty,
            List<String> rewards) {
        this.name = name;
        this.status = status;
        this.description = description;
        this.recommendedLevel = recommendedLevel;
        this.distance = distance;
        this.length = length;
        this.difficulty = difficulty;
        this.rewards = rewards;
    }

    public Optional<Location> getNextLocation() {
        return StyledTextUtils.extractLocation(StyledText.fromString(description));
    }

    public boolean isTrackable() {
        return status == ContentStatus.AVAILABLE || status == ContentStatus.STARTED;
    }

    public String getName() {
        return name;
    }

    public int getRecommendedLevel() {
        return recommendedLevel;
    }

    public ContentStatus getStatus() {
        return status;
    }

    public ContentDistance getDistance() {
        return distance;
    }

    public ContentLength getLength() {
        return length;
    }

    public ContentDifficulty getDifficulty() {
        return difficulty;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getRewards() {
        return rewards;
    }
}
