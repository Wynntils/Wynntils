/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.character.actionbar.segments.CharacterSelectionClassSegment;
import com.wynntils.models.character.type.ClassType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharacterSelectionClassSegmentMatcher implements ActionBarSegmentMatcher {
    private static final Pattern CLASS_CARD_PATTERN =
            Pattern.compile("\uDAFF\uDF8C\u0001([\uE000-\uE004])\uDB00\uDC0A");

    @Override
    public ActionBarSegment parse(String actionBar) {
        Matcher matcher = CLASS_CARD_PATTERN.matcher(actionBar);
        if (!matcher.find()) return null;

        String classCard = matcher.group(1);
        ClassType classType = ClassType.fromCharacterSelectionCard(classCard);
        boolean isReskinned = ClassType.isReskinnedCharacterSelection(classType, classCard);

        return new CharacterSelectionClassSegment(actionBar, classType, isReskinned);
    }
}
