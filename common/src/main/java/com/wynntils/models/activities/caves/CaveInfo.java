/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.caves;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.activities.type.ActivityDifficulty;
import com.wynntils.models.activities.type.ActivityDistance;
import com.wynntils.models.activities.type.ActivityLength;
import com.wynntils.models.activities.type.ActivityStatus;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public class CaveInfo {
    private final String name;
    private final ActivityStatus status;
    private final String description;
    private final int recommendedLevel;
    private final ActivityDistance distance;
    private final ActivityLength length;
    private final ActivityDifficulty difficulty;
    private final List<String> rewards;
    private List<Component> displayLore = null;

    public CaveInfo(
            String name,
            ActivityStatus status,
            String description,
            int recommendedLevel,
            ActivityDistance distance,
            ActivityLength length,
            ActivityDifficulty difficulty,
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
        return status == ActivityStatus.AVAILABLE || status == ActivityStatus.STARTED;
    }

    public String getName() {
        return name;
    }

    public int getRecommendedLevel() {
        return recommendedLevel;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public ActivityDistance getDistance() {
        return distance;
    }

    public ActivityLength getLength() {
        return length;
    }

    public ActivityDifficulty getDifficulty() {
        return difficulty;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getRewards() {
        return rewards;
    }
}
