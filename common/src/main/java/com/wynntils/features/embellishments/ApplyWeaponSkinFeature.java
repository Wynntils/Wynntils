package com.wynntils.features.embellishments;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.ItemUsedEvent;
import com.wynntils.mc.event.PacketEvent;
import com.wynntils.mc.event.RenderArmWithItemEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.RequirementItemProperty;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;
import java.util.Optional;

public class ApplyWeaponSkinFeature extends Feature {
    private static final int MODEL_FLOAT_INDEX = 0;

    private boolean shouldPlayAnimation = false;
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
        if (itemStack == null || itemStack.isEmpty()) return;
        CustomModelData data = itemStack.getComponents().get(DataComponents.CUSTOM_MODEL_DATA);
        if (data == null) return;

        if (!isUsableWeapon(itemStack)) return;

        // Taken from ItemInHandRenderer
        // Have to manage equippedProgress ourselves since otherwise it will sometimes get reset
        // when Wynncraft sends a new ItemStack with weapon skin applied which causes an unpleasant visual effect
        event.setEquippedProgress(McUtils.mc().getItemModelResolver().swapAnimationScale(itemStack) * (1.0f - Mth.lerp(event.getPartialTick(), oHandHeight, handHeight)));

        if (data.getFloat(MODEL_FLOAT_INDEX).equals(value)) return;
        data.floats().set(MODEL_FLOAT_INDEX, value);
        shouldPlayAnimation = true;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        // Taken from ItemInHandRenderer
        oHandHeight = handHeight;
        LocalPlayer player = McUtils.player();
        if (player.isHandsBusy()) {
            handHeight = Mth.clamp(handHeight - 0.4f, 0.0f, 1.0f);
        } else {
            float f = player.getItemSwapScale(1.0f);
            float g = shouldPlayAnimation ? 0.0F : f * f * f;
            handHeight = handHeight + Mth.clamp(g - handHeight, -0.4f, 0.4f);
        }
        if (handHeight < 0.1f) {
			shouldPlayAnimation = false;
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
