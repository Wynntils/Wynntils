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
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.models.raid.event.RaidBossStartedEvent;
import com.wynntils.models.raid.event.RaidChallengeEvent;
import com.wynntils.models.raid.event.RaidFinishedEvent;
import com.wynntils.models.raid.event.RaidNewBestTimeEvent;
import com.wynntils.models.raid.type.Raid;
import com.wynntils.models.raid.type.RaidRoomType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RaidModel extends Model {
    // For raid entry use title.
    // For start of a challenge, look for an instructions label.
    // For end of a challenge look for BLACKSMITH_LABEL label.
    // For boss intermission look for BOSS_FIGHT_LABEL label.
    // For boss fight look for the boss' label.
    // For raid completion/failure use RAID_COMPLETE or RAID_FAILED title.
    private static final StyledText BLACKSMITH_LABEL = StyledText.fromString("§dBlacksmith");
    private static final StyledText BOSS_FIGHT_LABEL = StyledText.fromString("§4§l[§cBoss Fight§4§l]");
    // Title gradually builds up to full "§a§lRAID COMPLETED!"
    private static final StyledText RAID_COMPLETE = StyledText.fromString("§a§lD C");
    private static final StyledText RAID_FAILED = StyledText.fromString("§4Raid Failed!");

    @Persisted
    public final Storage<Map<String, Integer>> bestTimes = new Storage<>(new TreeMap<>());

    private final Map<RaidRoomType, Integer> roomTimers = new EnumMap<>(RaidRoomType.class);

    private long raidStartTime;
    private long roomStartTime;
    private Raid currentRaid = null;
    private RaidRoomType currentRoom = null;

    public RaidModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onTitle(TitleSetTextEvent event) {
        Component component = event.getComponent();
        StyledText styledText = StyledText.fromComponent(component);

        if (currentRaid == null) {
            currentRaid = Raid.fromTitle(styledText);

            if (currentRaid != null) {
                // In a raid, set to intro room and start timer
                currentRoom = RaidRoomType.INTRO;
                raidStartTime = System.currentTimeMillis();
            }
        } else {
            if (styledText.equals(RAID_COMPLETE)) {
                int timeTaken = (int) ((System.currentTimeMillis() - raidStartTime) / 1000);
                // Raid has been completed, post event with time taken in seconds
                WynntilsMod.postEvent(new RaidFinishedEvent.Completed(currentRaid, timeTaken));

                checkForNewPersonalBest(currentRaid, timeTaken);
            } else if (styledText.equals(RAID_FAILED)) {
                // Raid failed, post event with time elapsed in seconds
                WynntilsMod.postEvent(new RaidFinishedEvent.Failed(
                        currentRaid, (int) ((System.currentTimeMillis() - raidStartTime) / 1000)));
            } else {
                return;
            }

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
    public void onWorldStateChange(WorldStateEvent event) {
        currentRaid = null;
        currentRoom = null;
        roomTimers.clear();
    }

    public int getRaidBestTime(Raid raid) {
        return bestTimes.get().getOrDefault(raid.getName(), -1);
    }

    public Raid getCurrentRaid() {
        return currentRaid;
    }

    public RaidRoomType getCurrentRoom() {
        return currentRoom;
    }

    public int getRoomTime(RaidRoomType roomType) {
        if (roomType == currentRoom) {
            return currentRoomTime();
        }

        return roomTimers.getOrDefault(roomType, -1);
    }

    public int currentRaidTime() {
        return (int) (System.currentTimeMillis() - raidStartTime) / 1000;
    }

    public int currentRoomTime() {
        return (int) (System.currentTimeMillis() - roomStartTime) / 1000;
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
            int roomTime = (int) (System.currentTimeMillis() - roomStartTime) / 1000;
            roomTimers.put(previousRoom, roomTime);
            roomStartTime = System.currentTimeMillis();
        }
    }

    private void checkForNewPersonalBest(Raid raid, int time) {
        if (bestTimes.get().get(raid.getName()) == null) {
            bestTimes.get().put(raid.getName(), time);
            bestTimes.touched();
        } else {
            int currentBestTime = bestTimes.get().get(raid.getName());

            // New time is faster
            if (currentBestTime > time) {
                bestTimes.get().put(raid.getName(), time);
                bestTimes.touched();

                WynntilsMod.postEvent(new RaidNewBestTimeEvent(raid, time));
            }
        }
    }
}
