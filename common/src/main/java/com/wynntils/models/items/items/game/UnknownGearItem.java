/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.stats.type.StatActualValue;
import java.util.List;

public class UnknownGearItem extends GameItem
        implements GearTierItemProperty, GearTypeItemProperty, LeveledItemProperty {
    private final String name;
    private final GearType gearType;
    private final GearTier gearTier;
    private final int level;
    // FIXME: Better types than strings...
    private final List<String> damages;
    private final List<String> requirements;
    private final List<StatActualValue> identifications;
    private final List<Powder> powders;
    private final int rerolls;

    public UnknownGearItem(
            int emeraldPrice,
            String name,
            GearType gearType,
            GearTier gearTier,
            int level,
            List<String> damages,
            List<String> requirements,
            List<StatActualValue> identifications,
            List<Powder> powders,
            int rerolls) {
        super(emeraldPrice);
        this.name = name;
        this.gearType = gearType;
        this.gearTier = gearTier;
        this.level = level;
        this.damages = damages;
        this.requirements = requirements;
        this.identifications = identifications;
        this.powders = powders;
        this.rerolls = rerolls;
    }

    public String getName() {
        return name;
    }

    @Override
    public GearType getGearType() {
        return gearType;
    }

    @Override
    public GearTier getGearTier() {
        return gearTier;
    }

    @Override
    public int getLevel() {
        return level;
    }

    public List<String> getDamages() {
        return damages;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public List<StatActualValue> getIdentifications() {
        return identifications;
    }

    public List<Powder> getPowders() {
        return powders;
    }

    public int getRerolls() {
        return rerolls;
    }

    @Override
    public String toString() {
        return "UnknownGearItem{" + "name='"
                + name + '\'' + ", gearType="
                + gearType + ", gearTier="
                + gearTier + ", level="
                + level + ", damages="
                + damages + ", requirements="
                + requirements + ", identifications="
                + identifications + ", powders="
                + powders + ", rerolls="
                + rerolls + ", emeraldPrice="
                + emeraldPrice + '}';
    }
}
