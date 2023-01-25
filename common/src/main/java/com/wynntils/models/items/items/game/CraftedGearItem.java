/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.concepts.Powder;
import com.wynntils.models.gear.GearIdentification;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class CraftedGearItem extends GameItem implements GearTierItemProperty, DurableItemProperty {
    // FIXME: Better types than strings...
    private final List<String> damages;
    private final List<String> requirements;
    private final List<GearIdentification> identifications;
    private final List<Powder> powders;
    private final CappedValue durability;

    public CraftedGearItem(
            List<String> damages,
            List<String> requirements,
            List<GearIdentification> identifications,
            List<Powder> powders,
            CappedValue durability) {
        this.damages = damages;
        this.requirements = requirements;
        this.identifications = identifications;
        this.powders = powders;
        this.durability = durability;
    }

    public List<GearIdentification> getIdentifications() {
        return identifications;
    }

    public List<Powder> getPowders() {
        return powders;
    }

    public List<String> getDamages() {
        return damages;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public CappedValue getDurability() {
        return durability;
    }

    @Override
    public GearTier getGearTier() {
        return GearTier.CRAFTED;
    }

    @Override
    public String toString() {
        return "CraftedGearItem{" + "damages="
                + damages + ", requirements="
                + requirements + ", identifications="
                + identifications + ", powders="
                + powders + ", durability="
                + durability + '}';
    }
}
