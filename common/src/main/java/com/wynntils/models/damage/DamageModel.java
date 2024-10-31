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
import com.wynntils.handlers.labels.event.LabelsRemovedEvent;
import com.wynntils.models.damage.label.DamageLabelInfo;
import com.wynntils.models.damage.label.DamageLabelParser;
import com.wynntils.models.damage.type.DamageDealtEvent;
import com.wynntils.models.damage.type.FocusedDamageEvent;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.TimedSet;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;

public final class DamageModel extends Model {
    // Test in DamageModel_DAMAGE_BAR_PATTERN
    private static final Pattern DAMAGE_BAR_PATTERN = Pattern.compile(
            "^\\s*§[0-9a-f](.*) - §c(\\d+(?:\\.\\d+)?[kKmM]?)§4❤(?:§r - ( ?(§.(.+))(Dam|Weak|Def))+)?\\s*$");

    // Wynncraft updates the focused mob health bar by repeatedly destroying and recreating the boss bar. We don't want
    // to lose the entire focused mob state every time the boss bar is recreated, so we delay invalidation by this many
    // milliseconds and revalidate if a recreation/update event arrives during the delay
    private static final long FOCUSED_MOB_INVALIDATION_DELAY = 1000L;

    private final DamageBar damageBar = new DamageBar();

    private final TimedSet<Long> areaDamageSet = new TimedSet<>(60, TimeUnit.SECONDS, true);
    private final Map<Integer, Map<DamageType, Long>> liveDamageInfo = new HashMap<>();

    private String focusedMobName = "";
    private String focusedMobElementals = "";
    private long focusedMobHealth;
    private CappedValue focusedMobHealthPercent = CappedValue.EMPTY;
    private long focusedMobExpiryTime = -1L;

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

        int id = damageLabelInfo.getEntity().getId();
        Map<DamageType, Long> damages;

        if (liveDamageInfo.containsKey(id)) {
            Map<DamageType, Long> oldDamages = liveDamageInfo.get(id);
            Map<DamageType, Long> newDamages = damageLabelInfo.getDamages();
            liveDamageInfo.put(id, new EnumMap<>(newDamages));

            for (Map.Entry<DamageType, Long> entry : newDamages.entrySet()) {
                DamageType type = entry.getKey();
                long newValue = entry.getValue();

                if (oldDamages.containsKey(type)) {
                    long oldValue = oldDamages.get(type);
                    newDamages.put(type, newValue - oldValue);
                }
            }

            damages = newDamages;
        } else {
            damages = damageLabelInfo.getDamages();
            liveDamageInfo.put(id, damages);
        }

        long damageSum = damages.values().stream().mapToLong(d -> d).sum();
        areaDamageSet.put(damageSum);

        WynntilsMod.postEvent(new DamageDealtEvent(damages));

        lastDamageDealtTimestamp = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onLabelsRemoved(LabelsRemovedEvent event) {
        event.getRemovedLabels()
                .forEach(
                        labelInfo -> liveDamageInfo.remove(labelInfo.getEntity().getId()));
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        areaDamageSet.clear();
        focusedMobName = "";
        focusedMobElementals = "";
        focusedMobHealth = 0;
        focusedMobHealthPercent = CappedValue.EMPTY;
        focusedMobExpiryTime = -1L;
        lastDamageDealtTimestamp = 0L;
        liveDamageInfo.clear();
    }

    public String getFocusedMobName() {
        checkFocusedMobValidity();
        return focusedMobName;
    }

    public String getFocusedMobElementals() {
        checkFocusedMobValidity();
        // TODO: Parse this into specific elements and expose as functions
        return focusedMobElementals;
    }

    public long getFocusedMobHealth() {
        checkFocusedMobValidity();
        return focusedMobHealth;
    }

    public CappedValue getFocusedMobHealthPercent() {
        checkFocusedMobValidity();
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

    private void checkFocusedMobValidity() {
        if (focusedMobExpiryTime >= 0 && System.currentTimeMillis() >= focusedMobExpiryTime) {
            focusedMobName = "";
            focusedMobElementals = "";
            focusedMobHealth = 0;
            focusedMobHealthPercent = CappedValue.EMPTY;
            focusedMobExpiryTime = -1L;
        }
    }

    private void invalidateFocusedMob() {
        focusedMobExpiryTime = System.currentTimeMillis() + FOCUSED_MOB_INVALIDATION_DELAY;
    }

    private void revalidateFocusedMob() {
        focusedMobExpiryTime = -1L;
    }

    public final class DamageBar extends TrackedBar {
        private DamageBar() {
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

            checkFocusedMobValidity();
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
            revalidateFocusedMob();

            lastDamageDealtTimestamp = System.currentTimeMillis();
        }

        @Override
        public void onUpdateProgress(float progress) {
            focusedMobHealthPercent = new CappedValue(Math.round(progress * 100), 100);
            revalidateFocusedMob();

            lastDamageDealtTimestamp = System.currentTimeMillis();
        }

        @Override
        protected void reset() {
            invalidateFocusedMob();
        }
    }
}
