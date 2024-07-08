/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.matchers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.handlers.actionbar.segments.HealthBarSegment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HealthBarSegmentMatcher implements ActionBarSegmentMatcher {
    // 󏾜󏿂󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󏿿󐀠

    // The health bar can have multiple different background characters, collected from the resource pack
    private static final String HEALTH_BACKGROUND_CHARS = "\uE029\uE039\uE049\uE059\uE069";

    // This is the expected pattern for the health bar background (spacer + health bar background)
    private static final Pattern BACKGROUND_PATTERN = Pattern.compile("\uDAFF\uDF9C[" + HEALTH_BACKGROUND_CHARS + "]");

    // This is the last character of the health bar
    private static final String LAST_SPACE_STRING = "\uDB00\uDC20";

    // These are the characters that build the health bar, collected from the resource pack
    private static final String HEALTH_BAR_CHARS =
            "\uE020\uE021\uE022\uE023\uE024\uE025\uE026\uE027\uE028\uE030\uE031\uE032\uE033\uE034\uE035\uE036\uE037\uE038\uE040\uE041\uE042\uE043\uE044\uE045\uE046\uE047\uE048\uE051\uE052\uE053\uE054\uE055\uE056\uE057\uE058";

    // The health bar should have 10 spacer+bar pairs
    private static final Pattern HEALTH_BAR_PATTERN = Pattern.compile("(.[" + HEALTH_BAR_CHARS + "]){10}");

    @Override
    public ActionBarSegment parse(String actionBar) {
        Matcher matcher = BACKGROUND_PATTERN.matcher(actionBar);

        if (!matcher.find()) {
            return null;
        }

        int beginIndex = actionBar.indexOf(matcher.group());
        int endIndex = actionBar.indexOf(LAST_SPACE_STRING, beginIndex);

        if (endIndex == -1) {
            WynntilsMod.warn("Found health bar background, but couldn't find the end of the segment: " + actionBar);
            return null;
        }

        String segmentText = actionBar.substring(beginIndex, endIndex + LAST_SPACE_STRING.length());

        // Remove the background and last space characters to get the characters that build the health bar
        String healthBarText =
                segmentText.substring(matcher.group().length(), segmentText.length() - LAST_SPACE_STRING.length());

        if (!HEALTH_BAR_PATTERN.matcher(healthBarText).matches()) {
            WynntilsMod.warn("Health bar segment seems to match, but the bar text is not expected: " + healthBarText);
            return null;
        }

        return new HealthBarSegment(segmentText);
    }
}
