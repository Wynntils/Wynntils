/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.type.ActionBarPosition;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManaSegment implements ActionBarSegment {
    private static final Pattern MANA_PATTERN = Pattern.compile("§b✺ ([0-9]+)/([0-9]+)");

    private CappedValue mana = CappedValue.EMPTY;
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
        int currentMana = Integer.parseInt(matcher.group(1));
        int maxMana = Integer.parseInt(matcher.group(2));

        mana = new CappedValue(currentMana, maxMana);
    }

    public CappedValue getMana() {
        return mana;
    }

    @Override
    public ActionBarPosition getPosition() {
        return ActionBarPosition.RIGHT;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
