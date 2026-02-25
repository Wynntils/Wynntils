/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.athena.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.services.athena.actionbar.segments.WynncraftVersionSegment;
import com.wynntils.services.athena.type.WynncraftVersion;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WynncraftVersionSegmentMatcher implements ActionBarSegmentMatcher {
    // The §8 formatting code is not present in the unformatted string, so we match without it
    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "(?:(?<dev>DEV)|v(?<versiongroup>\\d+)\\.(?<majorversion>\\d+)\\.(?<minorversion>\\d+)_(?<revision>\\d+)(?<beta> BETA)?)");

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();
        Matcher matcher = VERSION_PATTERN.matcher(actionBarString);
        if (!matcher.find()) return null;

        if (matcher.group("dev") != null) {
            return new WynncraftVersionSegment(matcher.group(), matcher.start(), matcher.end(), WynncraftVersion.DEV);
        }

        return new WynncraftVersionSegment(
                matcher.group(),
                matcher.start(),
                matcher.end(),
                new WynncraftVersion(
                        matcher.group("versiongroup"),
                        matcher.group("majorversion"),
                        matcher.group("minorversion"),
                        matcher.group("revision"),
                        matcher.group("beta") != null));
    }
}
