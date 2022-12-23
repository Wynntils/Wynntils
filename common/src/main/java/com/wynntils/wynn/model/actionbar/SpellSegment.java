/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.actionbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.actionbar.ActionBarPosition;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.Event;

public class SpellSegment implements ActionBarSegment {
    private static final Pattern SPELL_PATTERN =
            Pattern.compile("§0 +§a([RL])§7-(?:§[a7n])+([RL?])§7-§r(?:§[a7n])+([LR?])§r +");

    @Override
    public Pattern getPattern() {
        return SPELL_PATTERN;
    }

    @Override
    public void handleMatch(Matcher matcher) {
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

    public static class SpellSegmentUpdateEvent extends Event {
        private final Matcher matcher;

        public SpellSegmentUpdateEvent(Matcher matcher) {
            this.matcher = matcher;
        }

        public Matcher getMatcher() {
            return matcher;
        }
    }
}
