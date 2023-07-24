/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.damage;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.models.damage.type.DamageDealtEvent;
import com.wynntils.models.damage.type.FocusedDamageEvent;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.type.TimedSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class DamageModel extends Model {
    // https://regexr.com/7968a
    private static final Pattern DAMAGE_LABEL_PATTERN = Pattern.compile("(?:§[245bcef](?:§l)?-(\\d+) ([❤✦✤❉❋✹☠]) )");

    // https://regexr.com/7965g
    private static final Pattern DAMAGE_BAR_PATTERN = Pattern.compile("^§[ac](.*) - §c(\\d+)§4❤(?: - §7(.*)§7)?$");

    private final DamageBar damageBar = new DamageBar();

    private final TimedSet<Integer> areaDamageSet = new TimedSet<>(60, TimeUnit.SECONDS, true);

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

    @SubscribeEvent
    public void onLabelChange(EntityLabelChangedEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) return;

        Matcher matcher = event.getName().getMatcher(DAMAGE_LABEL_PATTERN);
        if (!matcher.find()) return;

        Map<DamageType, Integer> damages = new HashMap<>();
        // Restart finding from the beginning
        matcher.reset();
        while (matcher.find()) {
            int damage = Integer.parseInt(matcher.group(1));
            DamageType damageType = DamageType.fromSymbol(matcher.group(2));

            damages.put(damageType, damage);
        }

        WynntilsMod.postEvent(new DamageDealtEvent(damages));

        int damageSum = damages.values().stream().mapToInt(Integer::intValue).sum();
        areaDamageSet.put(damageSum);

        lastDamageDealtTimestamp = System.currentTimeMillis();
    }

    public int getAreaDamagePerSecond() {
        return areaDamageSet.getEntries().stream()
                .filter(timedEntry -> (System.currentTimeMillis() - timedEntry.getCreation()) <= 1000L)
                .mapToInt(TimedSet.TimedEntry::getEntry)
                .sum();
    }

    public double getAverageAreaDamagePerSecond(int seconds) {
        return areaDamageSet.getEntries().stream()
                        .filter(timedEntry ->
                                (System.currentTimeMillis() - timedEntry.getCreation()) <= seconds * 1000L)
                        .mapToInt(TimedSet.TimedEntry::getEntry)
                        .sum()
                / (double) seconds;
    }

    private final class DamageBar extends TrackedBar {
        private DamageBar() {
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
            lastDamageDealtTimestamp = System.currentTimeMillis();
        }
    }
}
