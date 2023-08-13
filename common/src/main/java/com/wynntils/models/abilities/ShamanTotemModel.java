/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.mc.event.AddEntityEvent;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.models.abilities.event.TotemEvent;
import com.wynntils.models.abilities.type.ShamanTotem;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.models.worlds.WorldStateModel;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShamanTotemModel extends Model {
    private static final int MAX_TOTEM_COUNT = 3;

    private final ShamanTotem[] totems = new ShamanTotem[MAX_TOTEM_COUNT];
    private final Integer[] pendingTotemVisibleIds = new Integer[MAX_TOTEM_COUNT];

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
                            totemNumber, -1, totemAS.getId(), -1, ShamanTotem.TotemState.SUMMONED, totemAS.position());

                    totems[totemNumber - 1] = newTotem;
                    pendingTotemVisibleIds[totemNumber - 1] = totemAS.getId();
                },
                3);
    }

    @SubscribeEvent
    public void onTimerSpawn(AddEntityEvent e) {
        // We aren't looking for a new timer, skip
        if (Arrays.stream(pendingTotemVisibleIds).allMatch(Objects::isNull)) return;

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

        for (ArmorStand armorStand : toCheck) {
            // Recreate position for each ArmorStand checked for most accurate coordinates
            Position position = armorStand.position();

            for (int i = 0; i < pendingTotemVisibleIds.length; i++) {
                if (pendingTotemVisibleIds[i] != null && armorStand.getId() == pendingTotemVisibleIds[i]) {
                    ShamanTotem totem = totems[i];

                    totem.setTimerEntityId(entityId);
                    totem.setPosition(position);
                    totem.setState(ShamanTotem.TotemState.ACTIVE);

                    WynntilsMod.postEvent(new TotemEvent.Activated(totem.getTotemNumber(), position));

                    pendingTotemVisibleIds[i] = null;

                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onTotemRename(EntityLabelChangedEvent e) {
        if (!Models.WorldState.onWorld()) return;

        Entity entity = e.getEntity();
        if (!(entity instanceof ArmorStand)) return;

        StyledText name = e.getName();
        if (name.isEmpty()) return;

        Matcher m = name.getMatcher(SHAMAN_TOTEM_TIMER);
        if (!m.find()) return;

        int parsedTime = Integer.parseInt(m.group(1));
        Position position = entity.position();

        int entityId = entity.getId();
        if (getBoundTotem(entityId) == null) return;

        ShamanTotem boundTotem = getBoundTotem(entityId);

        if (boundTotem == null) return;

        for (ShamanTotem totem : totems) {
            if (boundTotem == totem) {
                totem.setTime(parsedTime);
                totem.setPosition(position);

                WynntilsMod.postEvent(new TotemEvent.Updated(totem.getTotemNumber(), parsedTime, position));

                break;
            }
        }
    }

    @SubscribeEvent
    public void onTotemDestroy(RemoveEntitiesEvent e) {
        if (!Models.WorldState.onWorld()) return;

        List<Integer> destroyedEntities = e.getEntityIds();

        for (ShamanTotem totem : totems) {
            if (totem != null
                    && (destroyedEntities.contains(totem.getTimerEntityId())
                            || destroyedEntities.contains(totem.getVisibleEntityId()))) {
                removeTotem(totem.getTotemNumber());
            }
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
        WynntilsMod.postEvent(new TotemEvent.Removed(totem, totems[totem - 1]));
        totems[totem - 1] = null;
        pendingTotemVisibleIds[totem - 1] = null;
        nextTotemSlot = totem;
    }

    /**
     * Resets all three totem variables.
     */
    private void removeAllTotems() {
        for (int i = 1; i <= MAX_TOTEM_COUNT; i++) {
            removeTotem(i);
        }

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
        for (ShamanTotem totem : totems) {
            if (totem != null && totem.getTimerEntityId() == timerId) {
                return totem;
            }
        }

        return null;
    }

    public List<ShamanTotem> getActiveTotems() {
        return Arrays.stream(totems).filter(Objects::nonNull).toList();
    }

    public ShamanTotem getTotem(int totemNumber) {
        if (totemNumber < 1 || totemNumber > MAX_TOTEM_COUNT) {
            return null;
        }

        return totems[totemNumber - 1];
    }
}
