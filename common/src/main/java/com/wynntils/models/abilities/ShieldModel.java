/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.AddEntityEvent;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.models.abilities.type.ArrowShield;
import com.wynntils.models.abilities.type.GuardianAngelsShield;
import com.wynntils.models.abilities.type.MantleShield;
import com.wynntils.models.abilities.type.ShieldType;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

public final class ShieldModel extends Model {
    private static final double SEARCH_RADIUS = 4.5;

    private final List<ShieldType> shieldTypes = new ArrayList<>();

    private List<Integer> collectedIds;
    private List<Integer> spawnedIds;
    private long shieldCastTime = 0;

    private ShieldType activeShieldType;

    public ShieldModel() {
        super(List.of());

        registerShieldTypes();
    }

    @SubscribeEvent
    public void onShieldCast(SpellEvent.Completed e) {
        for (ShieldType shieldType : shieldTypes) {
            if (shieldType.validSpell(e.getSpell())) {
                shieldCastTime = System.currentTimeMillis();
                collectedIds = new ArrayList<>();
                spawnedIds = null;
                break;
            }
        }
    }

    @SubscribeEvent
    public void onShieldSpawn(AddEntityEvent event) {
        for (ShieldType shieldType : shieldTypes) {
            if (shieldType.validClass()) {
                // It is possible for us to receive the cast event just after the shield has actually spawned
                // So we must collect all possible spawns
                Entity entity = McUtils.mc().level.getEntity(event.getId());
                if (entity == null) return;
                if (!(entity instanceof ArmorStand shieldAS)) return;

                Vec3 playerPos = McUtils.player().position();
                Managers.TickScheduler.scheduleLater(
                        () -> {
                            if (!isValidSpawn()) return;

                            // This must be ran with a delay, as inventory contents are set a couple ticks after the
                            // entity spawns.
                            if (!shieldType.verifyShield(shieldAS)) return;

                            // If the player is standing still, the armor stands spawn about 2.1 blocks away
                            // from the player. But if the player moves, it can be up to ~ 4 blocks depending
                            // on walk speed.
                            if (shieldAS.position().distanceTo(playerPos) > SEARCH_RADIUS) return;

                            // Save field in local variable to avoid surprises where it is overwritten by null
                            List<Integer> collector = collectedIds;
                            // If we're not collecting shields, do nothing.
                            if (collector == null) return;

                            collector.add(shieldAS.getId());

                            activeShieldType = shieldType;

                            // 5 tick total delay to ensure all armor stands have spawned and have their inventory set
                            Managers.TickScheduler.scheduleLater(this::registerShield, 2);
                        },
                        3);
            }
        }
    }

    @SubscribeEvent
    public void onShieldDestroy(RemoveEntitiesEvent event) {
        if (spawnedIds == null) return;

        for (Integer destroyedEntityId : event.getEntityIds()) {
            spawnedIds.stream()
                    .filter(id -> Objects.equals(id, destroyedEntityId))
                    .findFirst()
                    .ifPresent(id -> spawnedIds.remove(id));
        }

        if (spawnedIds.isEmpty()) {
            removeShield();
        }
    }

    @SubscribeEvent
    public void onClassChange(CharacterUpdateEvent e) {
        removeShield();
    }

    @SubscribeEvent
    public void onHeldItemChange(ChangeCarriedItemEvent e) {
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
        if (collectedIds != null) {
            spawnedIds = collectedIds;
            collectedIds = null;
        }
    }

    private void removeShield() {
        spawnedIds = null;
        activeShieldType = null;
    }

    /**
     * @return true if there was either a valid cast recently, or there is a possibility of an auto cast
     */
    private boolean isValidSpawn() {
        return System.currentTimeMillis() - shieldCastTime < 200 || Models.Inventory.hasAutoCasterItem();
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
