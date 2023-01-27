/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.concepts.Powder;
import com.wynntils.models.gear.GearIdentificationContainer;
import com.wynntils.models.gear.GearInstance;
import com.wynntils.models.gear.profile.GearProfile;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.stats.type.StatActualValue;
import java.util.List;
import net.minecraft.network.chat.Component;

public class GearItem extends GameItem implements GearTierItemProperty {
    private final GearProfile gearProfile;
    private final GearInstance gearInstance;

    public GearItem(
            GearProfile gearProfile,
            List<StatActualValue> identifications,
            List<GearIdentificationContainer> idContainers,
            List<Powder> powders,
            int rerolls,
            List<Component> setBonus) {
        this.gearProfile = gearProfile;
        this.gearInstance = new GearInstance(identifications, idContainers, powders, rerolls, setBonus);
    }

    public GearItem(GearProfile gearProfile, GearInstance gearInstance) {
        this.gearProfile = gearProfile;
        this.gearInstance = gearInstance;
    }

    public GearProfile getGearProfile() {
        return gearProfile;
    }

    public GearInstance getGearInstance() {
        return gearInstance;
    }

    public boolean isUnidentified() {
        return gearInstance == null;
    }

    public List<GearIdentificationContainer> getIdContainers() {
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
    public GearTier getGearTier() {
        return gearProfile.getTier();
    }

    @Override
    public String toString() {
        return "GearItem{" + "gearProfile=" + gearProfile + ", gearInstance=" + gearInstance + '}';
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
