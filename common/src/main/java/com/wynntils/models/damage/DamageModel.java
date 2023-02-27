/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.damage;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.models.damage.type.DamageEvent;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DamageModel extends Model {
    private final DamageBar damageBar = new DamageBar();

    private String focusedMobName;
    private String focusedMobElementals;
    private int focusedMobHealth;
    private long lastDamageDealtTimestamp;

    public DamageModel() {
        super(List.of());
        Handlers.BossBar.registerBar(damageBar);
    }

    public long getLastDamageDealtTimestamp() {
        return lastDamageDealtTimestamp;
    }

    public final class DamageBar extends TrackedBar {
        // https://regexr.com/7965g
        private static final Pattern DAMAGE_BAR_PATTERN =
                Pattern.compile("^§[ac](.*)§r - §c(\\d+)§4❤(?:§r - §7(.*)§7)?$");

        public DamageBar() {
            super(DAMAGE_BAR_PATTERN);
        }

        @Override
        public void onUpdateName(Matcher match) {
            String mobName = match.group(1);
            int health = Integer.parseInt(match.group(2));
            String mobElementals = match.group(3);
            if (mobElementals == null) {
                mobElementals = "";
            }

            if (mobName.equals(focusedMobName) && mobElementals.equals(focusedMobElementals)) {
                int oldHealth = focusedMobHealth;
                focusedMobHealth = health;

                WynntilsMod.postEvent(
                        new DamageEvent.MobDamaged(focusedMobName, focusedMobElementals, focusedMobHealth, oldHealth));
            } else {
                focusedMobName = mobName;
                focusedMobElementals = mobElementals;
                focusedMobHealth = health;

                WynntilsMod.postEvent(
                        new DamageEvent.MobFocused(focusedMobName, focusedMobElementals, focusedMobHealth));
            }
            lastDamageDealtTimestamp = System.currentTimeMillis();
        }

        @Override
        public void onUpdateProgress(float progress) {
            lastDamageDealtTimestamp = System.currentTimeMillis();
        }
    }
}
