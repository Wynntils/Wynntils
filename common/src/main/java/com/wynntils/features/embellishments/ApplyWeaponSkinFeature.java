package com.wynntils.features.embellishments;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.mc.event.DataComponentGetEvent;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.RenderArmWithItemEvent;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.RequirementItemProperty;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Optional;

public class ApplyWeaponSkinFeature extends Feature {
    private static final int MODEL_FLOAT_INDEX = 0;

    public ApplyWeaponSkinFeature() {
        super(ProfileDefault.onlyDefault());
    }

    @SubscribeEvent
    public void onRenderArmWithItem(RenderArmWithItemEvent event) {
        Float value = Models.Store.getWeaponModel();
        if (value == null) return;

        ItemStack itemStack = event.getItem();
        if (!ItemStack.matches(itemStack, McUtils.player().getMainHandItem())) return;
        CustomModelData data = itemStack.getComponents().get(DataComponents.CUSTOM_MODEL_DATA);
        if (data.getFloat(MODEL_FLOAT_INDEX).equals(value)) return;

        Optional<GearTypeItemProperty> gearItemOpt = Models.Item.asWynnItemProperty(itemStack, GearTypeItemProperty.class);
        if (gearItemOpt.isEmpty()) return;
        if (Models.Character.getClassType() != gearItemOpt.get().getGearType().getClassReq()) return;

        Optional<RequirementItemProperty> reqItemOpt = Models.Item.asWynnItemProperty(itemStack, RequirementItemProperty.class);
        if (reqItemOpt.isEmpty()) return;
        if (!reqItemOpt.get().meetsActualRequirements()) return;

        data.floats().set(MODEL_FLOAT_INDEX, value);

//        event.setEquippedProgress(1.0f);
    }

    @SubscribeEvent
    public void onHotbarSlotRender(HotbarSlotRenderEvent.Pre event) {

    }


    @SubscribeEvent
    public void onGetModelData(DataComponentGetEvent.CustomModelData event) {
        Float value = Models.Store.getWeaponModel();
        if (value == null) return;

        ItemStack itemStack = event.getItemStack();
        if (!ItemStack.matches(itemStack, McUtils.player().getMainHandItem())) return;
        CustomModelData data = itemStack.getComponents().get(DataComponents.CUSTOM_MODEL_DATA);

        if (!data.getFloat(MODEL_FLOAT_INDEX).equals(value)) return;
        if (event.getValue().getFloat(MODEL_FLOAT_INDEX).equals(value)) return;
    }
}
