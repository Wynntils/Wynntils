/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.spells.actionbar.segments.SpellCastSegment;
import com.wynntils.models.spells.type.SpellType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellCastSegmentMatcher implements ActionBarSegmentMatcher {
    // The start and end characters' high surrogate for the spell cast segment
    private static final char SPACER_HIGH_SURROGATE = '\uDAFF';

    // The character for mana cost
    private static final String MANA_ICON = "\uE531";

    // The character for health cost
    private static final String HEALTH_ICON = "\uE530";

    private static final Pattern SPELL_REGEX = Pattern.compile(
            ".(?<spellName>[A-Za-z ]+?) Cast!(?: -(?<costOne>[0-9]+) ?(?<costOneIcon>" + MANA_ICON + "|" + HEALTH_ICON
                    + "))?(?: -(?<costTwo>[0-9]+) ?(?<costTwoIcon>" + MANA_ICON + "|" + HEALTH_ICON + "))?.");

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();
        Matcher matcher = SPELL_REGEX.matcher(actionBarString);
        if (!matcher.find()) return null;

        String segmentText = matcher.group();

        // First unicode character's high surrogate
        char startChar = segmentText.charAt(0);

        // Last unicode character's high surrogate
        char endChar = segmentText.charAt(segmentText.length() - 2);

        // Check if the segment text is surrounded by the correct separators
        boolean validStart = startChar == SPACER_HIGH_SURROGATE;
        boolean validEnd = endChar == SPACER_HIGH_SURROGATE;

        if (!validStart || !validEnd) return null;

        SpellType spellType = SpellType.fromName(matcher.group("spellName"));

        int manaCost = 0;
        int healthCost = 0;

        if (MANA_ICON.equals(matcher.group("costOneIcon"))) {
            manaCost = Integer.parseInt(matcher.group("costOne"));
        } else {
            healthCost = Integer.parseInt(matcher.group("costOne"));
        }

        String costTwoIcon = matcher.group("costTwoIcon");

        if (MANA_ICON.equals(costTwoIcon)) {
            manaCost = Integer.parseInt(matcher.group("costTwo"));
        } else if (HEALTH_ICON.equals(costTwoIcon)) {
            healthCost = Integer.parseInt(matcher.group("costTwo"));
        }

        return new SpellCastSegment(matcher.group(), matcher.start(), matcher.end(), spellType, manaCost, healthCost);
    }
}
