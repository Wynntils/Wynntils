/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.type.ActionBarPosition;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SprintSegment implements ActionBarSegment {
    // Test in SprintSegment_SPRINT_PATTERN
    private static final Pattern SPRINT_PATTERN = Pattern.compile("§[246]\\[(§.*)§[246]]");
    private static final int MAX_SPRINT = 13;
    private CappedValue sprint = CappedValue.EMPTY;

    @Override
    public Pattern getPattern() {
        return SPRINT_PATTERN;
    }

    @Override
    public void update(Matcher matcher) {
        updateSprint(matcher);
    }

    @Override
    public void appeared(Matcher matcher) {
        updateSprint(matcher);
    }

    private void updateSprint(Matcher matcher) {
        // Expamples:
        // "§2[§a|||Sprint|||§2]" -- max
        // "§2[§a|||Spr§8int|||§2]"  -- partial
        // "§6[§e|||S§8print|||§6]" -- warning
        // "§4[§8|||Sprint|||§4]" -- flashing
        // "§4[§c|||Sprint|||§4]"

        StyledText sprint = StyledText.fromString(matcher.group(1));
        // If the sprint string starts with §[ae] we must start counting from after this
        // formatting, but not if it starts with §[8c]
        int startPos =
                (sprint.getString().charAt(1) == 'a' || sprint.getString().charAt(1) == 'e') ? 2 : 0;
        int redIndex = sprint.getString().indexOf("§8");
        if (redIndex == -1) {
            int greyIndex = sprint.getString().indexOf("§c");
            if (greyIndex != -1) {
                // We're at the last bar and has started flashing
                if (greyIndex != 0) {
                    WynntilsMod.warn("Incorrectly formatted sprint segment:" + sprint);
                }
                updateSprint(1);
            } else {
                updateSprint(MAX_SPRINT);
            }
        } else {
            // We add 1 since no bars left should not be interpreted as 0, which means no sprint at all
            updateSprint(redIndex + 1 - startPos);
        }
    }

    @Override
    public ActionBarPosition getPosition() {
        return ActionBarPosition.CENTER;
    }

    @Override
    public void removed() {
        removeSprint();
    }

    private void updateSprint(int newSprint) {
        if (newSprint != sprint.current()) {
            sprint = new CappedValue(newSprint, MAX_SPRINT);
        }
    }

    private void removeSprint() {
        sprint = CappedValue.EMPTY;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    public CappedValue getSprint() {
        return sprint;
    }
}
