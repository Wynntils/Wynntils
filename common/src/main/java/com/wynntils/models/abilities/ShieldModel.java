/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.mc.event.AddEntityEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.mc.event.SetEntityDataEvent;
import com.wynntils.models.abilities.type.ArrowShield;
import com.wynntils.models.abilities.type.GuardianAngelsShield;
import com.wynntils.models.abilities.type.MantleShield;
import com.wynntils.models.abilities.type.ShieldType;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
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

public final class ShieldModel extends Model {
    private static final double SEARCH_RADIUS = 4.5;
    private static final int CAST_MAX_DELAY_MS = 250;

    private final List<ShieldType> shieldTypes = new ArrayList<>();

    private final Set<Integer> spawnedDuringCastWindow = new HashSet<>();
    private final Set<Integer> collectedRootIds = new HashSet<>();

    private List<Integer> spawnedIds;
    private long shieldCastTime = 0;
    private ShieldType activeShieldType;
    private String activeShieldGroup;
    private String pendingShieldGroup;

    public ShieldModel() {
        super(List.of());
        registerShieldTypes();
    }

    @SubscribeEvent
    public void onShieldCast(SpellEvent.Cast e) {
        for (ShieldType shieldType : shieldTypes) {
            if (!shieldType.validSpell(e.getSpellType())) continue;

            shieldCastTime = System.currentTimeMillis();
            collectedRootIds.clear();

            // Entities that spawned just before the cast are already in spawnedDuringCastWindow
            // but were skipped in onEntitySetData because isValidSpawn() was false then.
            // Re-process them now that shieldCastTime is set.
            for (int id : new HashSet<>(spawnedDuringCastWindow)) {
                Entity entity = McUtils.mc().level.getEntity(id);
                if (!(entity instanceof Display.ItemDisplay)) continue;
                if (entity.position().distanceTo(McUtils.player().position()) > SEARCH_RADIUS) continue;

                processShieldEntity(id);
            }

            break;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySetData(SetEntityDataEvent event) {
        Entity entity = McUtils.mc().level.getEntity(event.getId());
        if (!(entity instanceof Display.ItemDisplay)) return;

        if (!isValidSpawn() && !isGuardianAngelsTransition(entity)) return;

        if (entity.position().distanceTo(McUtils.player().position()) > SEARCH_RADIUS) return;
        if (collectedRootIds.contains(entity.getId())) return;
        if (!spawnedDuringCastWindow.contains(entity.getId())) return;

        processShieldEntity(entity.getId());
    }

    private void processShieldEntity(int entityId) {
        Entity entity = McUtils.mc().level.getEntity(entityId);
        if (!(entity instanceof Display.ItemDisplay)) return;

        if (collectedRootIds.contains(entityId)) return;

        for (Entity passenger : entity.getPassengers()) {
            if (!(passenger instanceof Display.ItemDisplay passengerDisplay)) continue;

            ItemStack itemStack = passengerDisplay.getEntityData().get(Display.ItemDisplay.DATA_ITEM_STACK_ID);

            if (!itemStack.is(Items.OAK_BOAT)) continue;

            CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);

            if (customModelData == null || customModelData.floats().isEmpty()) continue;

            float modelId = customModelData.floats().getFirst();

            for (ShieldType shieldType : shieldTypes) {
                if (!shieldType.verifyShield(modelId)) continue;

                collectedRootIds.add(entityId);
                activeShieldType = shieldType;
                pendingShieldGroup = Services.CustomModel.getGroup(modelId).orElse(null);

                Managers.TickScheduler.scheduleLater(this::registerShield, 4);
                return;
            }
        }
    }

    /**
     * this is a temporary fix, because SpellEvent does not support ultimates yet, which means
     * isValidSpawn() never becomes true when switching from normal ga to ult ga or vice versa.
     * to remove the fix we also need a way of knowing when the ult expires, because that also won't make isValidSpawn() true
     */
    private boolean isGuardianAngelsTransition(Entity entity) {
        if (!(activeShieldType instanceof GuardianAngelsShield)) return false;
        if (spawnedIds == null || spawnedIds.isEmpty()) return false;

        for (Entity passenger : entity.getPassengers()) {
            if (!(passenger instanceof Display.ItemDisplay passengerDisplay)) continue;

            ItemStack itemStack = passengerDisplay.getEntityData().get(Display.ItemDisplay.DATA_ITEM_STACK_ID);
            if (!itemStack.is(Items.OAK_BOAT)) continue;

            CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (customModelData == null || customModelData.floats().isEmpty()) continue;

            float modelId = customModelData.floats().getFirst();
            Optional<String> group = Services.CustomModel.getGroup(modelId);
            if (group.isEmpty()) continue;

            if (group.get().equals(activeShieldGroup)) return false;
            return group.get().equals(GuardianAngelsShield.GROUP) || group.get().equals(GuardianAngelsShield.ULT_GROUP);
        }

        return false;
    }

    @SubscribeEvent
    public void onEntitySpawn(AddEntityEvent e) {
        // entities are added before onShieldCast is fired, so we have to do it like this.
        if (!(e.getEntity() instanceof Display.ItemDisplay)) return;
        if (e.getEntity().position().distanceTo(McUtils.player().position()) > SEARCH_RADIUS) return;

        int id = e.getId();
        spawnedDuringCastWindow.add(id);

        // if this is longer than 5 ticks it can cause duplicates while spamming the spell, so there will be 2x as many
        // shields.
        Managers.TickScheduler.scheduleLater(() -> spawnedDuringCastWindow.remove(id), 5);
    }

    @SubscribeEvent
    public void onShieldDestroy(RemoveEntitiesEvent event) {
        event.getEntityIds().forEach(collectedRootIds::remove);

        if (spawnedIds == null) return;

        boolean changed = spawnedIds.removeAll(event.getEntityIds());
        if (changed && spawnedIds.isEmpty() && collectedRootIds.isEmpty()) {
            removeShield();
        }
    }

    @SubscribeEvent
    public void onClassChange(CharacterUpdateEvent e) {
        removeShield();
    }

    @SubscribeEvent
    public void onWorldStateChanged(WorldStateEvent e) {
        removeShield();
    }

    public int getShieldCharge() {
        return spawnedIds == null ? 0 : spawnedIds.size();
    }

    public ShieldType getActiveShieldType() {
        return activeShieldType;
    }

    private void registerShield() {
        if (!collectedRootIds.isEmpty()) {
            spawnedIds = new ArrayList<>(collectedRootIds);
            collectedRootIds.clear();
            activeShieldGroup = pendingShieldGroup;
            pendingShieldGroup = null;
        }
    }

    private void removeShield() {
        spawnedIds = null;
        activeShieldType = null;
        activeShieldGroup = null;
        pendingShieldGroup = null;
        collectedRootIds.clear();
    }

    /**
     * @return true if there was either a valid cast recently, or there is a possibility of an auto cast
     */
    private boolean isValidSpawn() {
        return System.currentTimeMillis() - shieldCastTime < CAST_MAX_DELAY_MS || Models.Inventory.hasAutoCasterItem();
    }

    private void registerShieldTypes() {
        registerShieldType(new ArrowShield());
        registerShieldType(new GuardianAngelsShield());
        registerShieldType(new MantleShield());
    }

    private void registerShieldType(ShieldType shieldType) {
        shieldTypes.add(shieldType);
    }
}
