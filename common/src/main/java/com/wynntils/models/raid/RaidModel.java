/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.mc.event.PlayerTeleportEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.models.raid.event.RaidBossStartedEvent;
import com.wynntils.models.raid.event.RaidChallengeEvent;
import com.wynntils.models.raid.event.RaidEndedEvent;
import com.wynntils.models.raid.event.RaidNewBestTimeEvent;
import com.wynntils.models.raid.type.RaidKind;
import com.wynntils.models.raid.type.RaidRoomType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.PosUtils;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

public class RaidModel extends Model {
    // For raid entry use title.
    // For start of a challenge, look for an instructions label.
    // For end of a challenge look for BLACKSMITH_LABEL label.
    // For boss intermission look for BOSS_FIGHT_LABEL label.
    // For boss fight look for the boss' label.
    // For raid completion use RAID_COMPLETE title.
    // For failure use check for RAID_FAILED title or chat message.
    // TCC Requires a position check after teleport due to a lack of fail title/message.
    public static final int MAX_CHALLENGES = 3;
    public static final int ROOM_TIMERS_COUNT = 5;
    private static final Position TCC_FAIL_POS = new Vec3(674.5, 49.0, -4447.5);
    private static final StyledText BLACKSMITH_LABEL = StyledText.fromString("§dBlacksmith");
    private static final StyledText BOSS_FIGHT_LABEL = StyledText.fromString("§4§l[§cBoss Fight§4§l]");
    // Title gradually builds up to full "§a§lRAID COMPLETED!"
    private static final StyledText RAID_COMPLETE = StyledText.fromString("§a§lD C");
    private static final StyledText RAID_FAILED = StyledText.fromString("§4Raid Failed!");

    @Persisted
    private final Storage<Map<String, Long>> bestTimes = new Storage<>(new TreeMap<>());

    private final Map<RaidRoomType, Long> roomTimers = new EnumMap<>(RaidRoomType.class);

    private long raidStartTime;
    private long roomStartTime;
    private RaidKind currentRaid = null;
    private RaidRoomType currentRoom = null;

    public RaidModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onTitle(TitleSetTextEvent event) {
        Component component = event.getComponent();
        StyledText styledText = StyledText.fromComponent(component);

        if (currentRaid == null) {
            currentRaid = RaidKind.fromTitle(styledText);

            if (currentRaid != null) {
                // In a raid, set to intro room and start timer
                currentRoom = RaidRoomType.INTRO;
                raidStartTime = System.currentTimeMillis();
            }
            return;
        }

        if (styledText.equals(RAID_COMPLETE)) {
            // Add the boss time to room timers
            long bossTime = System.currentTimeMillis() - roomStartTime;
            roomTimers.put(RaidRoomType.BOSS_FIGHT, bossTime);

            // Raid has been completed, post event with timers
            WynntilsMod.postEvent(new RaidEndedEvent.Completed(currentRaid, getAllRoomTimes(), currentRaidTime()));

            checkForNewPersonalBest(currentRaid, currentRaidTime());

            currentRaid = null;
            currentRoom = null;
            roomTimers.clear();
        } else if (styledText.equals(RAID_FAILED)) {
            // Raid failed, post event with timers
            WynntilsMod.postEvent(new RaidEndedEvent.Failed(currentRaid, getAllRoomTimes(), currentRaidTime()));

            currentRaid = null;
            currentRoom = null;
            roomTimers.clear();
        }
    }

    @SubscribeEvent
    public void onLabelChange(EntityLabelChangedEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) return;
        if (currentRaid == null) return;
        if (currentRoom == RaidRoomType.BOSS_FIGHT) return;

