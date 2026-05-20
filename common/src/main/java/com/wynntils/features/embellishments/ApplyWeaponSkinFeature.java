package com.wynntils.features.embellishments;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.RequirementItemProperty;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;
import java.util.Optional;

public class ApplyWeaponSkinFeature extends Feature {
    private static final int MODEL_FLOAT_INDEX = 0;

    public ApplyWeaponSkinFeature() {
        super(ProfileDefault.onlyDefault());
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
        if (!isUsableWeapon(itemStack)) return;

        if (!data.getFloat(MODEL_FLOAT_INDEX).equals(value)) {
            data.floats().set(MODEL_FLOAT_INDEX, value);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        if (!Models.WorldState.onWorld()) return;
        Float value = Models.Store.getWeaponModel();
        if (value == null) return;

        ItemStack handItem = McUtils.player().getMainHandItem();
        if (handItem.isEmpty()) return;
        CustomModelData handData = handItem.get(DataComponents.CUSTOM_MODEL_DATA);
        if (handData == null) return;
        if (!isUsableWeapon(handItem)) return;

        List<ItemStack> items = event.getItems();
        for (int i = 36; i < 45; i++) {
            if (!handData.getFloat(MODEL_FLOAT_INDEX).equals(value)) continue;
            ItemStack itemStack = items.get(i);
            if (itemStack.isEmpty()) continue;
            CustomModelData data = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (data == null) continue;
            if (!isUsableWeapon(itemStack)) continue;
            if (!data.getFloat(MODEL_FLOAT_INDEX).equals(value)) continue;
            items.set(i, handItem);
        }
    }

    private static boolean isUsableWeapon(ItemStack itemStack) {
        Optional<GearTypeItemProperty> gearItemOpt = Models.Item.asWynnItemProperty(itemStack, GearTypeItemProperty.class);
        if (gearItemOpt.isEmpty()) return false;
        if (!gearItemOpt.get().getGearType().isValidWeapon(Models.Character.getClassType())) return false;

        Optional<RequirementItemProperty> reqItemOpt = Models.Item.asWynnItemProperty(itemStack, RequirementItemProperty.class);
        if (reqItemOpt.isEmpty()) return false;
        if (!reqItemOpt.get().meetsActualRequirements()) return false;

        return true;
    }
}
