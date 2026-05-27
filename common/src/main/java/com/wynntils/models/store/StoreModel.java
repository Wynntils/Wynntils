/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.store;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.ServerResourcePackEvent;
import com.wynntils.models.containers.containers.CosmeticContainer;
import com.wynntils.models.items.items.gui.StoreItem;
import com.wynntils.models.store.type.CosmeticItemType;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.SubscribeEvent;
import org.jspecify.annotations.Nullable;

public class StoreModel extends Model {
    private static final int SELECTED_COSMETIC_SLOT = 4;

    public static final int WEAPON_MODEL_FLOAT_INDEX = 0;

    @Persisted
    private final Storage<Map<String, Float>> weaponModelStorage = new Storage<>(new TreeMap<>());

    public StoreModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Post event) {
        if (event.getSlot() != SELECTED_COSMETIC_SLOT) return;
        if (!(Models.Container.getCurrentContainer() instanceof CosmeticContainer container)) return;
        CosmeticItemType cosmeticItemType = container.getCosmeticItemType();
        if (cosmeticItemType == null) return;
        ItemStack itemStack = event.getItemStack();
        // Actual skins are StoreItems, while an empty selected skin is not
        boolean isStoreItem = Models.Item.asWynnItem(itemStack, StoreItem.class).isPresent();
        CustomModelData data = itemStack.getComponents().get(DataComponents.CUSTOM_MODEL_DATA);

        if (cosmeticItemType == CosmeticItemType.WEAPON_SKIN) {
            if (!isStoreItem || data == null) {
                putWeaponModel(null);
                return;
            }
            Float value = data.getFloat(WEAPON_MODEL_FLOAT_INDEX);
            if (value == null) {
                putWeaponModel(null);
                return;
            }
            putWeaponModel(value);
        }
    }

    @SubscribeEvent
    public void onServerResourcePackChange(ServerResourcePackEvent.Change event) {
        weaponModelStorage.store(new TreeMap<>());
    }

    @Nullable
    public Float getWeaponModel() {
        return weaponModelStorage.get().get(Models.Character.getId());
    }

    private void putWeaponModel(Float value) {
        weaponModelStorage.get().put(Models.Character.getId(), value);
        weaponModelStorage.touched();
    }
}
