/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.AddEntityEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.mc.event.SetEntityDataEvent;
import com.wynntils.models.abilities.type.ArrowShieldAbility;
import com.wynntils.models.abilities.type.CastedAbilityType;
import com.wynntils.models.abilities.type.GuardianAngelsAbility;
import com.wynntils.models.abilities.type.MantleAbility;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.type.SpellType;
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

public final class CastedAbilityModel extends Model {
    private static final int CAST_MAX_DELAY_MS = 250;

    private final List<CastedAbilityType> abilityTypes = new ArrayList<>();

    private final Set<Integer> recentItemDisplayIds = new HashSet<>();
    private final Set<Integer> mainVehicleIds = new HashSet<>();

    private long lastCastTime = 0;
    private SpellType lastCastSpell;

    public CastedAbilityModel() {
        super(List.of());
        registerAbilityTypes();
    }

    @SubscribeEvent
    public void onSpellCast(SpellEvent.Cast e) {
        lastCastTime = System.currentTimeMillis();
        lastCastSpell = e.getSpellType();

        for (int id : new HashSet<>(recentItemDisplayIds)) {
            processEntity(id, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySetData(SetEntityDataEvent event) {
        Entity entity = McUtils.mc().level.getEntity(event.getId());
        if (!(entity instanceof Display.ItemDisplay)) return;

        boolean withinCastWindow = isWithinCastWindow();

        processEntity(entity.getId(), withinCastWindow);
    }

    private void processEntity(int entityId, boolean withinCastWindow) {
        Entity entity = McUtils.mc().level.getEntity(entityId);
        if (!(entity instanceof Display.ItemDisplay)) return;
        if (entity.getPassengers().isEmpty()) return;
        if (mainVehicleIds.contains(entityId)) return;
        if (!recentItemDisplayIds.contains(entity.getId())) return;

        List<Float> modelIds = extractModelIdsFromPassengers(entity);
        if (modelIds.isEmpty()) return;

        for (CastedAbilityType type : abilityTypes) {
            if (!type.validClass()) continue;
            if (type.isWithinProximity(entity)) continue;

            if (withinCastWindow) {
                if (!type.validSpell(lastCastSpell)) continue;
            } else {
                if (!type.allowsOutOfWindowSpawn(modelIds)) continue;
            }

            if (!type.verifyCustomModelData(modelIds)) continue;
            if (isBlocked(type)) continue;

            mainVehicleIds.add(entityId);
            type.onMatched(entityId, modelIds);
            return;
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(AddEntityEvent e) {
        if (!(e.getEntity() instanceof Display.ItemDisplay)) return;

        int id = e.getId();
        recentItemDisplayIds.add(id);

        Managers.TickScheduler.scheduleLater(() -> recentItemDisplayIds.remove(id), 5);
    }

    @SubscribeEvent
    public void onEntityRemoved(RemoveEntitiesEvent event) {
        event.getEntityIds().forEach(mainVehicleIds::remove);
        abilityTypes.forEach(t -> t.onEntityRemoved(event.getEntityIds()));
    }

    @SubscribeEvent
    public void onClassChange(CharacterUpdateEvent e) {
        clearAll();
    }

    @SubscribeEvent
    public void onWorldStateChanged(WorldStateEvent e) {
        clearAll();
    }

    /**
     * @return the currently active ability types, in registration order.
     */
    public List<CastedAbilityType> getActiveAbilities() {
        return abilityTypes.stream().filter(CastedAbilityType::isActive).toList();
    }

    /**
     * @return the active ability of the given type, if any.
     */
    public <T extends CastedAbilityType> Optional<T> getActiveAbility(Class<T> type) {
        return abilityTypes.stream()
                .filter(type::isInstance)
                .filter(CastedAbilityType::isActive)
                .map(type::cast)
                .findFirst();
    }

    private boolean isWithinCastWindow() {
        return System.currentTimeMillis() - lastCastTime < CAST_MAX_DELAY_MS || Models.Inventory.hasAutoCasterItem();
    }

    /**
     * @return true if any currently-active type conflicts with the candidate,
     * meaning the candidate can never activate right now.
     */
    private boolean isBlocked(CastedAbilityType candidate) {
        for (CastedAbilityType other : abilityTypes) {
            if (other == candidate) continue;
            if (!other.isActive()) continue;

            if (other.getConflictingTypes().contains(candidate.getClass())) return true;
            if (candidate.getConflictingTypes().contains(other.getClass())) return true;
        }
        return false;
    }

    private void clearAll() {
        abilityTypes.forEach(CastedAbilityType::onCleared);
        mainVehicleIds.clear();
        recentItemDisplayIds.clear();
    }

    /**
     * Extracts the custom model data float from all item display passengers, if present.
     */
    private static List<Float> extractModelIdsFromPassengers(Entity entity) {
        List<Float> modelIds = new ArrayList<>();

        for (Entity passenger : entity.getPassengers()) {
            if (!(passenger instanceof Display.ItemDisplay passengerDisplay)) continue;

            ItemStack itemStack = passengerDisplay.getEntityData().get(Display.ItemDisplay.DATA_ITEM_STACK_ID);
            if (!itemStack.is(Items.OAK_BOAT)) continue;

            CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (customModelData == null || customModelData.floats().isEmpty()) continue;

            modelIds.addAll(customModelData.floats());
        }

        return modelIds;
    }

    private void registerAbilityTypes() {
        abilityTypes.add(new MantleAbility());
        abilityTypes.add(new GuardianAngelsAbility());
        abilityTypes.add(new ArrowShieldAbility());
    }
}
