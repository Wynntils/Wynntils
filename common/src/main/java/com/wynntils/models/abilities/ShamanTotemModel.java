/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.models.abilities.event.TotemEvent;
import com.wynntils.models.abilities.label.ShamanTotemLabelInfo;
import com.wynntils.models.abilities.label.ShamanTotemLabelParser;
import com.wynntils.models.abilities.type.ShamanTotem;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.TimedSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;

public final class ShamanTotemModel extends Model {
    private static final int MAX_TOTEM_COUNT = 4;
    private static final long PENDING_SUMMON_TIMEOUT_MS = 3000L;
    private static final float SHAMAN_TOTEM_CUSTOM_MODEL_DATA = 30628.0f;

    private final ShamanTotem[] totems = new ShamanTotem[MAX_TOTEM_COUNT];
    private final TimedSet<Integer> pendingSummonedTotems =
            new TimedSet<>(PENDING_SUMMON_TIMEOUT_MS, TimeUnit.MILLISECONDS, true);
    private int nextTotemSlot = 1;

    public ShamanTotemModel() {
        super(List.of());

        Handlers.Label.registerParser(new ShamanTotemLabelParser());
    }

    @SubscribeEvent
    public void onTotemSpellCast(SpellEvent.Cast event) {
        if (event.getSpellType() != SpellType.TOTEM) return;
        if (!Models.WorldState.onWorld()) return;
        if (Models.Character.getClassType() != ClassType.SHAMAN) return;

        pruneExpiredPendingSummonedTotems();
        registerPendingTotem();
    }

    @SubscribeEvent
    public void onTotemLabelIdentified(LabelIdentifiedEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (Models.Character.getClassType() != ClassType.SHAMAN) return;
        if (!(event.getLabelInfo() instanceof ShamanTotemLabelInfo totemLabelInfo)) return;
        if (!totemLabelInfo.getPlayerName().equals(McUtils.playerName())) return;
        if (!(totemLabelInfo.getEntity() instanceof Display.TextDisplay timerDisplay)) return;

        pruneExpiredPendingSummonedTotems();

        int timerEntityId = timerDisplay.getId();
        ShamanTotem boundTotem = getTotemByTimerEntityId(timerEntityId);
        if (boundTotem != null) {
            updateTotem(boundTotem, totemLabelInfo.getTimeLeft(), timerDisplay.position());
            return;
        }

        ShamanTotem pendingTotem = getNextPendingTotem();
        if (pendingTotem != null) {
            bindPendingTotem(pendingTotem, timerDisplay, totemLabelInfo.getTimeLeft());
            return;
        }

        registerTotem(timerDisplay, totemLabelInfo.getTimeLeft());
    }

    @SubscribeEvent
    public void onTotemDestroy(RemoveEntitiesEvent event) {
        if (!Models.WorldState.onWorld()) return;

        for (int entityId : event.getEntityIds()) {
            ShamanTotem totem = getTotemByTimerEntityId(entityId);
            if (totem == null) continue;

            removeTotem(totem.getTotemNumber());
        }
    }

    @SubscribeEvent
    public void onClassChange(CharacterUpdateEvent event) {
        removeAllTotems();
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent event) {
        removeAllTotems();
    }

    private void registerPendingTotem() {
        int totemNumber = getNextTotemSlot();
        if (totems[totemNumber - 1] != null) {
            removeTotem(totemNumber);
        }

        ShamanTotem newTotem = new ShamanTotem(
                totemNumber,
                -1,
                -1,
                ShamanTotem.TotemState.SUMMONED,
                McUtils.player().position());

        totems[totemNumber - 1] = newTotem;
        pendingSummonedTotems.remove(totemNumber);
        pendingSummonedTotems.put(totemNumber);
    }

    private void registerTotem(Display.TextDisplay timerDisplay, int parsedTime) {
        int totemNumber = getNextTotemSlot();
        if (totems[totemNumber - 1] != null) {
            removeTotem(totemNumber);
        }

        ShamanTotem newTotem = new ShamanTotem(
                totemNumber, timerDisplay.getId(), parsedTime, ShamanTotem.TotemState.ACTIVE, timerDisplay.position());

        totems[totemNumber - 1] = newTotem;
        WynntilsMod.postEvent(new TotemEvent.Summoned(totemNumber, timerDisplay));
    }

    private void bindPendingTotem(ShamanTotem totem, Display.TextDisplay timerDisplay, int parsedTime) {
        int timerEntityId = timerDisplay.getId();
        Position position = timerDisplay.position();

        totem.setTimerEntityId(timerEntityId);
        totem.setTime(parsedTime);
        totem.setPosition(position);
        totem.setState(ShamanTotem.TotemState.ACTIVE);

        pendingSummonedTotems.remove(totem.getTotemNumber());

        Display.ItemDisplay totemDisplay = findTotemDisplay(position);
        totem.setItemDisplay(totemDisplay);

        WynntilsMod.postEvent(new TotemEvent.Activated(totem.getTotemNumber(), totemDisplay, position));
    }

