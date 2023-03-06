/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.actionbar;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.type.ActionBarPosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManaSegment implements ActionBarSegment {
    private static final Pattern MANA_PATTERN = Pattern.compile("§b✺ ([0-9]+)/([0-9]+)");

    private int currentMana = -1;
    private int maxMana = -1;

    private boolean hidden;

    @Override
    public Pattern getPattern() {
        return MANA_PATTERN;
    }

    @Override
    public void update(Matcher matcher) {
        updateMana(matcher);
    }

    @Override
    public void appeared(Matcher matcher) {
        updateMana(matcher);
    }

    private void updateMana(Matcher matcher) {
        currentMana = Integer.parseInt(matcher.group(1));
        maxMana = Integer.parseInt(matcher.group(2));
    }

    @Override
    public ActionBarPosition getPosition() {
        return ActionBarPosition.RIGHT;
    }

    public int getCurrentMana() {
        return currentMana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
