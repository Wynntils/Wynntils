/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.wynn.handleditems.properties.GearTierItemProperty;
import com.wynntils.wynn.objects.GearInstance;
import com.wynntils.wynn.objects.ItemIdentificationContainer;
import com.wynntils.wynn.objects.Powder;
import com.wynntils.wynn.objects.profiles.item.GearIdentification;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.objects.profiles.item.ItemTier;
import java.util.List;
import net.minecraft.network.chat.Component;

public class GearItem extends GameItem implements GearTierItemProperty {
    private final ItemProfile itemProfile;
    private final GearInstance gearInstance;

    public GearItem(
            ItemProfile itemProfile,
            List<GearIdentification> identifications,
            List<ItemIdentificationContainer> idContainers,
            List<Powder> powders,
            int rerolls,
            List<Component> setBonus) {
        this.itemProfile = itemProfile;
        this.gearInstance = new GearInstance(identifications, idContainers, powders, rerolls, setBonus);
    }

    public GearItem(ItemProfile itemProfile, GearInstance gearInstance) {
        this.itemProfile = itemProfile;
        this.gearInstance = gearInstance;
    }

    public ItemProfile getItemProfile() {
        return itemProfile;
    }

    public GearInstance getGearInstance() {
        return gearInstance;
    }

    public boolean isUnidentified() {
        return gearInstance == null;
    }

    public List<ItemIdentificationContainer> getIdContainers() {
        if (gearInstance == null) return List.of();

        return gearInstance.getIdContainers();
    }

    public List<Powder> getPowders() {
        if (gearInstance == null) return List.of();

        return gearInstance.getPowders();
    }

    public int getRerolls() {
        if (gearInstance == null) return 0;

        return gearInstance.getRerolls();
    }

    @Override
    public ItemTier getGearTier() {
        return itemProfile.getTier();
    }

    @Override
    public String toString() {
        return "GearItem{" + "itemProfile=" + itemProfile + ", gearInstance=" + gearInstance + '}';
    }

    public boolean hasVariableIds() {
        if (gearInstance == null) return false;

        return gearInstance.hasVariableIds();
    }

    public float getOverallPercentage() {
        if (gearInstance == null) return 0;

        return gearInstance.getOverallPercentage();
    }

    public boolean isPerfect() {
        if (gearInstance == null) return false;

        return gearInstance.isPerfect();
    }

    public boolean isDefective() {
        if (gearInstance == null) return false;

        return gearInstance.isDefective();
    }
}
