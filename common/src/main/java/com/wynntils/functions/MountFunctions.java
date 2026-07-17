/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.type.MountStat;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.type.CappedValue;

import java.util.Optional;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class MountFunctions {

    private static Optional<CappedValue> getCappedStat(String statArg) {
        Optional<MountItem> mount = getMount();
        if (mount.isEmpty()) return Optional.empty();

        Optional<MountStat> stat = MountStat.fromKey(statArg);
        if (stat.isEmpty() || !stat.get().isCapped()) return Optional.empty();

        return Optional.of(getCappedStatValue(mount.get(), stat.get()));
    }

    private static Optional<Integer> getStatCurrent(String statArg) {
        Optional<MountItem> mount = getMount();
        if (mount.isEmpty()) return Optional.empty();

        return MountStat.fromKey(statArg).map(stat -> getStatCurrentValue(mount.get(), stat));
    }

    private static Optional<Integer> getStatMax(String statArg) {
        Optional<MountItem> mount = getMount();
        if (mount.isEmpty()) return Optional.empty();

        Optional<MountStat> stat = MountStat.fromKey(statArg);
        if (stat.isEmpty() || !stat.get().isCapped()) return Optional.empty();

        return Optional.of(getCappedStatValue(mount.get(), stat.get()).max());
    }

    @TemplateFunction(name = "capped_mount_stat", aliases = {"cap_mnt_stat"})
    public static CappedValue cappedMountStatFunction(String stat) {
        return getCappedStat(stat).orElse(CappedValue.EMPTY);
    }

    @TemplateFunction(name = "mount_stat", aliases = {"mnt_stat"})
    public static int mountStatFunction(String stat) {
        return getStatCurrent(stat).orElse(-1);
    }

    @TemplateFunction(name = "mount_stat_max", aliases = {"mnt_stat_max"})
    public static int mountStatMaxFunction(String stat) {
        return getStatMax(stat).orElse(-1);
    }

    @TemplateFunction(name = "mount_name", aliases = {"mnt_name"})
    public static String mountNameFunction() {
        return getMount().flatMap(MountItem::getName).orElse("");
    }

    @TemplateFunction(name = "current_mount_energy", aliases = {"mnt_energy"})
    public static CappedValue currentMountEnergyFunction(String stat) {
        return Models.Mount.getCurrentMountEnergy().orElse(CappedValue.EMPTY);
    }

    private static Optional<MountItem> getMount() {
        return Models.Mount.getMount();
    }

    private static int getStatCurrentValue(MountItem mount, MountStat stat) {
        return switch (stat) {
            case ACCELERATION -> mount.getAcceleration().current();
            case ALTITUDE -> mount.getAltitude().current();
            case JUMP_HEIGHT -> mount.getJumpHeight().current();
            case ENERGY -> mount.getEnergy().current();
            case HANDLING -> mount.getHandling().current();
            case POTENTIAL -> mount.getPotential();
            case BOOST -> mount.getBoost().current();
            case SPEED -> mount.getSpeed().current();
            case TOUGHNESS -> mount.getToughness().current();
            case TRAINING -> mount.getTraining().current();
        };
    }

    private static CappedValue getCappedStatValue(MountItem mount, MountStat stat) {
        return switch (stat) {
            case ACCELERATION -> mount.getAcceleration();
            case ALTITUDE -> mount.getAltitude();
            case JUMP_HEIGHT -> mount.getJumpHeight();
            case ENERGY -> mount.getEnergy();
            case HANDLING -> mount.getHandling();
            case BOOST -> mount.getBoost();
            case SPEED -> mount.getSpeed();
            case TOUGHNESS -> mount.getToughness();
            case TRAINING -> mount.getTraining();
            case POTENTIAL -> CappedValue.EMPTY;
        };
    }
}
