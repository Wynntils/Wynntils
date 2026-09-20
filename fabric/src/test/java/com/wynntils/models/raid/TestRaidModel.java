/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.models.combat.type.DamageDealtEvent;
import com.wynntils.models.raid.event.RaidChallengeEvent;
import com.wynntils.models.raid.event.RaidStartedEvent;
import com.wynntils.models.raid.raids.OrphionsNexusOfLightRaid;
import com.wynntils.models.raid.raids.RaidKind;
import com.wynntils.models.raid.raids.TheCanyonColossusRaid;
import com.wynntils.models.raid.type.RaidInfo;
import com.wynntils.models.raid.type.RaidRoomInfo;
import com.wynntils.models.raid.type.SavableRaidInfo;
import com.wynntils.models.raid.type.SavableRaidRoomInfo;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.type.CappedValue;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.neoforged.bus.api.SubscribeEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestRaidModel {
    private static final long RAID_START_TIME = 1_780_000_000_000L;
    private static final long MINUTE = 60_000L;

    private static final RaidKind CANYON_RAID = new TheCanyonColossusRaid();
    private static final RaidKind NEXUS_RAID = new OrphionsNexusOfLightRaid();
    private static final String CANYON = CANYON_RAID.getRaidName();
    private static final String NEXUS = NEXUS_RAID.getRaidName();

    private EventRecorder recorder;

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        resetModelState();
        recorder = new EventRecorder();
        WynntilsMod.registerEventListener(recorder);
    }

    @AfterEach
    public void afterEach() throws Exception {
        WynntilsMod.unregisterEventListener(recorder);
        resetModelState();
    }

    // ==================================================================
    // Check raid restore conditions
    // ==================================================================

    @Test
    public void restoreFiresOnFirstJoinWorldWhenStorageHasBackup() throws Exception {
        setSavedRaidInfo(new SavableRaidInfo(CANYON, RAID_START_TIME, Map.of()));

        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));

        Assertions.assertNotNull(getCurrentRaid());
        Assertions.assertEquals(1, recorder.raidStartedCount);
        Assertions.assertEquals(CANYON, recorder.lastRaidKind.getRaidName(), "event carries the restored kind");
    }

    @Test
    public void restoreDoesNotFireWhenThereIsNoBackup() throws Exception {
        Assertions.assertTrue(
                getSavedRaidInfo().raidName().isEmpty(), "precondition: storage holds the empty placeholder");

        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "test", true));

        Assertions.assertNull(getCurrentRaid());
        Assertions.assertEquals(0, recorder.raidStartedCount);
    }

    @Test
    public void restoreDoesNotFireWhenNotFirstJoinWorld() throws Exception {
        setSavedRaidInfo(new SavableRaidInfo(CANYON, RAID_START_TIME, Map.of()));

        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", false));

        Assertions.assertNull(getCurrentRaid());
        Assertions.assertEquals(0, recorder.raidStartedCount);
    }

    @Test
    public void restoreDoesNotFireWhenRejoiningViaCharacterSelection() throws Exception {
        setSavedRaidInfo(new SavableRaidInfo(CANYON, RAID_START_TIME, Map.of()));

        Models.Raid.onWorldStateChange(
                new WorldStateEvent(WorldState.WORLD, WorldState.CHARACTER_SELECTION, "NA1", true));

        Assertions.assertNull(getCurrentRaid(), "raid must not be restored from a CHARACTER_SELECTION transition");
        Assertions.assertEquals(0, recorder.raidStartedCount, "no RaidStartedEvent should be posted");
        Assertions.assertFalse(flag("awaitingRaidResume"), "no resume wait should be started");
        Assertions.assertNull(resumeTask(), "no resume timeout should be scheduled");
        Assertions.assertTrue(
                getSavedRaidInfo().raidName().isEmpty(),
                "a non-reconnect join means the backup is stale and must be wiped");
    }

    @Test
    public void characterSelectionJoinWipesStaleBackupSoNoRaidIsRestoredLater() throws Exception {
        setSavedRaidInfo(new SavableRaidInfo(CANYON, RAID_START_TIME, Map.of()));

        Models.Raid.onWorldStateChange(
                new WorldStateEvent(WorldState.WORLD, WorldState.CHARACTER_SELECTION, "NA1", true));
        Assertions.assertTrue(getSavedRaidInfo().raidName().isEmpty(), "stale backup must be cleared on a normal join");

        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));

        Assertions.assertNull(getCurrentRaid(), "no raid to restore");
        Assertions.assertEquals(0, recorder.raidStartedCount);
        Assertions.assertFalse(flag("awaitingRaidResume"));
        Assertions.assertNull(resumeTask());
    }

    @Test
    public void restoreDoesNotFireWhenNewStateIsNotWorld() throws Exception {
        setSavedRaidInfo(new SavableRaidInfo(CANYON, RAID_START_TIME, Map.of()));

        Models.Raid.onWorldStateChange(
                new WorldStateEvent(WorldState.CHARACTER_SELECTION, WorldState.INTERIM, "NA1", true));

        Assertions.assertNull(getCurrentRaid());
        Assertions.assertEquals(0, recorder.raidStartedCount);
    }

    @Test
    public void restoreDoesNotFireWhenRaidTrackingIsDisabled() throws Exception {
        setSavedRaidInfo(new SavableRaidInfo(CANYON, RAID_START_TIME, Map.of()));
        Models.Raid.trackRaids.store(false);

        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));

        Assertions.assertNull(getCurrentRaid());
        Assertions.assertEquals(0, recorder.raidStartedCount);
    }

    @Test
    public void restoreDoesNotFireWhenARaidIsAlreadyActive() throws Exception {
        Models.Raid.onTitle(new TitleSetTextEvent(CANYON_RAID.getEntryTitle().getComponent()));
        RaidInfo currentRaid = getCurrentRaid();
        Assertions.assertNotNull(currentRaid, "the title must start a raid");

        setSavedRaidInfo(new SavableRaidInfo(NEXUS, RAID_START_TIME, Map.of()));

        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));

        Assertions.assertSame(currentRaid, getCurrentRaid(), "existing raid must not be replaced");
        Assertions.assertEquals(1, recorder.raidStartedCount, "only current raid should exist, not stored raid");
    }

    // ==================================================================
    // Check restore raid content conditions
    // ==================================================================

    @Test
    public void restoreOfARaidWithNoRoomsYetTracksRoomOneWhenItStarts() throws Exception {
        // The crash happened right after entering the raid, before the first challenge began.
        setSavedRaidInfo(new SavableRaidInfo(CANYON, RAID_START_TIME, Map.of()));

        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));

        RaidInfo restored = getCurrentRaid();
        Assertions.assertNotNull(restored);
        Assertions.assertEquals(-1, restored.getCurrentChallengeNum());
        Assertions.assertNull(restored.getCurrentRoom());
        Assertions.assertFalse(flag("completedCurrentChallenge"), "no room yet, so nothing can be completed");

        // The scoreboard confirms the raid and shows the first challenge.
        Models.Raid.notifyRaidScoreboardShown();
        Models.Raid.tryStartChallenge(scoreboardLine(CANYON_RAID, 1));

        Assertions.assertEquals(1, restored.getCurrentChallengeNum());
        Assertions.assertEquals(
                roomName(CANYON_RAID, 1), restored.getCurrentRoom().getRoomName());
        Assertions.assertTrue(getSavedRaidInfo().challenges().containsKey(1), "room 1 is persisted as usual");
    }

    @Test
    public void restoreMarksChallengeCompletedWhenLastRoomHasEndTime() throws Exception {
        // Room 1 was completed: it started 1 minute in and ran for 1 minute.
        setSavedRaidInfo(new SavableRaidInfo(
                NEXUS,
                RAID_START_TIME,
                Map.of(
                        1,
                        new SavableRaidRoomInfo(
                                roomName(NEXUS_RAID, 1),
                                RAID_START_TIME + 1 * MINUTE,
                                RAID_START_TIME + 2 * MINUTE,
                                0L))));

        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));

        Assertions.assertNotNull(getCurrentRaid());
        Assertions.assertTrue(flag("completedCurrentChallenge"), "last room has an end time -> it was completed");
    }

    @Test
    public void restoreRestoresEveryRoom() throws Exception {
        // Rooms 1-4 completed. room 5 (Parasite) still in progress.
        setSavedRaidInfo(new SavableRaidInfo(
                NEXUS,
                RAID_START_TIME,
                Map.of(
                        1,
                        new SavableRaidRoomInfo(
                                roomName(NEXUS_RAID, 1),
                                RAID_START_TIME + 1 * MINUTE,
                                RAID_START_TIME + 2 * MINUTE,
                                100L),
                        2,
                        new SavableRaidRoomInfo(
                                roomName(NEXUS_RAID, 2),
                                RAID_START_TIME + 3 * MINUTE,
                                RAID_START_TIME + 4 * MINUTE,
                                200L),
                        3,
                        new SavableRaidRoomInfo(
                                roomName(NEXUS_RAID, 3),
                                RAID_START_TIME + 5 * MINUTE,
                                RAID_START_TIME + 6 * MINUTE,
                                300L),
                        4,
                        new SavableRaidRoomInfo(
                                roomName(NEXUS_RAID, 4),
                                RAID_START_TIME + 7 * MINUTE,
                                RAID_START_TIME + 8 * MINUTE,
                                400L),
                        5,
                        new SavableRaidRoomInfo(roomName(NEXUS_RAID, 5), RAID_START_TIME + 9 * MINUTE, -1L, 500L))));

        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));

        RaidInfo restored = getCurrentRaid();
        Assertions.assertNotNull(restored);
        Assertions.assertEquals(RAID_START_TIME, restored.getRaidStartTime());
        Assertions.assertEquals(5, restored.getChallenges().size());
        Assertions.assertEquals(5, restored.getCurrentChallengeNum(), "the in-progress room is current");
        Assertions.assertFalse(flag("completedCurrentChallenge"), "the last room has no end time");
        Assertions.assertEquals(1_500L, restored.getRaidDamage(), "total damage is the sum of all rooms");

        // Room 1
        Assertions.assertEquals(
                roomName(NEXUS_RAID, 1), restored.getRoomByNumber(1).getRoomName());
        Assertions.assertEquals(
                RAID_START_TIME + 1 * MINUTE, restored.getRoomByNumber(1).getRoomStartTime());
        Assertions.assertEquals(
                RAID_START_TIME + 2 * MINUTE, restored.getRoomByNumber(1).getRoomEndTime());
        Assertions.assertEquals(MINUTE, restored.getRoomByNumber(1).getRoomTotalTime());
        Assertions.assertEquals(100L, restored.getRoomByNumber(1).getRoomDamage());

        // Room 2
        Assertions.assertEquals(
                roomName(NEXUS_RAID, 2), restored.getRoomByNumber(2).getRoomName());
        Assertions.assertEquals(
                RAID_START_TIME + 3 * MINUTE, restored.getRoomByNumber(2).getRoomStartTime());
        Assertions.assertEquals(
                RAID_START_TIME + 4 * MINUTE, restored.getRoomByNumber(2).getRoomEndTime());
        Assertions.assertEquals(MINUTE, restored.getRoomByNumber(2).getRoomTotalTime());
        Assertions.assertEquals(200L, restored.getRoomByNumber(2).getRoomDamage());

        // Room 3
        Assertions.assertEquals(
                roomName(NEXUS_RAID, 3), restored.getRoomByNumber(3).getRoomName());
        Assertions.assertEquals(
                RAID_START_TIME + 5 * MINUTE, restored.getRoomByNumber(3).getRoomStartTime());
        Assertions.assertEquals(
                RAID_START_TIME + 6 * MINUTE, restored.getRoomByNumber(3).getRoomEndTime());
        Assertions.assertEquals(MINUTE, restored.getRoomByNumber(3).getRoomTotalTime());
        Assertions.assertEquals(300L, restored.getRoomByNumber(3).getRoomDamage());

        // Room 4
        Assertions.assertEquals(
                roomName(NEXUS_RAID, 4), restored.getRoomByNumber(4).getRoomName());
        Assertions.assertEquals(
                RAID_START_TIME + 7 * MINUTE, restored.getRoomByNumber(4).getRoomStartTime());
        Assertions.assertEquals(
                RAID_START_TIME + 8 * MINUTE, restored.getRoomByNumber(4).getRoomEndTime());
        Assertions.assertEquals(MINUTE, restored.getRoomByNumber(4).getRoomTotalTime());
        Assertions.assertEquals(400L, restored.getRoomByNumber(4).getRoomDamage());

        // Room 5
        Assertions.assertEquals(
                roomName(NEXUS_RAID, 5), restored.getRoomByNumber(5).getRoomName());
        Assertions.assertEquals(
                RAID_START_TIME + 9 * MINUTE, restored.getRoomByNumber(5).getRoomStartTime());
        Assertions.assertEquals(-1L, restored.getRoomByNumber(5).getRoomEndTime());
        Assertions.assertEquals(500L, restored.getRoomByNumber(5).getRoomDamage());
    }

    @Test
    public void invalidRaidNameInStorageIsWipedAndNoRaidIsRestored() throws Exception {
        setSavedRaidInfo(new SavableRaidInfo("NotARealRaid", RAID_START_TIME, Map.of()));

        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));

        Assertions.assertNull(getCurrentRaid());
        Assertions.assertEquals(0, recorder.raidStartedCount);
        Assertions.assertTrue(getSavedRaidInfo().raidName().isEmpty(), "unparseable raid got parsed");
    }

    // ==================================================================
    // Persistence through the real production paths
    // ==================================================================

    @Test
    public void backupIsWrittenAtEveryLifecycleStep() throws Exception {
        // Start a new TCC raid via title
        Models.Raid.onTitle(new TitleSetTextEvent(CANYON_RAID.getEntryTitle().getComponent()));
        Assertions.assertNotNull(getCurrentRaid(), "the entry title must start a raid");

        SavableRaidInfo saved = getSavedRaidInfo();
        Assertions.assertEquals(CANYON, saved.raidName());
        Assertions.assertEquals(getCurrentRaid().getRaidStartTime(), saved.raidStartTime());
        Assertions.assertTrue(saved.challenges().isEmpty());

        // Scoreboard line for room 1: tryStartChallenge persists the new room
        Models.Raid.tryStartChallenge(scoreboardLine(CANYON_RAID, 1));

        SavableRaidRoomInfo savedRoom = getSavedRaidInfo().challenges().get(1);
        Assertions.assertNotNull(savedRoom, "tryStartChallenge must persist the new room");
        Assertions.assertEquals(roomName(CANYON_RAID, 1), savedRoom.roomName());
        Assertions.assertEquals(-1L, savedRoom.roomEndTime());
        Assertions.assertEquals(1, recorder.challengeStartedCount);

        // Damage: characterisation. Damage dealt during a room isn't persisted
        // immediately and only reaches storage at the next persist point.
        // Update this assertion if damage persistence is added in the future.
        Models.Raid.onDamageDealtEvent(new DamageDealtEvent(Map.of(DamageType.NEUTRAL, 300L)));

        Assertions.assertEquals(300L, getCurrentRaid().getRoomByNumber(1).getRoomDamage());
        Assertions.assertEquals(0L, getSavedRaidInfo().challenges().get(1).roomDamage(), "not persisted mid-room");

        // Completion: completeChallenge persists the end time and the room's damage
        Models.Raid.completeChallenge();

        savedRoom = getSavedRaidInfo().challenges().get(1);
        Assertions.assertNotEquals(-1L, savedRoom.roomEndTime(), "completeChallenge must persist the end time");
        Assertions.assertEquals(300L, savedRoom.roomDamage());
        Assertions.assertEquals(1, recorder.challengeCompletedCount);
    }

    @Test
    public void repeatedScoreboardUpdatesForTheSameRoomAreIgnored() throws Exception {
        Models.Raid.onTitle(new TitleSetTextEvent(CANYON_RAID.getEntryTitle().getComponent()));
        Models.Raid.tryStartChallenge(scoreboardLine(CANYON_RAID, 1));
        RaidRoomInfo room1 = getCurrentRaid().getRoomByNumber(1);

        // The scoreboard is re-sent constantly; none of these may restart the room or spam events.
        Models.Raid.tryStartChallenge(scoreboardLine(CANYON_RAID, 1));
        Models.Raid.tryStartChallenge(scoreboardLine(CANYON_RAID, 1));

        Assertions.assertSame(room1, getCurrentRaid().getRoomByNumber(1));
        Assertions.assertEquals(1, recorder.challengeStartedCount);
    }

    @Test
    public void startingTheNextRoomResetsPerRoomFlags() throws Exception {
        Models.Raid.onTitle(new TitleSetTextEvent(CANYON_RAID.getEntryTitle().getComponent()));
        Models.Raid.tryStartChallenge(scoreboardLine(CANYON_RAID, 1));
        Models.Raid.completeChallenge();
        Models.Raid.tryEnterChallengeIntermission();
        Assertions.assertTrue(flag("completedCurrentChallenge"));
        Assertions.assertTrue(flag("inIntermissionRoom"));

        Models.Raid.tryStartChallenge(scoreboardLine(CANYON_RAID, 2));

        Assertions.assertEquals(2, getCurrentRaid().getCurrentChallengeNum());
        Assertions.assertFalse(flag("completedCurrentChallenge"));
        Assertions.assertFalse(flag("inIntermissionRoom"));
    }

    @Test
    public void liveRaidSurvivesACrash() throws Exception {
        // Play through room 1 and into room 2
        Models.Raid.onTitle(new TitleSetTextEvent(CANYON_RAID.getEntryTitle().getComponent()));
        Models.Raid.tryStartChallenge(scoreboardLine(CANYON_RAID, 1));
        Models.Raid.onDamageDealtEvent(new DamageDealtEvent(Map.of(DamageType.NEUTRAL, 500L)));
        Models.Raid.completeChallenge();
        Models.Raid.tryStartChallenge(scoreboardLine(CANYON_RAID, 2));
        long raidStart = getCurrentRaid().getRaidStartTime();

        // Crash: only storage is kept. Then rejoin.
        simulateCrash();
        Assertions.assertNull(getCurrentRaid());
        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));

        RaidInfo restored = getCurrentRaid();
        Assertions.assertNotNull(restored, "live raid must survive a crash");
        Assertions.assertEquals(CANYON, restored.getRaidKind().getRaidName());
        Assertions.assertEquals(raidStart, restored.getRaidStartTime());
        Assertions.assertEquals(2, restored.getCurrentChallengeNum(), "still in room 2");
        Assertions.assertEquals(
                roomName(CANYON_RAID, 2), restored.getCurrentRoom().getRoomName());
        Assertions.assertEquals(-1L, restored.getCurrentRoom().getRoomEndTime());

        RaidRoomInfo room1 = restored.getRoomByNumber(1);
        Assertions.assertEquals(roomName(CANYON_RAID, 1), room1.getRoomName());
        Assertions.assertEquals(500L, room1.getRoomDamage());
        Assertions.assertNotEquals(-1L, room1.getRoomEndTime(), "room 1 was completed before the crash");
        Assertions.assertEquals(2, recorder.raidStartedCount, "one event for the live start, one for the restore");
    }

    @Test
    public void firstScoreboardUpdateAfterRestoreDoesNotResetTheCurrentRoom() throws Exception {
        // Room 1 done (~1 minute), room 2 in progress.
        SavableRaidInfo saved = new SavableRaidInfo(
                CANYON,
                RAID_START_TIME,
                Map.of(
                        1,
                        new SavableRaidRoomInfo(
                                roomName(CANYON_RAID, 1),
                                RAID_START_TIME + 1 * MINUTE,
                                RAID_START_TIME + 2 * MINUTE,
                                500L),
                        2,
                        new SavableRaidRoomInfo(roomName(CANYON_RAID, 2), RAID_START_TIME + 3 * MINUTE, -1L, 0L)));
        setSavedRaidInfo(saved);
        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));
        Models.Raid.notifyRaidScoreboardShown();

        RaidInfo restored = getCurrentRaid();
        Assertions.assertNotNull(restored);
        RaidRoomInfo restoredRoom = restored.getCurrentRoom();

        // The scoreboard confirms we are still in the room we were in.
        Models.Raid.tryStartChallenge(scoreboardLine(CANYON_RAID, 2));

        Assertions.assertSame(restoredRoom, restored.getCurrentRoom());
        Assertions.assertEquals(
                RAID_START_TIME + 3 * MINUTE, restoredRoom.getRoomStartTime(), "start time must not be reset");
        Assertions.assertEquals(0, recorder.challengeStartedCount, "no Started event for a room we were already in");
        Assertions.assertEquals(saved, getSavedRaidInfo(), "restoring and confirming must not alter the backup");
    }

    // ==================================================================
    // Skip-ahead: crash in one room, rejoin in a later one
    // ==================================================================

    @Test
    public void rejoinAtLaterRoomAbandonsTheInProgressRoomAndSurvivesASecondCrash() throws Exception {
        // Room 1 was in progress: started 1 minute after the raid began, no end time yet.
        setSavedRaidInfo(new SavableRaidInfo(
                NEXUS,
                RAID_START_TIME,
                Map.of(1, new SavableRaidRoomInfo(roomName(NEXUS_RAID, 1), RAID_START_TIME + 1 * MINUTE, -1L, 750L))));

        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));

        RaidInfo restored = getCurrentRaid();
        Assertions.assertNotNull(restored);
        Assertions.assertEquals(1, restored.getCurrentChallengeNum());
        Assertions.assertEquals(-1L, restored.getRoomByNumber(1).getRoomEndTime());

        // The scoreboard reports room 3: room 1 is abandoned, room 2 never existed.
        Models.Raid.tryStartChallenge(scoreboardLine(NEXUS_RAID, 3));

        // Room 1
        Assertions.assertEquals(
                RAID_START_TIME + 1 * MINUTE,
                restored.getRoomByNumber(1).getRoomEndTime(),
                "abandoned: endTime = startTime");
        Assertions.assertEquals(0L, restored.getRoomByNumber(1).getRoomTotalTime(), "abandoned room time is 0");
        Assertions.assertEquals(750L, restored.getRoomByNumber(1).getRoomDamage(), "abandoned room keeps its damage");

        // Room 2
        Assertions.assertNull(restored.getRoomByNumber(2), "skipped room 2 was never started");

        // Room 3
        Assertions.assertEquals(3, restored.getCurrentChallengeNum());
        Assertions.assertEquals(
                roomName(NEXUS_RAID, 3), restored.getCurrentRoom().getRoomName());
        Assertions.assertEquals(-1L, restored.getCurrentRoom().getRoomEndTime(), "room 3 is in progress");

        // The skip must have been persisted: only rooms 1 and 3 exist in storage.
        Assertions.assertEquals(Set.of(1, 3), getSavedRaidInfo().challenges().keySet());
        Assertions.assertEquals(
                RAID_START_TIME + 1 * MINUTE,
                getSavedRaidInfo().challenges().get(1).roomEndTime());
        Assertions.assertEquals(-1L, getSavedRaidInfo().challenges().get(3).roomEndTime());

        // Second crash: storage must bring back exactly the same state.
        simulateCrash();
        Assertions.assertNull(getCurrentRaid());
        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));

        RaidInfo second = getCurrentRaid();
        Assertions.assertNotNull(second);

        // Room 1
        Assertions.assertEquals(
                RAID_START_TIME + 1 * MINUTE, second.getRoomByNumber(1).getRoomEndTime(), "room 1 stays abandoned");
        Assertions.assertEquals(0L, second.getRoomByNumber(1).getRoomTotalTime());
        Assertions.assertEquals(750L, second.getRoomByNumber(1).getRoomDamage());
        Assertions.assertNull(second.getRoomByNumber(2), "room 2 still must not exist");

        // Room 2
        Assertions.assertEquals(3, second.getCurrentChallengeNum(), "room 3 is still current after two crashes");
        Assertions.assertEquals(roomName(NEXUS_RAID, 3), second.getCurrentRoom().getRoomName());
        Assertions.assertEquals(-1L, second.getCurrentRoom().getRoomEndTime());
    }

    @Test
    public void rejoinAtLaterRoomDoesNotAbandonCompletedRooms() throws Exception {
        // Rooms 1-2 done, room 3 in progress
        setSavedRaidInfo(new SavableRaidInfo(
                NEXUS,
                RAID_START_TIME,
                Map.of(
                        1,
                        new SavableRaidRoomInfo(
                                roomName(NEXUS_RAID, 1),
                                RAID_START_TIME + 1 * MINUTE,
                                RAID_START_TIME + 2 * MINUTE,
                                100L),
                        2,
                        new SavableRaidRoomInfo(
                                roomName(NEXUS_RAID, 2),
                                RAID_START_TIME + 3 * MINUTE,
                                RAID_START_TIME + 4 * MINUTE,
                                200L),
                        3,
                        new SavableRaidRoomInfo(roomName(NEXUS_RAID, 3), RAID_START_TIME + 5 * MINUTE, -1L, 300L))));
        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));

        RaidInfo restored = getCurrentRaid();
        Assertions.assertNotNull(restored);

        // Skip from room 3 straight to room 5.
        Models.Raid.tryStartChallenge(scoreboardLine(NEXUS_RAID, 5));

        Assertions.assertEquals(
                RAID_START_TIME + 2 * MINUTE, restored.getRoomByNumber(1).getRoomEndTime(), "room 1 stays completed");
        Assertions.assertEquals(
                RAID_START_TIME + 4 * MINUTE, restored.getRoomByNumber(2).getRoomEndTime(), "room 2 stays completed");
        Assertions.assertEquals(
                RAID_START_TIME + 5 * MINUTE,
                restored.getRoomByNumber(3).getRoomEndTime(),
                "in-progress room 3 abandoned");
        Assertions.assertEquals(0L, restored.getRoomByNumber(3).getRoomTotalTime());
        Assertions.assertEquals(300L, restored.getRoomByNumber(3).getRoomDamage(), "abandoned room keeps its damage");
        Assertions.assertNull(restored.getRoomByNumber(4), "skipped boss was never started");
        Assertions.assertEquals(5, restored.getCurrentChallengeNum());
    }

    // ==================================================================
    // Resume: only trust a restored raid after the scoreboard confirms it
    // ==================================================================

    @Test
    public void restoredRaidWaitsForScoreboardAndIsKeptOnceConfirmed() throws Exception {
        setSavedRaidInfo(new SavableRaidInfo(CANYON, RAID_START_TIME, Map.of()));
        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));

        Assertions.assertNotNull(getCurrentRaid());
        Assertions.assertTrue(flag("awaitingRaidResume"), "must wait for the scoreboard before trusting the backup");
        Assertions.assertNotNull(resumeTask(), "a timeout must be scheduled for the wait");

        Models.Raid.notifyRaidScoreboardShown();

        Assertions.assertFalse(flag("awaitingRaidResume"));
        Assertions.assertNull(resumeTask(), "timeout must be cancelled");
        Assertions.assertNotNull(getCurrentRaid(), "raid is kept once the scoreboard confirms it");

        runResumeTimeout();
        Assertions.assertNotNull(getCurrentRaid());
    }

    @Test
    public void timeoutWithoutScoreboardDiscardsTheRestoredRaidAndWipesTheBackup() throws Exception {
        setSavedRaidInfo(new SavableRaidInfo(CANYON, RAID_START_TIME, Map.of()));
        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));
        Assertions.assertNotNull(getCurrentRaid());

        runResumeTimeout();

        Assertions.assertNull(getCurrentRaid(), "unconfirmed raid must be dropped");
        Assertions.assertFalse(flag("awaitingRaidResume"));
        Assertions.assertNull(resumeTask());
        Assertions.assertTrue(getSavedRaidInfo().raidName().isEmpty(), "stale backup must be wiped");
    }

    // ==================================================================
    // Ending a raid clears everything, including the backup
    // ==================================================================

    @Test
    public void failingARaidWipesTheBackupCancelsTheResumeAndResetsSessionState() throws Exception {
        setSavedRaidInfo(new SavableRaidInfo(CANYON, RAID_START_TIME, Map.of()));
        Models.Raid.onWorldStateChange(new WorldStateEvent(WorldState.WORLD, WorldState.INTERIM, "NA1", true));
        Assertions.assertNotNull(getCurrentRaid());
        Assertions.assertNotNull(resumeTask(), "precondition: there is a pending resume to cancel");

        setRaidField("inBuffRoom", true);
        setRaidField("inIntermissionRoom", true);
        setRaidField("parasiteOvertaken", true);
        setRaidField("completedCurrentChallenge", true);
        setRaidField("timeLeft", 123);
        setRaidField("challenges", new CappedValue(2, 4));
        getPartyRaidBuffs().put("SomePlayer", new ArrayList<>(List.of("SomeBuff")));

        Models.Raid.failedRaid();

        Assertions.assertNull(getCurrentRaid());
        Assertions.assertTrue(getSavedRaidInfo().raidName().isEmpty(), "backup wiped when the raid ends");
        Assertions.assertFalse(flag("awaitingRaidResume"));
        Assertions.assertNull(resumeTask());
        Assertions.assertFalse(flag("inBuffRoom"));
        Assertions.assertFalse(flag("inIntermissionRoom"));
        Assertions.assertFalse(flag("parasiteOvertaken"));
        Assertions.assertFalse(flag("completedCurrentChallenge"));
        Assertions.assertEquals(0, raidField("timeLeft", Integer.class));
        Assertions.assertEquals(CappedValue.EMPTY, raidField("challenges", CappedValue.class));
        Assertions.assertTrue(Models.Raid.getChosenBuffs("SomePlayer").isEmpty());
        Assertions.assertEquals(1, Models.Raid.historicRaids.get().size(), "failed raid is archived");
    }

    @Test
    public void completingARaidWipesTheBackup() throws Exception {
        Models.Raid.onTitle(new TitleSetTextEvent(CANYON_RAID.getEntryTitle().getComponent()));
        Models.Raid.tryStartChallenge(scoreboardLine(CANYON_RAID, 1));
        Assertions.assertEquals(CANYON, getSavedRaidInfo().raidName(), "precondition: a backup exists");

        // The title-matching regex is not what is under test, so call the completion step directly.
        callPrivate("completeRaid");

        Assertions.assertNull(getCurrentRaid());
        Assertions.assertTrue(getSavedRaidInfo().raidName().isEmpty(), "backup wiped when the raid completes");
        Assertions.assertEquals(1, Models.Raid.historicRaids.get().size(), "completed raid is archived");
    }

    // ==================================================================
    // Access RaidModel
    // ==================================================================

    private static String roomName(RaidKind raid, int roomNumber) throws Exception {
        return roomEntry(raid, roomNumber).getValue();
    }

    private static StyledText scoreboardLine(RaidKind raid, int roomNumber) throws Exception {
        return StyledText.fromString(roomEntry(raid, roomNumber).getKey());
    }

    private static Map.Entry<String, String> roomEntry(RaidKind raid, int roomNumber) throws Exception {
        Map<String, String> rooms = roomMap(raid).get(roomNumber);
        Assertions.assertNotNull(rooms, "No room " + roomNumber + " in " + raid.getRaidName());

        return rooms.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("No scoreboard line for room " + roomNumber + " in " + raid.getRaidName()));
    }

    private static Map<Integer, Map<String, String>> roomMap(RaidKind raid) throws Exception {
        Field field = RaidKind.class.getDeclaredField("challengeNames");
        field.setAccessible(true);
        return (Map<Integer, Map<String, String>>) field.get(raid);
    }

    private static RaidInfo getCurrentRaid() throws Exception {
        return raidField("currentRaid", RaidInfo.class);
    }

    private static boolean flag(String name) throws Exception {
        return raidField(name, Boolean.class);
    }

    private static Object resumeTask() throws Exception {
        return raidField("raidResumeTask", Object.class);
    }

    private static Storage<SavableRaidInfo> savedRaidStorage() throws Exception {
        return (Storage<SavableRaidInfo>) raidField("savedRaidInfo", Storage.class);
    }

    private static SavableRaidInfo getSavedRaidInfo() throws Exception {
        return savedRaidStorage().get();
    }

    private static void setSavedRaidInfo(SavableRaidInfo info) throws Exception {
        savedRaidStorage().store(info);
    }

    private static Map<String, List<String>> getPartyRaidBuffs() throws Exception {
        return (Map<String, List<String>>) raidField("partyRaidBuffs", Map.class);
    }

    private static Map<String, Long> getBestTimes() throws Exception {
        return ((Storage<Map<String, Long>>) raidField("bestTimes", Storage.class)).get();
    }

    private static <T> T raidField(String name, Class<T> type) throws Exception {
        Field field = Models.Raid.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(Models.Raid));
    }

    private static void setRaidField(String name, Object value) throws Exception {
        Field field = Models.Raid.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(Models.Raid, value);
    }

    private static void callPrivate(String methodName) throws Exception {
        Method method = Models.Raid.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(Models.Raid);
    }

    private static void runResumeTimeout() throws Exception {
        try {
            callPrivate("checkRaidResume");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            boolean chatMessageFailure = cause instanceof NullPointerException
                    && cause.getStackTrace().length > 0
                    && cause.getStackTrace()[0].getClassName().endsWith("McUtils");

            if (!chatMessageFailure) throw e;
        }
    }

    // ==================================================================
    // State reset
    // ==================================================================

    private static void simulateCrash() throws Exception {
        callPrivate("cancelPendingResume");

        getPartyRaidBuffs().clear();

        setRaidField("currentRaid", null);
        setRaidField("completedCurrentChallenge", false);
        setRaidField("inBuffRoom", false);
        setRaidField("inIntermissionRoom", false);
        setRaidField("parasiteOvertaken", false);
        setRaidField("challenges", CappedValue.EMPTY);
        setRaidField("timeLeft", 0);
        setRaidField("awaitingRaidResume", false);
        setRaidField("raidResumeTask", null);
    }

    private static void resetModelState() throws Exception {
        simulateCrash();

        setSavedRaidInfo(new SavableRaidInfo("", -1L, Map.of()));
        Models.Raid.trackRaids.store(true);
        Models.Raid.historicRaids.get().clear();
        getBestTimes().clear();
    }

    // ==================================================================
    // Event recorder
    // ==================================================================

    private static final class EventRecorder {
        private int raidStartedCount = 0;
        private int challengeStartedCount = 0;
        private int challengeCompletedCount = 0;
        private RaidKind lastRaidKind;

        @SubscribeEvent
        public void onRaidStarted(RaidStartedEvent event) {
            raidStartedCount++;
            lastRaidKind = event.getRaidKind();
        }

        @SubscribeEvent
        public void onChallengeStarted(RaidChallengeEvent.Started event) {
            challengeStartedCount++;
        }

        @SubscribeEvent
        public void onChallengeCompleted(RaidChallengeEvent.Completed event) {
            challengeCompletedCount++;
        }
    }
}
