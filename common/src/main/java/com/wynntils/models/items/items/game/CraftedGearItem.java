/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class CraftedGearItem extends GameItem
        implements GearTierItemProperty, GearTypeItemProperty, DurableItemProperty, LeveledItemProperty {
    private final GearType gearType;
    private final int level;
    // FIXME: Better types than strings...
    private final List<String> damages;
    private final List<String> requirements;
    private final List<StatActualValue> identifications;
    private final List<Powder> powders;
    private final CappedValue durability;

    public CraftedGearItem(
            GearType gearType,
            int level,
            List<String> damages,
            List<String> requirements,
            List<StatActualValue> identifications,
            List<Powder> powders,
            CappedValue durability) {
        this.gearType = gearType;
        this.level = level;
        this.damages = damages;
        this.requirements = requirements;
        this.identifications = identifications;
        this.powders = powders;
        this.durability = durability;
    }

    @Override
    public GearType getGearType() {
        return gearType;
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

    @Override
    public CappedValue getDurability() {
        return durability;
    }

    @Override
    public GearTier getGearTier() {
        return GearTier.CRAFTED;
    }

    @Override
    public String toString() {
        return "CraftedGearItem{" + "gearType="
                + gearType + ", level="
                + level + ", damages="
                + damages + ", requirements="
                + requirements + ", identifications="
                + identifications + ", powders="
                + powders + ", durability="
                + durability + '}';
    }
}
