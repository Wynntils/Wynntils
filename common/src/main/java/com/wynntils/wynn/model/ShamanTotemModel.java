/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.mc.event.AddEntityEvent;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.mc.event.SetEntityDataEvent;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.CharacterUpdateEvent;
import com.wynntils.wynn.event.SpellCastEvent;
import com.wynntils.wynn.event.TotemEvent;
import com.wynntils.wynn.objects.ShamanTotem;
import com.wynntils.wynn.objects.SpellType;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ShamanTotemModel extends Model {

    private ShamanTotem totem1 = null;
    private Integer pendingTotem1Id = null;

    private ShamanTotem totem2 = null;
    private Integer pendingTotem2Id = null;

    private ShamanTotem totem3 = null;
    private Integer pendingTotem3Id = null;

    private long totemCastTimestamp = 0;
    private int nextTotemSlot = 1;

    private static final Pattern SHAMAN_TOTEM_TIMER = Pattern.compile("§c(\\d+)s");

    @SubscribeEvent
    public void onTotemSpellCast(SpellCastEvent e) {
        if (e.getSpell() != SpellType.TOTEM) return;

        totemCastTimestamp = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onTotemSpawn(AddEntityEvent e) {
        Managers.TickScheduler.scheduleLater(
                () -> {
                    if (Math.abs(totemCastTimestamp - System.currentTimeMillis()) > 450) return;
                    Entity entity = getBufferedEntity(e.getId());
                    if (!(entity instanceof ArmorStand totemAS)) return;

                    // Checks to verify this is a totem
                    if (Math.abs(totemAS.getMyRidingOffset() - 0.10000000149011612f) > 0.00000000000012f) return;
                    if (Math.abs(totemAS.getHealth() - 1.0f) > 0.0001f) return;
                    if (Math.abs(totemAS.getEyeHeight() - 1.7775f) > 0.00001f) return;
                    List<ItemStack> inv = new ArrayList<>();
                    totemAS.getArmorSlots().forEach(inv::add);
                    if (inv.size() < 4 || inv.get(3).getItem() != Items.STONE_SHOVEL) return;

                    int totemNumber = getNextTotemSlot();

                    // Chores complete, this is a valid totem
                    WynntilsMod.postEvent(new TotemEvent.Summoned(totemNumber, (ArmorStand) entity));

                    ShamanTotem newTotem = new ShamanTotem(
                            totemNumber,
                            -1,
                            -1,
                            ShamanTotem.TotemState.SUMMONED,
                            new Location(totemAS.position().x, totemAS.position().y, totemAS.position().z));

                    switch (totemNumber) {
                        case 1 -> {
                            totem1 = newTotem;
                            pendingTotem1Id = totemAS.getId();
                        }
                        case 2 -> {
                            totem2 = newTotem;
                            pendingTotem2Id = totemAS.getId();
                        }
                        case 3 -> {
                            totem3 = newTotem;
                            pendingTotem3Id = totemAS.getId();
                        }
                        default -> throw new IllegalArgumentException(
                                "totemNumber should be 1, 2, or 3! (totem variable switch in #onTotemSpawn in ShamanTotemTrackingFeature");
                    }
                },
                3);
    }

    @SubscribeEvent
    public void onTotemRename(SetEntityDataEvent e) {
        if (!WynnUtils.onWorld()) return;

        int entityId = e.getId();
        Entity entity = getBufferedEntity(entityId);
        if (!(entity instanceof ArmorStand)) return;

        String name = getNameFromMetadata(e.getPackedItems());
        if (name == null || name.isEmpty()) return;

        /*
        Logic flow for the following bits:
        - First, the given entity is checked to see if it is a totem timer
        - If the given timerId (int entityId) is not already a totem, assign it to the lowest # totem slot
          - Additionally, assign location, state, and time
        - If the given timerId is already a totem, update the time and location instead
          - Location is updated because there are now totems that can move
         */
        Matcher m = SHAMAN_TOTEM_TIMER.matcher(name);
        if (!m.find()) return;

        int parsedTime = Integer.parseInt(m.group(1));
        Location parsedLocation = new Location(entity.position().x, entity.position().y, entity.position().z);

        if (getBoundTotem(entityId) == null && Math.abs(totemCastTimestamp - System.currentTimeMillis()) < 15000) {
            // Given timerId is not a totem, make a new totem (assuming regex matches and we are within 15s of casting)
            // First check if this is actually one casted by us
            List<ArmorStand> toCheck = McUtils.mc()
                    .level
                    .getEntitiesOfClass(
                            ArmorStand.class,
                            new AABB(
                                    entity.position().x - 0.5,
                                    entity.position().y - 0.1,
                                    entity.position().z - 0.5,
                                    entity.position().x + 0.5,
                                    entity.position().y + 0.1,
                                    entity.position().z + 0.5));

            for (ArmorStand as : toCheck) {
                if (pendingTotem1Id != null && as.getId() == pendingTotem1Id) {
                    totem1 = new ShamanTotem(1, entityId, parsedTime, ShamanTotem.TotemState.ACTIVE, parsedLocation);
                    WynntilsMod.postEvent(new TotemEvent.Activated(1, parsedTime, parsedLocation));
                    pendingTotem1Id = null;
                } else if (pendingTotem2Id != null && as.getId() == pendingTotem2Id) {
                    totem2 = new ShamanTotem(2, entityId, parsedTime, ShamanTotem.TotemState.ACTIVE, parsedLocation);
                    WynntilsMod.postEvent(new TotemEvent.Activated(2, parsedTime, parsedLocation));
                    pendingTotem2Id = null;
                } else if (pendingTotem3Id != null && as.getId() == pendingTotem3Id) {
                    totem3 = new ShamanTotem(3, entityId, parsedTime, ShamanTotem.TotemState.ACTIVE, parsedLocation);
                    WynntilsMod.postEvent(new TotemEvent.Activated(3, parsedTime, parsedLocation));
                    pendingTotem3Id = null;
                } else {
                    // No totem slots available?
                    WynntilsMod.getLogger().warn("Received a new totem {}, but no totem slots are available", entityId);
                }
            }
        } else if (totem1 != null && getBoundTotem(entityId) == totem1) {
            totem1.setTime(parsedTime);
            totem1.setLocation(parsedLocation);
            WynntilsMod.postEvent(new TotemEvent.Activated(1, parsedTime, parsedLocation));
        } else if (totem2 != null && getBoundTotem(entityId) == totem2) {
            totem2.setTime(parsedTime);
            totem2.setLocation(parsedLocation);
            WynntilsMod.postEvent(new TotemEvent.Activated(2, parsedTime, parsedLocation));
        } else if (totem3 != null && getBoundTotem(entityId) == totem3) {
            totem3.setTime(parsedTime);
            totem3.setLocation(parsedLocation);
            WynntilsMod.postEvent(new TotemEvent.Activated(3, parsedTime, parsedLocation));
        }
    }

    @SubscribeEvent
    public void onTotemDestroy(RemoveEntitiesEvent e) {
        if (!WynnUtils.onWorld()) return;

        List<Integer> destroyedEntities = e.getEntityIds();

        if (totem1 != null && destroyedEntities.contains(totem1.getTimerId())) {
            removeTotem(1);
        }
        if (totem2 != null && destroyedEntities.contains(totem2.getTimerId())) {
            removeTotem(2);
        }
        if (totem3 != null && destroyedEntities.contains(totem3.getTimerId())) {
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

    private String getNameFromMetadata(List<SynchedEntityData.DataValue<?>> data) {
        // The rename stuff we're looking for is eventually something like
        // Optional[literal{§c26s}[style={}]]
        for (SynchedEntityData.DataValue<?> packedItem : data) {
            if (!(packedItem.value() instanceof Optional<?> packetData)
                    || packetData.isEmpty()
                    || (!(packetData.get() instanceof MutableComponent content))) continue;
            return content.toString();
        }
        return null;
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
                pendingTotem1Id = null;
                nextTotemSlot = 1;
            }
            case 2 -> {
                WynntilsMod.postEvent(new TotemEvent.Removed(2, totem2));
                totem2 = null;
                pendingTotem2Id = null;
                if (nextTotemSlot != 1) {
                    nextTotemSlot = 2; // Only set to 2 if it's not already lower
                }
            }
            case 3 -> {
                WynntilsMod.postEvent(new TotemEvent.Removed(3, totem3));
                totem3 = null;
                pendingTotem3Id = null;
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
        if (totem1 != null && totem1.getTimerId() == timerId) return totem1;
        if (totem2 != null && totem2.getTimerId() == timerId) return totem2;
        if (totem3 != null && totem3.getTimerId() == timerId) return totem3;
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
