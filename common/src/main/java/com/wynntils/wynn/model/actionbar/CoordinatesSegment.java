/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.actionbar;

import com.wynntils.handlers.actionbar.ActionBarPosition;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoordinatesSegment implements ActionBarSegment {
    private static final Pattern COORDINATES_PATTERN = Pattern.compile("(§0 *)§7 ?(-?\\d+)§f ?(.+)§7 ?(-?\\d+)( *)");

    @Override
    public Pattern getPattern() {
        return COORDINATES_PATTERN;
    }

    @Override
    public void handleMatch(Matcher matcher) {
        /* Currently we don't care about the actual matches.
        String leftPad = matcher.group(1);
        String xCoord = matcher.group(2);
        String orient = matcher.group(3);
        String yCoord = matcher.group(4);
        String rightPad = matcher.group(5);
         */
    }

    @Override
    public ActionBarPosition getPosition() {
        return ActionBarPosition.CENTER;
    }

    @Override
    public boolean isHidden() {
        return false;
    }
}