    private void updateTotem(ShamanTotem totem, int parsedTime, Position position) {
        totem.setTime(parsedTime);
        totem.setPosition(position);
        totem.setState(ShamanTotem.TotemState.ACTIVE);

        if (totem.getItemDisplay() == null) {
            Display.ItemDisplay resolvedDisplay = findTotemDisplay(position);
            if (resolvedDisplay != null) {
                totem.setItemDisplay(resolvedDisplay);
                WynntilsMod.postEvent(new TotemEvent.Activated(totem.getTotemNumber(), resolvedDisplay, position));
            }
        }

        WynntilsMod.postEvent(new TotemEvent.Updated(totem.getTotemNumber(), parsedTime, position));
    }

    private Display.ItemDisplay findTotemDisplay(Position totemPosition) {
        if (McUtils.mc().level == null) return null;

        double x = totemPosition.x();
        double y = totemPosition.y();
        double z = totemPosition.z();

        AABB search = new AABB(x - 1.5, y - 3.0, z - 1.5, x + 1.5, y + 0.5, z + 1.5);

        return McUtils.mc().level.getEntitiesOfClass(Display.ItemDisplay.class, search).stream()
                .filter(this::isShamanTotemDisplay)
                .filter(item -> {
                    double yDelta = y - item.getY();
                    return yDelta >= 0.75 && yDelta <= 2.75;
                })
                .min(Comparator.comparingDouble(item -> {
                    double dx = item.getX() - x;
                    double dz = item.getZ() - z;
                    double dy = Math.abs((y - item.getY()) - 1.5);
                    return dx * dx + dz * dz + dy * dy;
                }))
                .orElse(null);
    }

    private boolean isShamanTotemDisplay(Display.ItemDisplay itemDisplay) {
        ItemStack stack = itemDisplay.getEntityData().get(Display.ItemDisplay.DATA_ITEM_STACK_ID);
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.is(Items.OAK_BOAT)) return false;

        CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return cmd != null && Float.valueOf(SHAMAN_TOTEM_CUSTOM_MODEL_DATA).equals(cmd.getFloat(0));
    }

    private void removeTotem(int totemNumber) {
        ShamanTotem existingTotem = totems[totemNumber - 1];
        if (existingTotem == null) return;

        WynntilsMod.postEvent(new TotemEvent.Removed(totemNumber, existingTotem));
        pendingSummonedTotems.remove(totemNumber);
        totems[totemNumber - 1] = null;
        nextTotemSlot = totemNumber;
    }

    private void removeAllTotems() {
        for (int i = 1; i <= MAX_TOTEM_COUNT; i++) {
            removeTotem(i);
        }

        pendingSummonedTotems.clear();
        nextTotemSlot = 1;
    }

    private int getNextTotemSlot() {
        for (int i = 0; i < MAX_TOTEM_COUNT; i++) {
            if (totems[i] == null) {
                nextTotemSlot = i + 1;
                return nextTotemSlot;
            }
        }

        int toReturn = nextTotemSlot;
        nextTotemSlot = nextTotemSlot == MAX_TOTEM_COUNT ? 1 : nextTotemSlot + 1;
        return toReturn;
    }

    private ShamanTotem getTotemByTimerEntityId(int timerEntityId) {
        for (ShamanTotem totem : totems) {
            if (totem != null && totem.getTimerEntityId() == timerEntityId) {
                return totem;
            }
        }

        return null;
    }

    private ShamanTotem getNextPendingTotem() {
        return pendingSummonedTotems.getEntries().stream()
                .sorted(Comparator.comparingLong(TimedSet.TimedEntry::getCreation))
                .map(TimedSet.TimedEntry::getEntry)
                .map(this::getTotemInternal)
                .filter(Objects::nonNull)
                .filter(totem -> totem.getState() == ShamanTotem.TotemState.SUMMONED)
                .filter(totem -> totem.getTimerEntityId() == -1)
                .findFirst()
                .orElse(null);
    }

    public List<ShamanTotem> getActiveTotems() {
        pruneExpiredPendingSummonedTotems();
        return Arrays.stream(totems).filter(Objects::nonNull).toList();
    }

    public ShamanTotem getTotem(int totemNumber) {
        pruneExpiredPendingSummonedTotems();
        return getTotemInternal(totemNumber);
    }

    private ShamanTotem getTotemInternal(int totemNumber) {
        if (totemNumber < 1 || totemNumber > MAX_TOTEM_COUNT) {
            return null;
        }

        return totems[totemNumber - 1];
    }

    private void pruneExpiredPendingSummonedTotems() {
        Set<Integer> pendingTotemNumbers = pendingSummonedTotems.stream().collect(Collectors.toSet());

        for (int i = 0; i < MAX_TOTEM_COUNT; i++) {
            ShamanTotem totem = totems[i];
            if (totem == null) continue;
            if (totem.getState() != ShamanTotem.TotemState.SUMMONED) continue;
            if (totem.getTimerEntityId() != -1) continue;
            if (pendingTotemNumbers.contains(totem.getTotemNumber())) continue;

            totems[i] = null;
            nextTotemSlot = totem.getTotemNumber();
        }
    }
}
