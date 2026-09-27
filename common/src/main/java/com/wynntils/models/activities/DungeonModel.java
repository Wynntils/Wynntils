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
import com.wynntils.utils.type.BoundingCircle;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.core.Position;
import net.neoforged.bus.api.SubscribeEvent;

/*
 * Dungeon Entry Detection Rule:
 * 1. With map data available, teleport at least 32 blocks horizontally from within 32 blocks
 *    of a known normal entrance or corrupted key collector into an unmapped area outside collector areas.
 * 2. The server's Forgery kill-requirement or completion-reminder message also confirms a corrupted run.
 *
 * Dungeon End Detection Rule:
 * 1. End on the server's dungeon-completion message, return to mapped terrain, or teleport to a collector area.
 * 2. End on death in a corrupted dungeon; keep normal runs active for second chance respawns.
 * 3. Reset on any world-state change.
 */
public final class DungeonModel extends Model {
    private static final double ENTRANCE_RADIUS_SQUARED = 32 * 32;
    private static final double MIN_ENTRY_TELEPORT_DISTANCE_SQUARED = 32 * 32;
    private static final Pattern CORRUPTED_ENTRY_PATTERN = Pattern.compile(
            "^(?:You must kill \\d+ mobs and complete|Complete) the dungeon for this run to count towards the Forgery Chest\\.$");
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
                !Services.Map.getMapsForBoundingCircle(new BoundingCircle(0, 0, Float.POSITIVE_INFINITY))
                        .isEmpty(),
                isInMappedArea(event.getNewPosition()));
    }

    @SubscribeEvent
    public void onMove(CharacterMovedEvent event) {
        if (inDungeon && isInMappedArea(event.getPosition())) {
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
        if (destinationMapped || isNearCorruptedKeyCollector(to)) {
            reset();
            return;
        }
        if (inDungeon) return;

        double dx = to.x() - from.x();
        double dz = to.z() - from.z();
        if (dx * dx + dz * dz < MIN_ENTRY_TELEPORT_DISTANCE_SQUARED) return;

        if (isNearCorruptedKeyCollector(from)) {
            inDungeon = true;
            corrupted = true;
            return;
        }

        for (Dungeon dungeon : Dungeon.values()) {
            if (dungeon.getDungeonData()
                    .filter(data -> isNearEntrance(from, data))
                    .isPresent()) {
                inDungeon = true;
                corrupted = false;
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

    private static boolean isInMappedArea(Position position) {
        float x = (float) position.x();
        float z = (float) position.z();
        return Services.Map.getMapsForBoundingCircle(new BoundingCircle(x, z, 1)).stream()
                .anyMatch(map -> map.getBox().contains(x, z));
    }

    private static boolean isNearEntrance(Position position, Dungeon.DungeonData data) {
        double dx = position.x() - data.getXPos();
        double dz = position.z() - data.getZPos();
        return dx * dx + dz * dz <= ENTRANCE_RADIUS_SQUARED;
    }

    private static boolean isNearCorruptedKeyCollector(Position position) {
        return Arrays.stream(Dungeon.values())
                .flatMap(dungeon -> dungeon.getCorruptedDungeonData().stream())
                .anyMatch(data -> isNearEntrance(position, data));
    }

    private void reset() {
        inDungeon = false;
        corrupted = false;
    }
}
