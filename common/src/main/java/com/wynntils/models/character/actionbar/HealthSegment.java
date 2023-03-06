/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.actionbar;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.type.ActionBarPosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HealthSegment implements ActionBarSegment {
    private static final Pattern HEALTH_PATTERN = Pattern.compile("§c❤ ([0-9]+)/([0-9]+)");

    private int currentHealth = -1;
    private int maxHealth = -1;

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
        currentHealth = Integer.parseInt(matcher.group(1));
        maxHealth = Integer.parseInt(matcher.group(2));
    }

    @Override
    public ActionBarPosition getPosition() {
        return ActionBarPosition.LEFT;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
