/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.handlers.labels.event.LabelsRemovedEvent;
import com.wynntils.handlers.labels.event.TextDisplayChangedEvent;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.models.combat.bossbar.DamageBar;
import com.wynntils.models.combat.label.DamageLabelInfo;
import com.wynntils.models.combat.label.DamageLabelParser;
import com.wynntils.models.combat.label.DebuffType;
import com.wynntils.models.combat.label.KillLabelInfo;
import com.wynntils.models.combat.label.KillLabelParser;
import com.wynntils.models.combat.label.MobDebuffsLabelInfo;
import com.wynntils.models.combat.label.MobDebuffsLabelParser;
import com.wynntils.models.combat.type.DamageDealtEvent;
import com.wynntils.models.combat.type.DebuffLabelEntry;
import com.wynntils.models.combat.type.FocusedDamageEvent;
import com.wynntils.models.combat.type.KillCreditType;
import com.wynntils.models.combat.type.MobElementals;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.TimedSet;
import com.wynntils.utils.wynn.RaycastUtils;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

public final class CombatModel extends Model {
    // Wynncraft updates the focused mob health bar by repeatedly destroying and recreating the boss bar. We don't want
    // to lose the entire focused mob state every time the boss bar is recreated, so we delay invalidation by this many
    // milliseconds and revalidate if a recreation/update event arrives during the delay
    private static final long FOCUSED_MOB_INVALIDATION_DELAY = 1000L;

    private final DamageBar damageBar = new DamageBar();

    private final TimedSet<Long> areaDamageSet = new TimedSet<>(60, TimeUnit.SECONDS, true);
    private final Map<Integer, Map<DamageType, Long>> liveDamageInfo = new HashMap<>();

    private final TimedSet<KillCreditType> killSet = new TimedSet<>(60, TimeUnit.SECONDS, true);

    private final Map<Integer, DebuffLabelEntry> debuffTextDisplays = new HashMap<>();

    private String focusedMobName = "";
    private MobElementals focusedMobElementals = MobElementals.EMPTY;
    private long focusedMobHealth;
    private CappedValue focusedMobHealthPercent = CappedValue.EMPTY;
    private long focusedMobExpiryTime = -1L;

    private long lastDamageDealtTimestamp;
    private long lastSharedKillTimestamp;
    private long lastSelfKillTimestamp;

