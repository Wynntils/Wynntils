/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.models.activities.type.Dungeon;
import com.wynntils.models.character.event.CharacterDeathEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestDungeonModel {
    private static final Vec3 INTERIOR = new Vec3(10000, 50, 10000);
    private static final Vec3 TOWN = new Vec3(-900, 70, -1500);
    private static final String FORGERY_ENTRY =
            "You must kill 25 mobs and complete the dungeon for this run to count towards the Forgery Chest.";

    @Test
    public void entersOnlyWhenLeavingAnEntranceForAnUnmappedArea() {
        DungeonModel model = new DungeonModel();
        Vec3 entrance = entrance(Dungeon.DECREPIT_SEWERS, false);
        model.handleTeleport(entrance, entrance.add(1, 0, 1), true, false);
        Assertions.assertFalse(model.isInDungeon());
        model.handleTeleport(entrance, TOWN, true, true);
        Assertions.assertFalse(model.isInDungeon());
        model.handleTeleport(TOWN, INTERIOR, true, false);
        Assertions.assertFalse(model.isInDungeon());
        model.handleTeleport(entrance, INTERIOR, true, false);
        Assertions.assertTrue(model.isInDungeon());
    }

    @Test
    public void doesNotTreatMissingMapDownloadsAsAnUnmappedDungeon() {
        DungeonModel model = new DungeonModel();
        model.handleTeleport(entrance(Dungeon.DECREPIT_SEWERS, false), INTERIOR, false, false);
        Assertions.assertFalse(model.isInDungeon());
    }

    @Test
    public void preservesNormalDungeonSecondChanceUntilReturningToPlayfield() {
        DungeonModel model = new DungeonModel();
        model.handleTeleport(entrance(Dungeon.INFESTED_PIT, false), INTERIOR, true, false);
        model.onDeath(new CharacterDeathEvent(new Location(10000, 50, 10000)));
        model.handleTeleport(INTERIOR, INTERIOR.add(100, 0, 100), true, false);
        Assertions.assertTrue(model.isInDungeon());
        model.onDeath(new CharacterDeathEvent(new Location(10000, 50, 10000)));
        model.handleTeleport(INTERIOR, TOWN, true, true);
        Assertions.assertFalse(model.isInDungeon());
    }

    @Test
    public void corruptedDungeonDeathEndsTheRun() {
        DungeonModel model = new DungeonModel();
        model.handleTeleport(entrance(Dungeon.UNDERGROWTH_RUINS, true), INTERIOR, true, false);
        Assertions.assertTrue(model.isInDungeon());
        model.onDeath(new CharacterDeathEvent(new Location(10000, 50, 10000)));
        Assertions.assertFalse(model.isInDungeon());
    }

    @Test
    public void recordedForgeryEntryStartsRunWithoutSeeingThePortalTeleport() {
        DungeonModel model = new DungeonModel();
        model.handleMessage(FORGERY_ENTRY);
        Assertions.assertTrue(model.isInDungeon());
        model.onDeath(new CharacterDeathEvent(new Location(10000, 50, 10000)));
        Assertions.assertFalse(model.isInDungeon());
    }

    @Test
    public void recordedCompletionEndsRunWithEncodedSpacesInDungeonName() {
        DungeonModel model = new DungeonModel();
        model.handleMessage(FORGERY_ENTRY);
        model.handleMessage("Great job! You've completed the CorruptedÀÀÀUndergrowthÀÀÀRuins Dungeon!");
        Assertions.assertFalse(model.isInDungeon());
    }

    @Test
    public void keyAccessAndTrackingInstructionsDoNotStartARun() {
        DungeonModel model = new DungeonModel();
        model.handleMessage("Key Collector: You have access to the dungeon... This time only.");
        model.handleMessage("Tracked Dungeon: Decrepit Sewers");
        model.handleMessage("Complete different dungeons without leaving or dying to get rewards.");
        Assertions.assertFalse(model.isInDungeon());
    }

    @Test
    public void worldChangeClearsRun() {
        DungeonModel model = new DungeonModel();
        model.handleMessage(FORGERY_ENTRY);
        model.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.WORLD, "WC1", false));
        Assertions.assertFalse(model.isInDungeon());
    }

    @Test
    public void onlyServerMessagesCanChangeDungeonState() {
        DungeonModel model = new DungeonModel();
        model.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.WORLD, "WC1", false));
        model.onChat(new ChatMessageEvent.Match(StyledText.fromString(FORGERY_ENTRY), RecipientType.PRIVATE));
        Assertions.assertFalse(model.isInDungeon());
        model.onChat(new ChatMessageEvent.Match(
                StyledText.fromString(
                        "§7You must kill §b25 §7mobs and complete the dungeon for this run to count towards the Forgery Chest."),
                RecipientType.GAME_MESSAGE));
        Assertions.assertTrue(model.isInDungeon());
        model.onChat(new ChatMessageEvent.Match(
                StyledText.fromString("§6Great job! You've completed the CorruptedÀÀÀUndergrowthÀÀÀRuins Dungeon!"),
                RecipientType.INFO));
        Assertions.assertFalse(model.isInDungeon());
    }

    private static Vec3 entrance(Dungeon dungeon, boolean corrupted) {
        Dungeon.DungeonData data =
                (corrupted ? dungeon.getCorruptedDungeonData() : dungeon.getDungeonData()).orElseThrow();
        return new Vec3(data.getXPos(), 65, data.getZPos());
    }
}
