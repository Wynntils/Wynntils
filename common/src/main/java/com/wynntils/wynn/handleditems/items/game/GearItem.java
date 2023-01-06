/*
 * Copyright © Wynntils 2022, 2023.
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

    public ItemProfile getItemProfile() {
        return itemProfile;
    }

    public List<ItemIdentificationContainer> getIdContainers() {
        return gearInstance.getIdContainers();
    }

    public List<Powder> getPowders() {
        return gearInstance.getPowders();
    }

    public int getRerolls() {
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
        return gearInstance.hasVariableIds();
    }

    public float getOverallPercentage() {
        return gearInstance.getOverallPercentage();
    }

    public boolean isPerfect() {
        return gearInstance.isPerfect();
    }

    public boolean isDefective() {
        return gearInstance.isDefective();
    }
}
