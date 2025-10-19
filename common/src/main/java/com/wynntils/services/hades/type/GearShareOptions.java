/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.hades.type;

import com.wynntils.models.inventory.type.InventoryAccessory;
import com.wynntils.models.inventory.type.InventoryArmor;
import java.util.Map;
import java.util.TreeMap;

public class GearShareOptions {
    private Map<InventoryArmor, Boolean> armorShare = new TreeMap<>();
    private Map<InventoryAccessory, Boolean> accessoryShare = new TreeMap<>();
    private boolean shareHeldItem = true;
    private boolean shareCraftedItems = true;
    private boolean shareCraftedNames = true;

    public boolean shouldShare() {
        return armorShare.values().stream().anyMatch(b -> b)
                || accessoryShare.values().stream().anyMatch(b -> b)
                || shareHeldItem;
    }

    public boolean shouldShareArmor(InventoryArmor armor) {
        return armorShare.getOrDefault(armor, true);
    }

    public void setShareArmor(InventoryArmor armor, boolean share) {
        armorShare.put(armor, share);
    }

    public boolean shouldShareAccessory(InventoryAccessory accessory) {
        return accessoryShare.getOrDefault(accessory, true);
    }

    public void setShareAccessory(InventoryAccessory accessory, boolean share) {
        accessoryShare.put(accessory, share);
    }

    public boolean shouldShareHeldItem() {
        return shareHeldItem;
    }

    public void setShareHeldItem(boolean shareHeldItem) {
        this.shareHeldItem = shareHeldItem;
    }

    public boolean shareCraftedItems() {
        return shareCraftedItems;
    }

    public void setShareCraftedItems(boolean shareCraftedItems) {
        this.shareCraftedItems = shareCraftedItems;
    }

    public boolean shareCraftedNames() {
        return shareCraftedNames;
    }

    public void setShareCraftedNames(boolean shareCraftedNames) {
        this.shareCraftedNames = shareCraftedNames;
    }
}
