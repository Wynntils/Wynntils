package com.wynntils.models.store;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.containers.containers.CosmeticContainer;
import com.wynntils.models.store.type.CosmeticItemType;
import net.minecraft.core.component.DataComponents;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;

public class StoreModel extends Model {

    private static final int SELECTED_COSMETIC_SLOT = 4;

    private float weaponModel = 0.0f;

    public StoreModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Post event) {
        if (event.getSlot() != SELECTED_COSMETIC_SLOT) return;
        if (!(Models.Container.getCurrentContainer() instanceof CosmeticContainer container)) return;
        CosmeticItemType cosmeticItemType = container.getCosmeticItemType();
        if (cosmeticItemType == null) return;
        if (cosmeticItemType == CosmeticItemType.WEAPON_SKIN) {
            weaponModel = event.getItemStack().getComponents().get(DataComponents.CUSTOM_MODEL_DATA).getFloat(0);
        }
    }

    public float getWeaponModel() {
        return weaponModel;
    }
}
