/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.type.ActionBarPosition;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HealthSegment implements ActionBarSegment {
    private static final Pattern HEALTH_PATTERN = Pattern.compile("§c❤ ([0-9]+)/([0-9]+)");

    private CappedValue health = CappedValue.EMPTY;
    private boolean hidden;

    @Override
    public Pattern getPattern() {
        return HEALTH_PATTERN;
    }

    @Override
    public void update(Matcher matcher) {
        updateHealth(matcher);
    }

    @Override
    public void appeared(Matcher matcher) {
        updateHealth(matcher);
    }

    private void updateHealth(Matcher matcher) {
        int currentHealth = Integer.parseInt(matcher.group(1));
        int maxHealth = Integer.parseInt(matcher.group(2));

        health = new CappedValue(currentHealth, maxHealth);
    }

    @Override
    public ActionBarPosition getPosition() {
        return ActionBarPosition.LEFT;
    }

    public CappedValue getHealth() {
        return health;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
