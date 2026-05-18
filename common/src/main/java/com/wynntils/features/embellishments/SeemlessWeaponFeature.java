package com.wynntils.features.embellishments;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.mc.event.DataComponentGetEvent;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Optional;

public class SeemlessWeaponFeature extends Feature {

    public SeemlessWeaponFeature() {
        super(ProfileDefault.onlyDefault());
    }


//    @SubscribeEvent
//    public void onGetModelData(DataComponentGetEvent.CustomModelData event) {
//        if (true) return;
//        ItemStack itemStack = event.getItemStack();
//        if (!itemStack.equals(McUtils.player().getMainHandItem())) return;
//        Optional<GearTypeItemProperty> gearItemOpt = Models.Item.asWynnItemProperty(itemStack, GearTypeItemProperty.class);
//        if (gearItemOpt.isEmpty()) return;
//        if (Models.Character.getClassType() != gearItemOpt.get().getGearType().getClassReq()) return;
//        System.out.println("gearItemModel " + itemStack.getComponents().get(DataComponents.CUSTOM_MODEL_DATA));
//    }
}
