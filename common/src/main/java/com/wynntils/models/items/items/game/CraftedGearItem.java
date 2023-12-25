/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;

public class CraftedGearItem extends GameItem
        implements GearTierItemProperty, GearTypeItemProperty, DurableItemProperty, LeveledItemProperty {
    private final String name;
    private final GearType gearType;
    private final int health;
    private final int level;
    private final List<Pair<DamageType, RangedValue>> damages;
    private final List<Pair<Element, Integer>> defences;
    private final GearRequirements requirements;
    private final List<StatActualValue> identifications;
    private final List<Powder> powders;
    private final CappedValue durability;

    public CraftedGearItem(
            String name,
            GearType gearType,
            int health,
            int level,
            List<Pair<DamageType, RangedValue>> damages,
            List<Pair<Element, Integer>> defences,
            GearRequirements requirements,
            List<StatActualValue> identifications,
            List<Powder> powders,
            CappedValue durability) {
        this.name = name;
        this.gearType = gearType;
        this.health = health;
        this.level = level;
        this.damages = damages;
        this.defences = defences;
        this.requirements = requirements;
        this.identifications = identifications;
        this.powders = powders;
        this.durability = durability;
    }

    @Override
    public GearType getGearType() {
        return gearType;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public int getLevel() {
        return level;
    }

    public List<Pair<DamageType, RangedValue>> getDamages() {
        return damages;
    }

    public List<Pair<Element, Integer>> getDefences() {
        return defences;
    }

    public GearRequirements getRequirements() {
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
        return "CraftedGearItem{" + "name='"
                + name + '\'' + ", gearType="
                + gearType + ", health="
                + health + ", level="
                + level + ", damages="
                + damages + ", defences="
                + defences + ", requirements="
                + requirements + ", identifications="
                + identifications + ", powders="
                + powders + ", durability="
                + durability + '}';
    }
}
