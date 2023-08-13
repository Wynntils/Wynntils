/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.actionbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.type.ActionBarPosition;
import com.wynntils.models.spells.event.SpellSegmentUpdateEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellSegment implements ActionBarSegment {
    private static final Pattern SPELL_PATTERN =
            Pattern.compile("§0 +§a([RL])§7-(?:§[a7n])?([RL?])(?:§r)?§7-(?:§[a7n])?([LR?])(?:§r)? +");

    @Override
    public Pattern getPattern() {
        return SPELL_PATTERN;
    }

    @Override
    public void update(Matcher matcher) {
        updateSpell(matcher);
    }

    @Override
    public void appeared(Matcher matcher) {
        updateSpell(matcher);
    }

    private void updateSpell(Matcher matcher) {
        WynntilsMod.postEvent(new SpellSegmentUpdateEvent(matcher));
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
