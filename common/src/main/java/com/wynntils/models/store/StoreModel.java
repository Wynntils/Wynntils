package com.wynntils.models.store;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.containers.containers.CosmeticContainer;
import com.wynntils.models.items.items.gui.StoreItem;
import com.wynntils.models.store.type.CosmeticItemType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;
import java.util.Optional;

public class StoreModel extends Model {

    private static final int SELECTED_COSMETIC_SLOT = 4;

    @Persisted
    private Storage<Float> weaponModel = new Storage<>(null);

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
        Optional<StoreItem> storeItemOpt = Models.Item.asWynnItem(itemStack, StoreItem.class);
        if (storeItemOpt.isEmpty()) {
            weaponModel = null;
            return;
        }
        if (cosmeticItemType == CosmeticItemType.WEAPON_SKIN) {
            weaponModel.store(itemStack.getComponents().get(DataComponents.CUSTOM_MODEL_DATA).getFloat(0));
        }
    }

    public Float getWeaponModel() {
        return weaponModel.get();
    }
}
