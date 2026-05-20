package com.wynntils.features.embellishments;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.mc.event.ItemUsedEvent;
import com.wynntils.mc.event.RenderArmWithItemEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.RequirementItemProperty;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Optional;

public class ApplyWeaponSkinFeature extends Feature {
    private static final int MODEL_FLOAT_INDEX = 0;

    private ItemStack lastTickItemStack = ItemStack.EMPTY;
    private ItemStack weaponItemStack;
    private boolean isRising = false;
    private boolean animationDone = false;
    private float oHandHeight;
    private float handHeight;

    public ApplyWeaponSkinFeature() {
        super(ProfileDefault.onlyDefault());
    }

    @SubscribeEvent
    public void onRenderArmWithItem(RenderArmWithItemEvent event) {
        if (!Models.WorldState.onWorld()) return;
        Float value = Models.Store.getWeaponModel();
        if (value == null) return;
        ItemStack itemStack = event.getItem();

        if (isUsableWeapon(itemStack)) return;

        // Have to manage equippedProgress ourselves since otherwise it will sometimes get reset
        // when Wynncraft sends a new ItemStack with weapon skin applied which causes an item to jump up and down
        event.setEquippedProgress(McUtils.mc().getItemModelResolver().swapAnimationScale(lastTickItemStack) * (1.0f - Mth.lerp(event.getPartialTick(), oHandHeight, handHeight)));
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!Models.WorldState.onWorld()) return;
        Float value = Models.Store.getWeaponModel();
        if (value == null) return;

        LocalPlayer player = McUtils.player();
        ItemStack itemStack = player.getMainHandItem();

        boolean itemStackChanged = lastTickItemStack != itemStack;

        lastTickItemStack = itemStack;

        CustomModelData data = itemStack.getComponents().get(DataComponents.CUSTOM_MODEL_DATA);
        if (data == null) return;

        if (!isUsableWeapon(itemStack)) return;

        // If we swapped back to reskinned weapon, restart the animation
        if (itemStackChanged && weaponItemStack != null && ItemStack.matches(weaponItemStack, itemStack)) {
            animationDone = false;
        }

        if (animationDone) {
            // Force item to be at full height
            handHeight = 1.0f;
            oHandHeight = 1.0f;
        } else {
            // Adapted from ItemInHandRenderer
            oHandHeight = handHeight;
            if (player.isHandsBusy()) {
                handHeight = Mth.clamp(handHeight - 0.4f, 0.0f, 1.0f);
            } else {
                float f = player.getItemSwapScale(1.0f);
                float g = !isRising ? 0.0F : f * f * f;
                handHeight = handHeight + Mth.clamp(g - handHeight, -0.4f, 0.4f);
            }
            if (handHeight < 0.1f) {
                isRising = true;
            }
            if (handHeight >= 0.9f) {
                animationDone = true;
                isRising = false;
            }
        }

        if (!data.getFloat(MODEL_FLOAT_INDEX).equals(value)) {
            data.floats().set(MODEL_FLOAT_INDEX, value);
            animationDone = false;
            weaponItemStack = itemStack;
        }
    }

    @SubscribeEvent
    public void onItemUsed(ItemUsedEvent event) {
        handHeight = 0.0f;
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
