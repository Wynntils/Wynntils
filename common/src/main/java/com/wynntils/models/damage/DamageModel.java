/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.damage;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.models.damage.label.DamageLabelInfo;
import com.wynntils.models.damage.label.DamageLabelParser;
import com.wynntils.models.damage.type.DamageDealtEvent;
import com.wynntils.models.damage.type.FocusedDamageEvent;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.TimedSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.neoforged.bus.api.SubscribeEvent;

public final class DamageModel extends Model {
    // Test in DamageModel_DAMAGE_BAR_PATTERN
    private static final Pattern DAMAGE_BAR_PATTERN = Pattern.compile(
            "^\\s*§[ace](.*) - §c(\\d+(?:\\.\\d+)?[kKmM]?)§4❤(?:§r - ( ?(§.(.+))(Dam|Weak|Def))+)?\\s*$");

    private final DamageBar damageBar = new DamageBar();

    private final TimedSet<Long> areaDamageSet = new TimedSet<>(60, TimeUnit.SECONDS, true);

    private String focusedMobName = "";
    private String focusedMobElementals = "";
    private long focusedMobHealth;
    private CappedValue focusedMobHealthPercent = CappedValue.EMPTY;
    private long lastDamageDealtTimestamp;

    public DamageModel() {
        super(List.of());

        Handlers.BossBar.registerBar(damageBar);
        Handlers.Label.registerParser(new DamageLabelParser());
    }

    public long getLastDamageDealtTimestamp() {
        return lastDamageDealtTimestamp;
    }

    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (!(event.getLabelInfo() instanceof DamageLabelInfo damageLabelInfo)) return;

        Map<DamageType, Long> damages = damageLabelInfo.getDamages();
        WynntilsMod.postEvent(new DamageDealtEvent(damages));

        long damageSum = damages.values().stream().mapToLong(d -> d).sum();
        areaDamageSet.put(damageSum);

        lastDamageDealtTimestamp = System.currentTimeMillis();
    }

    public String getFocusedMobName() {
        return focusedMobName;
    }

    public String getFocusedMobElementals() {
        return focusedMobElementals; // TODO: Parse this into specific elements and expose as functions
    }

    public long getFocusedMobHealth() {
        return focusedMobHealth;
    }

    public CappedValue getFocusedMobHealthPercent() {
        return focusedMobHealthPercent;
    }

    public long getAreaDamagePerSecond() {
        return areaDamageSet.getEntries().stream()
                .filter(timedEntry -> (System.currentTimeMillis() - timedEntry.getCreation()) <= 1000L)
                .mapToLong(TimedSet.TimedEntry::getEntry)
                .sum();
    }

    public double getAverageAreaDamagePerSecond(int seconds) {
        return areaDamageSet.getEntries().stream()
                        .filter(timedEntry ->
                                (System.currentTimeMillis() - timedEntry.getCreation()) <= seconds * 1000L)
                        .mapToLong(TimedSet.TimedEntry::getEntry)
                        .sum()
                / (double) seconds;
    }

    public final class DamageBar extends TrackedBar {
        private DamageBar() {
            super(DAMAGE_BAR_PATTERN);
        }

        @Override
        public void setEvent(LerpingBossEvent event) {
            super.setEvent(event);
            // Wynncraft updates the focused mob health bar by repeatedly destroying and recreating the boss bar. We
            // grab the progress here, when the bar's recreation event is received, to avoid the brief delay before
            // the first progress update event is received, during which the state would otherwise be inconsistent
            onUpdateProgress(event.getProgress());
        }

        @Override
        public void onUpdateName(Matcher match) {
            String mobName = match.group(1);
            long health = StringUtils.parseSuffixedInteger(match.group(2));
            String mobElementals = match.group(3);
            if (mobElementals == null) {
                mobElementals = "";
            }

            if (mobName.equals(focusedMobName) && mobElementals.equals(focusedMobElementals)) {
                long oldHealth = focusedMobHealth;
                focusedMobHealth = health;

                WynntilsMod.postEvent(new FocusedDamageEvent.MobDamaged(
                        focusedMobName, focusedMobElementals, focusedMobHealth, oldHealth));
            } else {
                focusedMobName = mobName;
                focusedMobElementals = mobElementals;
                focusedMobHealth = health;

                WynntilsMod.postEvent(
                        new FocusedDamageEvent.MobFocused(focusedMobName, focusedMobElementals, focusedMobHealth));
            }
            lastDamageDealtTimestamp = System.currentTimeMillis();
        }

        @Override
        public void onUpdateProgress(float progress) {
            focusedMobHealthPercent = new CappedValue(Math.round(progress * 100), 100);
            lastDamageDealtTimestamp = System.currentTimeMillis();
        }

        @Override
        protected void reset() {
            focusedMobName = "";
            focusedMobElementals = "";
            focusedMobHealth = 0;
            focusedMobHealthPercent = CappedValue.EMPTY;
        }
    }
}
