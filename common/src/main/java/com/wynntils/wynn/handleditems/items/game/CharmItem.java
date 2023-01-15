/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.wynn.handleditems.properties.GearTierItemProperty;
import com.wynntils.wynn.objects.profiles.item.CharmProfile;
import com.wynntils.wynn.gear.types.GearIdentification;
import com.wynntils.wynn.objects.profiles.item.GearTier;
import java.util.List;

public class CharmItem extends GameItem implements GearTierItemProperty {
    private final CharmProfile charmProfile;
    private final List<GearIdentification> identifications;
    private final int rerolls;

    public CharmItem(CharmProfile charmProfile, List<GearIdentification> identifications, int rerolls) {
        this.charmProfile = charmProfile;
        this.identifications = identifications;
        this.rerolls = rerolls;
    }

    public CharmProfile getCharmProfile() {
        return charmProfile;
    }

    public List<GearIdentification> getIdentifications() {
        return identifications;
    }

    public int getRerolls() {
        return rerolls;
    }

    @Override
    public GearTier getGearTier() {
        return charmProfile.tier();
    }

    @Override
    public String toString() {
        return "CharmItem{" + "charmProfile="
                + charmProfile + ", identifications="
                + identifications + ", rerolls="
                + rerolls + '}';
    }
}
