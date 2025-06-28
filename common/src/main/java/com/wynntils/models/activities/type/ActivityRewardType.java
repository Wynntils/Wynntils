/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.regex.Pattern;

public enum ActivityRewardType {
    XP(List.of(Pattern.compile("\\d+(?: .+)? XP")), Texture.XP_REWARD),
    EMERALDS(List.of(Pattern.compile("\\d+ Emeralds"), Pattern.compile("Various Emeralds")), Texture.EMERALD_REWARD),
    ACCESS(List.of(Pattern.compile("Access to .+")), Texture.ACCESS_REWARD),
    ITEM(List.of(), Texture.ITEM_REWARD);

    private final List<Pattern> patterns;
    private final Texture texture;

    ActivityRewardType(List<Pattern> patterns, Texture texture) {
        this.patterns = patterns;
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }

    public static ActivityRewardType matchRewardType(String rewardLine) {
        for (ActivityRewardType rewardType : ActivityRewardType.values()) {
            for (Pattern pattern : rewardType.patterns) {
                if (pattern.matcher(rewardLine).matches()) {
                    return rewardType;
                }
            }
        }

        // Item can just be the default as some rewarded items are just the name, meaning we could only
        // match them with .+ which will match anything
        return ITEM;
    }
}
