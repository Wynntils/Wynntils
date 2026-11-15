/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Services;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.mc.event.SetEntityDataEvent;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Vector3fc;

// This model could in the future be turned into a more general model for other spells that get attached to the player.
public final class ShamanRitualCircleModel extends Model {
    private static final String RITUAL_CIRCLE_GROUP = "Ritual Circle";

    // The circle's display scale grows by this much per radius (radius 1 = 31.968, radius 2 = 63.936, ...)
    private static final float RADIUS_SCALE_STEP = 31.968f;
    private final Set<Integer> circleDisplayIds = new HashSet<>();

    private int radius = 0;

    public ShamanRitualCircleModel() {
        super(List.of());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySetData(SetEntityDataEvent event) {
        if (McUtils.mc().level == null) return;

        Entity entity = McUtils.mc().level.getEntity(event.getId());
        if (!(entity instanceof Display.ItemDisplay display)) return;

        // Walk up the chain: ritual circle display -> root display -> local player.
        Entity root = display.getVehicle();
        if (root == null || root.getVehicle() != McUtils.player()) return;

        if (!isRitualCircleDisplay(display)) return;

        Vector3fc scale = display.getEntityData().get(Display.DATA_SCALE_ID);
        circleDisplayIds.add(display.getId());
        radius = Math.max(1, Math.round(scale.x() / RADIUS_SCALE_STEP));
    }

    @SubscribeEvent
    public void onEntityRemoved(RemoveEntitiesEvent.Post event) {
        event.getEntityIds().forEach(circleDisplayIds::remove);

        if (circleDisplayIds.isEmpty()) {
            radius = 0;
        }
    }

    @SubscribeEvent
    public void onClassChange(CharacterUpdateEvent e) {
        clearAll();
    }

    @SubscribeEvent
    public void onWorldStateChanged(WorldStateEvent e) {
        clearAll();
    }

    public int getRadius() {
        return radius;
    }

    private void clearAll() {
        circleDisplayIds.clear();
        radius = 0;
    }

    private static boolean isRitualCircleDisplay(Display.ItemDisplay display) {
        ItemStack itemStack = display.getEntityData().get(Display.ItemDisplay.DATA_ITEM_STACK_ID);
        if (!itemStack.is(Items.OAK_BOAT)) return false;

        CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (customModelData == null) return false;

        for (Float modelId : customModelData.floats()) {
            Optional<String> group = Services.CustomModel.getGroup(modelId);
            if (group.isPresent() && group.get().equals(RITUAL_CIRCLE_GROUP)) return true;
        }

        return false;
    }
}
