/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.store;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.ServerResourcePackEvent;
import com.wynntils.models.containers.containers.CosmeticContainer;
import com.wynntils.models.items.items.gui.StoreItem;
import com.wynntils.models.store.type.CosmeticItemType;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.SubscribeEvent;
import org.jspecify.annotations.Nullable;

public class StoreModel extends Model {
    private static final int SELECTED_COSMETIC_SLOT = 4;

    public static final int WARDROBE_WEAPON_GLINT_SLOT = 5;
    private static final int WARDROBE_WEAPON_SLOT = 6;

    public static final int WEAPON_MODEL_FLOAT_INDEX = 0;

    @Persisted
    private final Storage<Map<String, Float>> weaponModelStorage = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<String, Int2ObjectMap<Integer>>> glintStorage = new Storage<>(new TreeMap<>());

    public StoreModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Post event) {
        if (event.getSlot() != SELECTED_COSMETIC_SLOT) return;
        if (!(Models.Container.getCurrentContainer() instanceof CosmeticContainer container)) return;
        CosmeticItemType cosmeticItemType = container.getCosmeticItemType();
        if (cosmeticItemType == null) return;
        parseCosmeticItem(cosmeticItemType, event.getItemStack());
    }

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent.Post event) {
        List<ItemStack> items = event.getItems();
        int itemsSize = items.size();
        if (Models.Container.getCurrentContainer() instanceof CosmeticContainer container) {
            if (itemsSize > SELECTED_COSMETIC_SLOT) {
                parseCosmeticItem(container.getCosmeticItemType(), items.get(SELECTED_COSMETIC_SLOT));
            }
        } else if (Models.WorldState.inCharacterWardrobe()) {
            if (itemsSize > WARDROBE_WEAPON_SLOT) {
                parseCosmeticItem(CosmeticItemType.WEAPON_SKIN, items.get(WARDROBE_WEAPON_SLOT));
            }
            if (itemsSize > WARDROBE_WEAPON_GLINT_SLOT) {
                parseGlint(WARDROBE_WEAPON_GLINT_SLOT, items.get(WARDROBE_WEAPON_GLINT_SLOT));
            }
        }
    }

    private void parseCosmeticItem(CosmeticItemType type, ItemStack itemStack) {
        // Actual skins are StoreItems, while an empty selected skin is not
        boolean isStoreItem = Models.Item.asWynnItem(itemStack, StoreItem.class).isPresent();
        CustomModelData data = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);

        if (type == CosmeticItemType.WEAPON_SKIN) {
            if (!isStoreItem || data == null || data.floats().size() <= WEAPON_MODEL_FLOAT_INDEX) {
                putWeaponModel(null);
                return;
            }
            putWeaponModel(data.getFloat(WEAPON_MODEL_FLOAT_INDEX));
        }
    }

    private void parseGlint(int slot, ItemStack itemStack) {
        PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) {
            putGlint(slot, null);
            return;
        }
        putGlint(slot, potionContents.customColor().orElse(null));
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

    @Nullable
    public Integer getGlint(int slot) {
        Int2ObjectMap<Integer> map = glintStorage.get().get(Models.Character.getId());
        return map == null ? null : map.get(slot);
    }

    private void putGlint(int slot, Integer value) {
        glintStorage
                .get()
                .computeIfAbsent(Models.Character.getId(), i -> new Int2ObjectArrayMap<>())
                .put(slot, value);
        glintStorage.touched();
    }
}
