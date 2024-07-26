/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.utils.EnumUtils;
import java.util.List;
import java.util.Optional;

public class GearTypeStatProvider extends ItemStatProvider<String> {
    @Override
    public Optional<String> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof GearTypeItemProperty gearTypeItemProperty)) return Optional.empty();

        return Optional.of(
                EnumUtils.toNiceString(gearTypeItemProperty.getGearType().name()));
    }

    @Override
    public List<String> getValidInputs() {
        // Can't use all values of the enum as WEAPON, ACCESSORY, MASTERY_TOME and CHARM
        // don't work with this filter, ItemTypeStatProvider should be used for tomes and charms
        return List.of(
                EnumUtils.toNiceString(GearType.HELMET.name()),
                EnumUtils.toNiceString(GearType.CHESTPLATE.name()),
                EnumUtils.toNiceString(GearType.LEGGINGS.name()),
                EnumUtils.toNiceString(GearType.BOOTS.name()),
                EnumUtils.toNiceString(GearType.RING.name()),
                EnumUtils.toNiceString(GearType.BRACELET.name()),
                EnumUtils.toNiceString(GearType.NECKLACE.name()),
                EnumUtils.toNiceString(GearType.SPEAR.name()),
                EnumUtils.toNiceString(GearType.WAND.name()),
                EnumUtils.toNiceString(GearType.DAGGER.name()),
                EnumUtils.toNiceString(GearType.BOW.name()),
                EnumUtils.toNiceString(GearType.RELIK.name()));
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GEAR);
    }
}
