/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Services;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.mc.event.PlayerTeleportEvent;
import com.wynntils.models.activities.type.Dungeon;
import com.wynntils.models.character.event.CharacterDeathEvent;
import com.wynntils.models.character.event.CharacterMovedEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.core.Position;
import net.neoforged.bus.api.SubscribeEvent;

/** Tracks dungeon runs independently of the content-book activity tracker. */
public final class DungeonModel extends Model {
    private static final double ENTRANCE_RADIUS_SQUARED = 32 * 32;
    private static final double MIN_ENTRY_TELEPORT_DISTANCE_SQUARED = 32 * 32;
    // Observed when entering a corrupted dungeon, including when another player supplied the key.
    private static final Pattern CORRUPTED_ENTRY_PATTERN = Pattern.compile(
            "^You must kill \\d+ mobs and complete the dungeon for this run to count towards the Forgery Chest\\.$");
    private static final Pattern COMPLETION_PATTERN = Pattern.compile("^Great job! You've completed the .+ Dungeon!$");

    private boolean onWorld;
    private boolean inDungeon;
    private boolean corrupted;

    public DungeonModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onTeleport(PlayerTeleportEvent event) {
        if (!onWorld || McUtils.player() == null) return;
        handleTeleport(
                McUtils.player().position(),
                event.getNewPosition(),
                Services.Map.hasMapData(),
                Services.Map.isInMappedArea(event.getNewPosition()));
    }

    @SubscribeEvent
    public void onMove(CharacterMovedEvent event) {
        if (inDungeon && Services.Map.isInMappedArea(event.getPosition())) {
            reset();
        }
    }

    @SubscribeEvent
    public void onChat(ChatMessageEvent.Match event) {
        if (!onWorld) return;
        if (event.getRecipientType() != RecipientType.INFO && event.getRecipientType() != RecipientType.GAME_MESSAGE) {
            return;
        }
        handleMessage(
                StyledTextUtils.unwrap(event.getMessage()).stripAlignment().getStringWithoutFormatting());
    }

    @SubscribeEvent
    public void onDeath(CharacterDeathEvent event) {
        // Normal dungeons may respawn the player at an internal second-chance checkpoint.
        // Keep the run until the player actually returns to the mapped playfield.
        if (corrupted) {
            reset();
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        onWorld = event.getNewState() == WorldState.WORLD;
        reset();
    }

    public boolean isInDungeon() {
        return inDungeon;
    }

    void handleTeleport(Position from, Position to, boolean mapDataAvailable, boolean destinationMapped) {
        if (!mapDataAvailable) return;
        if (destinationMapped) {
            reset();
            return;
        }
        if (inDungeon) return;

        double dx = to.x() - from.x();
        double dz = to.z() - from.z();
        if (dx * dx + dz * dz < MIN_ENTRY_TELEPORT_DISTANCE_SQUARED) return;

        // A portal must take us away from a known entrance into an unmapped area.
        // Merely standing near an entrance or tracking a dungeon never starts a run.
        for (Dungeon dungeon : Dungeon.values()) {
            if (dungeon.getDungeonData()
                    .filter(data -> isNearEntrance(from, data))
                    .isPresent()) {
                inDungeon = true;
                corrupted = false;
                return;
            }
            if (dungeon.getCorruptedDungeonData()
                    .filter(data -> isNearEntrance(from, data))
                    .isPresent()) {
                inDungeon = true;
                corrupted = true;
                return;
            }
        }
    }

    void handleMessage(String message) {
        if (COMPLETION_PATTERN.matcher(message).matches()) {
            reset();
        } else if (CORRUPTED_ENTRY_PATTERN.matcher(message).matches()) {
            inDungeon = true;
            corrupted = true;
        }
    }

    private static boolean isNearEntrance(Position position, Dungeon.DungeonData data) {
        double dx = position.x() - data.getXPos();
        double dz = position.z() - data.getZPos();
        return dx * dx + dz * dz <= ENTRANCE_RADIUS_SQUARED;
    }

    private void reset() {
        inDungeon = false;
        corrupted = false;
    }
}
