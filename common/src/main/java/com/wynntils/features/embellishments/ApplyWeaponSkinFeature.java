/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.embellishments;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.store.StoreModel;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ItemUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.EMBELLISHMENTS)
public class ApplyWeaponSkinFeature extends Feature {
    public ApplyWeaponSkinFeature() {
        // Disabled by defaults due to only being useful on high ping and having problems with glint parsing
        super(ProfileDefault.DISABLED);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!Models.WorldState.onWorld()) return;
        Float value = Models.Store.getWeaponModel();
        if (value == null) return;

        ItemStack itemStack = McUtils.player().getMainHandItem();
        if (itemStack.isEmpty()) return;
        CustomModelData data = itemStack.getComponents().get(DataComponents.CUSTOM_MODEL_DATA);
        if (data == null) return;
        if (!ItemUtils.isUsableWeapon(itemStack)) return;

        if (data.getFloat(StoreModel.WEAPON_MODEL_FLOAT_INDEX).equals(value)) return;
        data.floats().set(StoreModel.WEAPON_MODEL_FLOAT_INDEX, value);

        PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) return;
        itemStack.set(
                DataComponents.POTION_CONTENTS,
                new PotionContents(
                        potionContents.potion(),
                        Optional.ofNullable(Models.Store.getGlint(StoreModel.WARDROBE_WEAPON_GLINT_SLOT)),
                        potionContents.customEffects(),
                        potionContents.customName()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        if (!Models.WorldState.onWorld()) return;
        Float value = Models.Store.getWeaponModel();
        if (value == null) return;

        List<ItemStack> items = event.getItems();
        Inventory inventory = McUtils.player().getInventory();

        ItemStack handItem = inventory.getSelectedItem();
        if (handItem.isEmpty()) return;
        CustomModelData handData = handItem.get(DataComponents.CUSTOM_MODEL_DATA);
        if (handData == null) return;
        if (!handData.getFloat(StoreModel.WEAPON_MODEL_FLOAT_INDEX).equals(value)) return;
        if (!ItemUtils.isUsableWeapon(handItem)) return;

        ItemStack itemStack = items.get(inventory.selected + Inventory.INVENTORY_SIZE);
        if (itemStack.isEmpty()) return;
        CustomModelData data = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (data == null) return;
        if (!ItemUtils.isUsableWeapon(itemStack)) return;

        // ItemInHandRenderer#tick() checks ItemStack changes by reference instead of ItemStack#matches
        // so to not trigger equipped animation we have to overwrite the ItemStack for selected slot
        // Wynncraft also sometimes sends unskinned weapon ItemStack which is why we don't check that
        // itemStack's model value is equal to handItem's
        items.set(inventory.selected + Inventory.INVENTORY_SIZE, handItem);
    }
}