        if (currentRoom == RaidRoomType.BOSS_INTERMISSION) { // Look for the raid boss
            if (event.getName().equals(currentRaid.getBossLabel())) {
                WynntilsMod.postEvent(new RaidBossStartedEvent(currentRaid));
                updateRoom();
            }
        } else if (inChallengeRoom()) { // Look for blacksmith to indicate challenge complete
            if (event.getName().equals(BLACKSMITH_LABEL)) {
                WynntilsMod.postEvent(new RaidChallengeEvent.Completed(currentRaid, currentRoom));
                updateRoom();
            }
        } else { // Either in a power up room or intro
            if (currentRoom == RaidRoomType.POWERUP_3) { // Look for boss fight label
                if (event.getName().equals(BOSS_FIGHT_LABEL)) {
                    updateRoom();
                }
            } else { // Look for label indicating challenge start
                // Check each known instruction pattern for the current raid until one is found to indicate a new
                // challenge has begun.
                for (Pattern pattern : currentRaid.getInstructionsPatterns()) {
                    if (event.getName().matches(pattern)) {
                        WynntilsMod.postEvent(new RaidChallengeEvent.Started(currentRaid, currentRoom));
                        updateRoom();
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onChatReceived(ChatMessageReceivedEvent event) {
        if (currentRaid == null) return;

        Component component = event.getMessage();
        StyledText styledText = StyledText.fromComponent(component);

        if (styledText.equals(RAID_FAILED)) {
            // Raid failed, post event with timers
            WynntilsMod.postEvent(new RaidEndedEvent.Failed(currentRaid, getAllRoomTimes(), currentRaidTime()));

            currentRaid = null;
            currentRoom = null;
            roomTimers.clear();
        }
    }

    // TCC is the only raid to not display a RAID_FAILED title or chat message so we have to
    // check for failure by being teleported back to the raid starting area
    @SubscribeEvent
    public void onTeleport(PlayerTeleportEvent e) {
        if (currentRaid != RaidKind.THE_CANYON_COLOSSUS) return;

        if (PosUtils.closerThanIgnoringY(e.getNewPosition(), TCC_FAIL_POS, 5)) {
            // Raid failed, post event with timers
            WynntilsMod.postEvent(new RaidEndedEvent.Failed(currentRaid, getAllRoomTimes(), currentRaidTime()));

            currentRaid = null;
            currentRoom = null;
            roomTimers.clear();
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        // Only want to send the message once the user has returned to an actual world
        if (currentRaid != null && event.getNewState() == WorldState.WORLD) {
            currentRaid = null;
            currentRoom = null;
            roomTimers.clear();

            McUtils.sendMessageToClient(Component.literal(
                            "Raid tracking has been interrupted, you will not be able to see progress for the current raid")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }

    public long getRaidBestTime(RaidKind raidKind) {
        return bestTimes.get().getOrDefault(raidKind.getName(), -1L);
    }

    public RaidKind getCurrentRaid() {
        return currentRaid;
    }

    public RaidRoomType getCurrentRoom() {
        return currentRoom;
    }

    public long getRoomTime(RaidRoomType roomType) {
        if (roomType == currentRoom) {
            return currentRoomTime();
        }

        return roomTimers.getOrDefault(roomType, -1L);
    }

    public long currentRaidTime() {
        return System.currentTimeMillis() - raidStartTime;
    }

    public long currentRoomTime() {
        return System.currentTimeMillis() - roomStartTime;
    }

    public long getIntermissionTime() {
        long currentTime = System.currentTimeMillis();
        long timeSpentInChallenges = 0;

        for (long time : roomTimers.values()) {
            timeSpentInChallenges += time;
        }

        long intermissionTime = currentTime - raidStartTime - timeSpentInChallenges;

        if (inChallengeRoom() || currentRoom == RaidRoomType.BOSS_FIGHT) {
            intermissionTime -= (currentTime - roomStartTime);
        }

        return intermissionTime;
    }

    private boolean inChallengeRoom() {
        return currentRoom == RaidRoomType.CHALLENGE_1
                || currentRoom == RaidRoomType.CHALLENGE_2
                || currentRoom == RaidRoomType.CHALLENGE_3;
    }

    private boolean inPowerupRoom() {
        return currentRoom == RaidRoomType.POWERUP_1
                || currentRoom == RaidRoomType.POWERUP_2
                || currentRoom == RaidRoomType.POWERUP_3;
    }

    private boolean inBossFight() {
        return currentRoom == RaidRoomType.BOSS_FIGHT;
    }

    private void updateRoom() {
        RaidRoomType previousRoom = currentRoom;

        if (currentRoom.ordinal() < RaidRoomType.BOSS_FIGHT.ordinal()) {
            currentRoom = RaidRoomType.values()[currentRoom.ordinal() + 1];
        }

        if (inChallengeRoom() || inBossFight()) {
            roomStartTime = System.currentTimeMillis();
        } else if (inPowerupRoom()) {
            long roomTime = System.currentTimeMillis() - roomStartTime;
            roomTimers.put(previousRoom, roomTime);
            roomStartTime = System.currentTimeMillis();
        }
    }

    private List<Long> getAllRoomTimes() {
        List<Long> allRoomTimes = new ArrayList<>();

        // Order is challenge 1, 2, 3, boss, intermission
        allRoomTimes.add(getRoomTime(RaidRoomType.CHALLENGE_1));
        allRoomTimes.add(getRoomTime(RaidRoomType.CHALLENGE_2));
        allRoomTimes.add(getRoomTime(RaidRoomType.CHALLENGE_3));
        allRoomTimes.add(getRoomTime(RaidRoomType.BOSS_FIGHT));
        // Need to add boss time to get correct intermission time since
        // currentRoom will still be boss room when getIntermissionTime() is called
        allRoomTimes.add(getIntermissionTime() + getRoomTime(RaidRoomType.BOSS_FIGHT));

        return allRoomTimes;
    }

    private void checkForNewPersonalBest(RaidKind raidKind, long time) {
        if (bestTimes.get().get(raidKind.getName()) == null) {
            bestTimes.get().put(raidKind.getName(), time);
            bestTimes.touched();
        } else {
            long currentBestTime = bestTimes.get().get(raidKind.getName());

            // New time is faster
            if (currentBestTime > time) {
                bestTimes.get().put(raidKind.getName(), time);
                bestTimes.touched();

                WynntilsMod.postEvent(new RaidNewBestTimeEvent(raidKind, time));
            }
        }
    }
}
