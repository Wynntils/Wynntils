/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.UsesItemPropery;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class CraftedConsumableItem extends GameItem implements UsesItemPropery, GearTierItemProperty {
    private final String name;
    private final List<StatActualValue> identifications;
    private final List<ItemEffect> effects;
    private final CappedValue uses;

    public CraftedConsumableItem(
            String name, List<StatActualValue> identifications, List<ItemEffect> effects, CappedValue uses) {
        this.name = name;
        this.identifications = identifications;
        this.effects = effects;
        this.uses = uses;
    }

    public String getName() {
        return name;
    }

    public List<StatActualValue> getIdentifications() {
        return identifications;
    }

    public List<ItemEffect> getEffects() {
        return effects;
    }

    public CappedValue getUses() {
        return uses;
    }

    public GearTier getGearTier() {
        return GearTier.CRAFTED;
    }

    public boolean isHealing() {
        return getEffects().stream()
                .filter(e -> e.type().equals("Heal"))
                .findFirst()
                .isPresent();
    }

    @Override
    public String toString() {
        return "CraftedConsumableItem{" + "name='"
                + name + '\'' + ", identifications="
                + identifications + ", effects="
                + effects + ", uses="
                + uses + '}';
    }
}
