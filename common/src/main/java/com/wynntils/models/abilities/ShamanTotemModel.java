/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.mc.event.AddEntityEvent;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.models.abilities.event.TotemEvent;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.models.worlds.WorldStateModel;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShamanTotemModel extends Model {

    private ShamanTotem totem1 = null;
    private Integer pendingTotem1VisibleId = null;

    private ShamanTotem totem2 = null;
    private Integer pendingTotem2VisibleId = null;

    private ShamanTotem totem3 = null;
    private Integer pendingTotem3VisibleId = null;

    private long totemCastTimestamp = 0;
    private int nextTotemSlot = 1;

    private static final Pattern SHAMAN_TOTEM_TIMER = Pattern.compile("§c(\\d+)s");
    private static final double TOTEM_SEARCH_RADIUS = 1.0;
    private static final int CAST_DELAY_MAX_MS = 450;

    public ShamanTotemModel(WorldStateModel worldStateModel) {
        super(List.of(worldStateModel));
    }

    @SubscribeEvent
    public void onTotemSpellCast(SpellEvent.Completed e) {
        if (e.getSpell() != SpellType.TOTEM) return;

        totemCastTimestamp = System.currentTimeMillis() - 40; // 40 == 2 ticks
        // The -2 ticks is required so that the #onTotemSpawn event does not occasionally fail the cast timestamp check
    }

    @SubscribeEvent
    public void onTotemSpawn(AddEntityEvent e) {
        Entity entity = getBufferedEntity(e.getId());
        if (!(entity instanceof ArmorStand totemAS)) return;

        if (Math.abs(totemCastTimestamp - System.currentTimeMillis()) > CAST_DELAY_MAX_MS) return;

        Managers.TickScheduler.scheduleLater(
                () -> {
                    // Checks to verify this is a totem
                    // These must be ran with a delay, as inventory contents are set a couple ticks after the totem
                    // actually spawns
                    List<ItemStack> inv = new ArrayList<>();
                    totemAS.getArmorSlots().forEach(inv::add);
                    if (inv.size() < 4) return;
                    ItemStack data = inv.get(3);
                    if (data.getItem() != Items.STONE_SHOVEL) return;
                    // This relies on the fact that damage values 28 (Shaman) and 29 (Skyseer) on the stone shovel set
                    // the totem's texture
                    if (data.getDamageValue() != 28 && data.getDamageValue() != 29) return;
                    // Checks complete, this is a valid totem

                    int totemNumber = getNextTotemSlot();

                    WynntilsMod.postEvent(new TotemEvent.Summoned(totemNumber, totemAS));

                    ShamanTotem newTotem = new ShamanTotem(
                            totemNumber,
                            -1,
                            totemAS.getId(),
                            -1,
                            ShamanTotem.TotemState.SUMMONED,
                            new Location(totemAS.position().x, totemAS.position().y, totemAS.position().z));

                    switch (totemNumber) {
                        case 1 -> {
                            totem1 = newTotem;
                            pendingTotem1VisibleId = totemAS.getId();
                        }
                        case 2 -> {
                            totem2 = newTotem;
                            pendingTotem2VisibleId = totemAS.getId();
                        }
                        case 3 -> {
                            totem3 = newTotem;
                            pendingTotem3VisibleId = totemAS.getId();
                        }
                        default -> throw new IllegalArgumentException(
                                "totemNumber should be 1, 2, or 3! (totem variable switch in #onTotemSpawn in ShamanTotemTrackingFeature");
                    }
                },
                3);
    }

    @SubscribeEvent
    public void onTimerSpawn(AddEntityEvent e) {
        // We aren't looking for a new timer, skip
        if (pendingTotem1VisibleId == null && pendingTotem2VisibleId == null && pendingTotem3VisibleId == null) return;

        int entityId = e.getId();

        // This timer is already bound to a totem but got respawned? skip
        if (getBoundTotem(entityId) != null) return;

        Entity possibleTimer = getBufferedEntity(entityId);
        if (!(possibleTimer instanceof ArmorStand)) return;

        // Given timerId is not a totem, make a new totem
        List<ArmorStand> toCheck = McUtils.mc()
                .level
                .getEntitiesOfClass(
                        ArmorStand.class,
                        new AABB(
                                possibleTimer.position().x - TOTEM_SEARCH_RADIUS,
                                possibleTimer.position().y
                                        - 0.3, // Don't modify this unless you are certain it is causing issues
                                possibleTimer.position().z - TOTEM_SEARCH_RADIUS,
                                possibleTimer.position().x + TOTEM_SEARCH_RADIUS,
                                // (a LOT) more vertical radius required for totems casted off high places
                                // This increased radius requirement increases as you cast from higher places and as the
                                // server gets laggier
                                possibleTimer.position().y + TOTEM_SEARCH_RADIUS * 5,
                                possibleTimer.position().z + TOTEM_SEARCH_RADIUS));

        for (ArmorStand as : toCheck) {
            // Recreate location for each ArmorStand checked for most accurate coordinates
            Location parsedLocation = new Location(as.position().x, as.position().y, as.position().z);
            if (pendingTotem1VisibleId != null && as.getId() == pendingTotem1VisibleId) {
                totem1.setTimerEntityId(entityId);
                totem1.setLocation(parsedLocation);
                totem1.setState(ShamanTotem.TotemState.ACTIVE);
                WynntilsMod.postEvent(new TotemEvent.Activated(1, parsedLocation));
                pendingTotem1VisibleId = null;
            } else if (pendingTotem2VisibleId != null && as.getId() == pendingTotem2VisibleId) {
                totem2.setTimerEntityId(entityId);
                totem2.setLocation(parsedLocation);
                totem2.setState(ShamanTotem.TotemState.ACTIVE);
                WynntilsMod.postEvent(new TotemEvent.Activated(2, parsedLocation));
                pendingTotem2VisibleId = null;
            } else if (pendingTotem3VisibleId != null && as.getId() == pendingTotem3VisibleId) {
                totem3.setTimerEntityId(entityId);
                totem3.setLocation(parsedLocation);
                totem3.setState(ShamanTotem.TotemState.ACTIVE);
                WynntilsMod.postEvent(new TotemEvent.Activated(3, parsedLocation));
                pendingTotem3VisibleId = null;
            } else {
                // No totem slots available?
                // WynntilsMod.getLogger().warn("Received a new totem {}, but no totem slots are available", entityId);
            }
        }
    }

    @SubscribeEvent
    public void onTotemRename(EntityLabelChangedEvent e) {
        if (!Models.WorldState.onWorld()) return;

        Entity entity = e.getEntity();
        if (!(entity instanceof ArmorStand)) return;

        String name = e.getName();
        if (name.isEmpty()) return;

        Matcher m = SHAMAN_TOTEM_TIMER.matcher(name);
        if (!m.find()) return;

        int parsedTime = Integer.parseInt(m.group(1));
        Location parsedLocation = new Location(entity.position().x, entity.position().y, entity.position().z);

        int entityId = entity.getId();
        if (getBoundTotem(entityId) == null) return;

        if (totem1 != null && getBoundTotem(entityId) == totem1) {
            totem1.setTime(parsedTime);
            totem1.setLocation(parsedLocation);
            WynntilsMod.postEvent(new TotemEvent.Updated(1, parsedTime, parsedLocation));
        } else if (totem2 != null && getBoundTotem(entityId) == totem2) {
            totem2.setTime(parsedTime);
            totem2.setLocation(parsedLocation);
            WynntilsMod.postEvent(new TotemEvent.Updated(2, parsedTime, parsedLocation));
        } else if (totem3 != null && getBoundTotem(entityId) == totem3) {
            totem3.setTime(parsedTime);
            totem3.setLocation(parsedLocation);
            WynntilsMod.postEvent(new TotemEvent.Updated(3, parsedTime, parsedLocation));
        }
    }

    @SubscribeEvent
    public void onTotemDestroy(RemoveEntitiesEvent e) {
        if (!Models.WorldState.onWorld()) return;

        List<Integer> destroyedEntities = e.getEntityIds();

        if (totem1 != null
                && (destroyedEntities.contains(totem1.getTimerEntityId())
                        || destroyedEntities.contains(totem1.getVisibleEntityId()))) {
            removeTotem(1);
        }
        if (totem2 != null
                && (destroyedEntities.contains(totem2.getTimerEntityId())
                        || destroyedEntities.contains(totem2.getVisibleEntityId()))) {
            removeTotem(2);
        }
        if (totem3 != null
                && (destroyedEntities.contains(totem3.getTimerEntityId())
                        || destroyedEntities.contains(totem3.getVisibleEntityId()))) {
            removeTotem(3);
        }
    }

    @SubscribeEvent
    public void onClassChange(CharacterUpdateEvent e) {
        removeAllTotems();
    }

    @SubscribeEvent
    public void onHeldItemChange(ChangeCarriedItemEvent e) {
        removeAllTotems();
    }

    private Entity getBufferedEntity(int entityId) {
        Entity entity = McUtils.mc().level.getEntity(entityId);
        if (entity != null) return entity;

        if (entityId == -1) {
            return new ArmorStand(McUtils.mc().level, 0, 0, 0);
        }

        return null;
    }

    /**
     * Removes the given totem from the list of totems.
     * @param totem The totem to remove. Must be 1, 2, or 3
     */
    private void removeTotem(int totem) {
        switch (totem) {
            case 1 -> {
                WynntilsMod.postEvent(new TotemEvent.Removed(1, totem1));
                totem1 = null;
                pendingTotem1VisibleId = null;
                nextTotemSlot = 1;
            }
            case 2 -> {
                WynntilsMod.postEvent(new TotemEvent.Removed(2, totem2));
                totem2 = null;
                pendingTotem2VisibleId = null;
                if (nextTotemSlot != 1) {
                    nextTotemSlot = 2; // Only set to 2 if it's not already lower
                }
            }
            case 3 -> {
                WynntilsMod.postEvent(new TotemEvent.Removed(3, totem3));
                totem3 = null;
                pendingTotem3VisibleId = null;
                if (nextTotemSlot != 1 && nextTotemSlot != 2) {
                    nextTotemSlot = 3; // Only set to 3 if it's not already lower
                }
            }
            default -> throw new IllegalArgumentException("Totem must be 1, 2, or 3");
        }
    }

    /**
     * Resets all three totem variables.
     */
    private void removeAllTotems() {
        removeTotem(1);
        removeTotem(2);
        removeTotem(3);
        nextTotemSlot = 1;
    }

    private int getNextTotemSlot() {
        int toReturn = nextTotemSlot;
        if (nextTotemSlot == 3) {
            nextTotemSlot = 1;
        } else {
            nextTotemSlot += 1;
        }
        return toReturn;
    }

    /**
     * Gets the totem bound to the given timerId.
     * @param timerId The timerId that is checked against the totems
     * @return The totem bound to the given timerId, or null if no totem is bound
     */
    private ShamanTotem getBoundTotem(int timerId) {
        if (totem1 != null && totem1.getTimerEntityId() == timerId) return totem1;
        if (totem2 != null && totem2.getTimerEntityId() == timerId) return totem2;
        if (totem3 != null && totem3.getTimerEntityId() == timerId) return totem3;
        return null;
    }

    public List<ShamanTotem> getActiveTotems() {
        List<ShamanTotem> toReturn = new ArrayList<>();
        if (totem1 != null) {
            toReturn.add(totem1);
        }
        if (totem2 != null) {
            toReturn.add(totem2);
        }
        if (totem3 != null) {
            toReturn.add(totem3);
        }
        return toReturn;
    }
}
