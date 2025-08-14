/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat.bossbar;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DamageBar extends TrackedBar {
    // Test in DamageBar_DAMAGE_BAR_PATTERN
    private static final Pattern DAMAGE_BAR_PATTERN = Pattern.compile(
            "^\\s*§[0-9a-f](.*) - §c(\\d+(?:\\.\\d+)?[kKmM]?)§4❤(?:§r - ( ?(§.(.+))(Dam|Weak|Def))+)?\\s*$");

    public DamageBar() {
        super(DAMAGE_BAR_PATTERN);
    }

    @Override
    public void onUpdateName(Matcher match) {
        String mobName = match.group(1);
        long health = StringUtils.parseSuffixedInteger(match.group(2));
        String mobElementals = match.group(3);
        if (mobElementals == null) {
            mobElementals = "";
        }

        Models.Combat.checkFocusedMobValidity();
        if (mobName.equals(Models.Combat.getFocusedMobName())
                && mobElementals.equals(Models.Combat.getFocusedMobElementals())) {
            Models.Combat.updateFocusedMobHealth(health);
        } else {
            Models.Combat.updateFocusedMob(mobName, mobElementals, health);
        }
        Models.Combat.revalidateFocusedMob();
        Models.Combat.setLastDamageDealtTimestamp(System.currentTimeMillis());
    }

    @Override
    public void onUpdateProgress(float progress) {
        Models.Combat.updateFocusedMobHealthPercent(new CappedValue(Math.round(progress * 100), 100));
        Models.Combat.revalidateFocusedMob();
    }

    @Override
    protected void reset() {
        Models.Combat.invalidateFocusedMob();
    }
}
