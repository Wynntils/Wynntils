/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.content.type.ContentDifficulty;
import com.wynntils.models.content.type.ContentDistance;
import com.wynntils.models.content.type.ContentInfo;
import com.wynntils.models.content.type.ContentLength;
import com.wynntils.models.content.type.ContentStatus;
import com.wynntils.models.content.type.ContentTrackingState;
import com.wynntils.models.content.type.ContentType;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class ContentModel extends Model {
    private static final Pattern LEVEL_PATTERN =
            Pattern.compile("^§..À?§7(?: Recommended)? Combat Lv(?:\\. Min)?: (\\d+)$");
    // FIXME: keep additional distance info?
    private static final Pattern DISTANCE_PATTERN = Pattern.compile("^   §7Distance: §.(\\w*)(?:§.*)?$");
    private static final Pattern DIFFICULTY_PATTERN = Pattern.compile("^   §7Difficulty: (\\w*)$");
    // FIXME: keep additional length info?
    private static final Pattern LENGTH_PATTERN = Pattern.compile("^   §7Length: (\\w*)(?:§.*)?$");
    private static final Pattern REWARD_HEADER_PATTERN = Pattern.compile("^   §dRewards:$");
    private static final Pattern REWARD_PATTERN = Pattern.compile("^   §d- §7\\+(.*)$");
    private static final Pattern TRACKING_PATTERN = Pattern.compile("^ *À*§.§lCLICK TO (UN)?TRACK$");
    /*
    Missing:
    quest req
    profession req
     */

    public ContentModel() {
        super(List.of());
    }

    public ContentInfo parseItem(String name, ContentType type, ItemStack itemStack) {
        LinkedList<StyledText> lore = LoreUtils.getLore(itemStack);

        String statusLine = lore.pop().getString();
        if (!statusLine.substring(0, 1).equals("§")) return null;

        ContentStatus status = ContentStatus.from(statusLine.substring(1, 2), itemStack.getItem());
        int specialInfoEnd = statusLine.indexOf(" - ");
        // If we have a specialInfo, skip the §x marker in the beginning, and keep everything
        // until the " - " comes.
        String specialInfo = specialInfoEnd != -1 ? statusLine.substring(2, specialInfoEnd) : null;
        if (!lore.pop().isEmpty()) return null;

        int level = 0;
        ContentDistance distance = null;
        ContentDifficulty difficulty = null;
        ContentLength length = null;
        ContentTrackingState trackingState = ContentTrackingState.UNTRACKABLE;
        List<String> rewards = new ArrayList<>();
        List<StyledText> descriptionLines = new ArrayList<>();

        for (StyledText line : lore) {
            Matcher levelMatcher = line.getMatcher(LEVEL_PATTERN);
            if (levelMatcher.matches()) {
                level = Integer.parseInt(levelMatcher.group(1));
                continue;
            }

            Matcher distanceMatcher = line.getMatcher(DISTANCE_PATTERN);
            if (distanceMatcher.matches()) {
                distance = ContentDistance.from(distanceMatcher.group(1));
                continue;
            }

            Matcher difficultyMatcher = line.getMatcher(DIFFICULTY_PATTERN);
            if (difficultyMatcher.matches()) {
                difficulty = ContentDifficulty.from(difficultyMatcher.group(1));
                continue;
            }

            Matcher lengthMatcher = line.getMatcher(LENGTH_PATTERN);
            if (lengthMatcher.matches()) {
                length = ContentLength.from(lengthMatcher.group(1));
                continue;
            }

            Matcher rewardHeaderMatcher = line.getMatcher(REWARD_HEADER_PATTERN);
            if (rewardHeaderMatcher.matches()) {
                // Just ignore the header
                continue;
            }

            Matcher rewardMatcher = line.getMatcher(REWARD_PATTERN);
            if (rewardMatcher.matches()) {
                rewards.add(rewardMatcher.group(1));
                continue;
            }

            Matcher trackingMatcher = line.getMatcher(TRACKING_PATTERN);
            if (trackingMatcher.matches()) {
                trackingState = trackingMatcher.group(1) == null
                        ? ContentTrackingState.TRACKABLE
                        : ContentTrackingState.TRACKED;
                continue;
            }

            if (line.isEmpty()) continue;

            // For all other lines, append it to the description
            descriptionLines.add(line);
        }

        StyledText description = StyledTextUtils.joinLines(descriptionLines);

        return new ContentInfo(
                type,
                name,
                status,
                specialInfo,
                description,
                level,
                distance,
                difficulty,
                length,
                rewards,
                List.of(),
                trackingState);
    }
}
