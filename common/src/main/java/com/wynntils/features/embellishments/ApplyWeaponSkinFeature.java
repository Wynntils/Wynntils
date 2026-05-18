package com.wynntils.features.embellishments;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;

public class ApplyWeaponSkinFeature extends Feature {

    public ApplyWeaponSkinFeature() {
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
