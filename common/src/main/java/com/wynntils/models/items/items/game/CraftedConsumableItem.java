/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.UsesItemPropery;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class CraftedConsumableItem extends GameItem
        implements UsesItemPropery, GearTierItemProperty, LeveledItemProperty {
    private final String name;
    private final int level;
    private final List<StatActualValue> identifications;
    private final List<ItemEffect> effects;
    private final CappedValue uses;

    public CraftedConsumableItem(
            String name, int level, List<StatActualValue> identifications, List<ItemEffect> effects, CappedValue uses) {
        this.name = name;
        this.level = level;
        this.identifications = identifications;
        this.effects = effects;
        this.uses = uses;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getLevel() {
        return level;
    }

    public List<StatActualValue> getIdentifications() {
        return identifications;
    }

    public List<ItemEffect> getEffects() {
        return effects;
    }

    @Override
    public CappedValue getUses() {
        return uses;
    }

    @Override
    public GearTier getGearTier() {
        return GearTier.CRAFTED;
    }

    public boolean isHealing() {
        return getEffects().stream().anyMatch(e -> e.type().equals("Heal"));
    }

    @Override
    public String toString() {
        return "CraftedConsumableItem{" + "name='"
                + name + '\'' + ", level="
                + level + ", identifications="
                + identifications + ", effects="
                + effects + ", uses="
                + uses + '}';
    }
}