    public CombatModel() {
        super(List.of());

        Handlers.BossBar.registerBar(damageBar);
        Handlers.Label.registerParser(new DamageLabelParser());
        Handlers.Label.registerParser(new KillLabelParser());
        Handlers.Label.registerParser(new MobDebuffsLabelParser());
    }

    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (event.getLabelInfo() instanceof DamageLabelInfo damageLabelInfo) {
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
        } else if (event.getLabelInfo() instanceof KillLabelInfo killLabelInfo) {
            killSet.put(killLabelInfo.getKillCredit());

            if (killLabelInfo.getKillCredit() == KillCreditType.SELF) {
                lastSelfKillTimestamp = System.currentTimeMillis();
            } else if (killLabelInfo.getKillCredit() == KillCreditType.SHARED) {
                lastSharedKillTimestamp = System.currentTimeMillis();
            }
        }
    }

    @SubscribeEvent
    public void onTextDisplayChanged(TextDisplayChangedEvent.Text event) {
        int id = event.getTextDisplay().getId();
        Optional<LabelInfo> labelInfo = event.getLabelInfo();

        if (labelInfo.isEmpty()) {
            debuffTextDisplays.remove(id);
            return;
        }

        if (labelInfo.get() instanceof MobDebuffsLabelInfo debuffInfo) {
            debuffTextDisplays.put(
                    debuffInfo.getEntity().getId(),
                    new DebuffLabelEntry(debuffInfo, (Display.TextDisplay) debuffInfo.getEntity()));
        } else {
            debuffTextDisplays.remove(id);
        }
    }

    @SubscribeEvent
    public void onLabelsRemoved(LabelsRemovedEvent event) {
        event.getRemovedLabels().forEach(label -> {
            int id = label.getEntity().getId();

            debuffTextDisplays.remove(id);
            liveDamageInfo.remove(id);
        });
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        areaDamageSet.clear();
        focusedMobName = "";
        focusedMobHealth = 0;
        focusedMobElementals = MobElementals.EMPTY;
        focusedMobHealthPercent = CappedValue.EMPTY;
        focusedMobExpiryTime = -1L;
        lastDamageDealtTimestamp = 0L;
        liveDamageInfo.clear();
    }

    public int getTargetedDebuffCount(
            double range, double horizontalFovDegrees, double verticalFovDegrees, DebuffType debuffType) {
        if (debuffTextDisplays.isEmpty()) return 0;

        Optional<Display.TextDisplay> targeted = RaycastUtils.getTargetedLabel(
                debuffTextDisplays.values().stream()
                        .map(DebuffLabelEntry::entity)
                        .toList(),
                range,
                horizontalFovDegrees,
                verticalFovDegrees);

        if (targeted.isEmpty()) return 0;

        int id = targeted.get().getId();
        DebuffLabelEntry entry = debuffTextDisplays.get(id);

        if (entry == null) return 0;

        return entry.info().getDebuffs().getOrDefault(debuffType, 0);
    }

    public int getDebuffCountInRadius(double radius, DebuffType debuffType) {
        if (debuffTextDisplays.isEmpty()) return 0;

        Vec3 center = McUtils.mc().gameRenderer.getMainCamera().position();

        int total = 0;

        for (DebuffLabelEntry entry : debuffTextDisplays.values()) {
            int count = entry.info().getDebuffs().getOrDefault(debuffType, 0);
            if (count == 0) continue;

            Display.TextDisplay display = entry.entity();
            Vec3 pos = display.position();
            double dx = pos.x - center.x;
            double dz = pos.z - center.z;

            if (dx * dx + dz * dz > radius * radius) continue;

            total += count;
        }

        return total;
    }

    public long getLastDamageDealtTimestamp() {
        return lastDamageDealtTimestamp;
    }

    public void setLastDamageDealtTimestamp(long lastDamageDealtTimestamp) {
        this.lastDamageDealtTimestamp = lastDamageDealtTimestamp;
    }

    public long getLastKillTimestamp(boolean includeShared) {
        return includeShared ? Math.max(lastSelfKillTimestamp, lastSharedKillTimestamp) : lastSelfKillTimestamp;
    }

    public void updateFocusedMob(String name, MobElementals elementals, long health) {
        focusedMobName = name;
        focusedMobElementals = elementals;
        focusedMobHealth = health;

        WynntilsMod.postEvent(new FocusedDamageEvent.MobFocused(name, elementals, health));
    }

    public String getFocusedMobName() {
        checkFocusedMobValidity();
        return focusedMobName;
    }

    public MobElementals getFocusedMobElementals() {
        checkFocusedMobValidity();
        // TODO: Parse this into specific elements and expose as functions
        return focusedMobElementals;
    }

    public long getFocusedMobHealth() {
        checkFocusedMobValidity();
        return focusedMobHealth;
    }

    public void updateFocusedMobHealth(long newHealth) {
        long oldHealth = this.focusedMobHealth;
        this.focusedMobHealth = newHealth;

        WynntilsMod.postEvent(
                new FocusedDamageEvent.MobDamaged(focusedMobName, focusedMobElementals, newHealth, oldHealth));
    }

    public CappedValue getFocusedMobHealthPercent() {
        checkFocusedMobValidity();
        return focusedMobHealthPercent;
    }

    public void updateFocusedMobHealthPercent(CappedValue newHealthPercent) {
        this.focusedMobHealthPercent = newHealthPercent;
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

    public double getTotalAreaDamageOverSeconds(int seconds) {
        return areaDamageSet.getEntries().stream()
                .filter(timedEntry -> (System.currentTimeMillis() - timedEntry.getCreation()) <= seconds * 1000L)
                .mapToLong(TimedSet.TimedEntry::getEntry)
                .sum();
    }

    public int getKillsPerMinute(boolean includeShared) {
        return killSet.getEntries().stream()
                .filter(creditType -> includeShared || creditType.getEntry() == KillCreditType.SELF)
                .filter(timedEntry -> (System.currentTimeMillis() - timedEntry.getCreation()) <= 60000L)
                .toList()
                .size();
    }

    public void checkFocusedMobValidity() {
        if (focusedMobExpiryTime >= 0 && System.currentTimeMillis() >= focusedMobExpiryTime) {
            focusedMobName = "";
            focusedMobElementals = MobElementals.EMPTY;
            focusedMobHealth = 0;
            focusedMobHealthPercent = CappedValue.EMPTY;
            focusedMobExpiryTime = -1L;
        }
    }

    public void invalidateFocusedMob() {
        focusedMobExpiryTime = System.currentTimeMillis() + FOCUSED_MOB_INVALIDATION_DELAY;
    }

    public void revalidateFocusedMob() {
        focusedMobExpiryTime = -1L;
    }
}
