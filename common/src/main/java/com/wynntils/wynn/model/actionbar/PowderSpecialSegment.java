/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.actionbar;

import com.wynntils.handlers.actionbar.ActionBarPosition;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.wynn.objects.Powder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PowderSpecialSegment implements ActionBarSegment {
    private static final Pattern POWDER_SPECIAL_PATTERN = Pattern.compile("§0 *§.([✤✦❉✹❋]) (\\d+)% *");

    private float powderSpecialCharge = 0;
    private Powder powderSpecialType = null;

    private boolean hidden;

    @Override
    public Pattern getPattern() {
        return POWDER_SPECIAL_PATTERN;
    }

    @Override
    public void update(Matcher matcher) {
        updatePowderSpecial(matcher);
    }

    @Override
    public void appeared(Matcher matcher) {
        updatePowderSpecial(matcher);
    }

    private void updatePowderSpecial(Matcher matcher) {
        powderSpecialType = Powder.getFromSymbol(matcher.group(1).charAt(0));
        powderSpecialCharge = Integer.parseInt(matcher.group(2));
    }

    @Override
    public ActionBarPosition getPosition() {
        return ActionBarPosition.CENTER;
    }

    @Override
    public void removed() {
        powderSpecialType = null;
        powderSpecialCharge = 0;
    }

    public float getPowderSpecialCharge() {
        return powderSpecialCharge;
    }

    public Powder getPowderSpecialType() {
        return powderSpecialType;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
