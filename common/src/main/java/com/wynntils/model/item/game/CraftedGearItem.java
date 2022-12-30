/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.game;

import com.wynntils.model.item.properties.DurableItemProperty;
import com.wynntils.model.item.properties.GearTierItemProperty;
import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.objects.Powder;
import com.wynntils.wynn.objects.profiles.item.GearIdentification;
import com.wynntils.wynn.objects.profiles.item.ItemTier;
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
    public ItemTier getGearTier() {
        return ItemTier.CRAFTED;
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
