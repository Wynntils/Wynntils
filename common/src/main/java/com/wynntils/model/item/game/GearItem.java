/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.game;

import com.wynntils.wynn.objects.Powder;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import java.util.List;
import net.minecraft.network.chat.Component;

public class GearItem extends GameItem {
    private final ItemProfile itemProfile;
    private final List<GearIdentification> identifications;
    private final List<Powder> powders;
    private final int rerolls;
    private final List<Component> setBonus;

    public GearItem(
            ItemProfile itemProfile,
            List<GearIdentification> identifications,
            List<Powder> powders,
            int rerolls,
            List<Component> setBonus) {
        this.itemProfile = itemProfile;
        this.identifications = identifications;
        this.powders = powders;
        this.rerolls = rerolls;
        this.setBonus = setBonus;
    }

    public ItemProfile getItemProfile() {
        return itemProfile;
    }

    public List<GearIdentification> getIdentifications() {
        return identifications;
    }

    public List<Powder> getPowders() {
        return powders;
    }

    public int getRerolls() {
        return rerolls;
    }

    public List<Component> getSetBonus() {
        return setBonus;
    }
}
