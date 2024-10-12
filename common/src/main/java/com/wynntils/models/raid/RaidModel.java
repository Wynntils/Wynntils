/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.models.damage.type.DamageDealtEvent;
import com.wynntils.models.raid.event.RaidChallengeEvent;
import com.wynntils.models.raid.event.RaidEndedEvent;
import com.wynntils.models.raid.event.RaidNewBestTimeEvent;
import com.wynntils.models.raid.scoreboard.RaidScoreboardPart;
import com.wynntils.models.raid.type.RaidKind;
import com.wynntils.models.raid.type.RaidRoomType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

public class RaidModel extends Model {
    public static final int MAX_CHALLENGES = 3;
    public static final int ROOM_DAMAGES_COUNT = 4;
    public static final int ROOM_TIMERS_COUNT = 5;
    private static final Pattern CHALLENGE_COMPLETED_PATTERN = Pattern.compile("\uDB00\uDC5F§a§lChallenge Completed");
    private static final Pattern RAID_COMPLETED_PATTERN = Pattern.compile("§f§lR§#4d4d4dff§laid Completed!");
    private static final Pattern RAID_FAILED_PATTERN = Pattern.compile("§4§kRa§c§lid Failed!");

    private static final Pattern RAID_CHOOSE_BUFF_PATTERN = Pattern.compile(
            "§#d6401eff(\\uE009\\uE002|\\uE001) §#fa7f63ff((§o)?(\\w+))§#d6401eff has chosen the §#fa7f63ff(\\w+ \\w+)§#d6401eff buff!");

    private static final RaidScoreboardPart RAID_SCOREBOARD_PART = new RaidScoreboardPart();

    @Persisted
    private final Storage<Map<String, Long>> bestTimes = new Storage<>(new TreeMap<>());

    private final Map<RaidRoomType, Long> roomTimers = new EnumMap<>(RaidRoomType.class);
    private final Map<RaidRoomType, Long> roomDamages = new EnumMap<>(RaidRoomType.class);

    private Map<String, List<String>> partyRaidBuffs = new HashMap<>();

    private boolean completedCurrentChallenge = false;
    private CappedValue challenges = CappedValue.EMPTY;
    private int timeLeft = 0;
    private long raidStartTime;
    private long roomStartTime;
    private long currentRoomDamage = 0;
    private RaidKind currentRaid = null;
    private RaidRoomType currentRoom = null;

    public RaidModel() {
        super(List.of());

        Handlers.Scoreboard.addPart(RAID_SCOREBOARD_PART);
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
                completedCurrentChallenge = false;
            }
        } else if (styledText.matches(RAID_COMPLETED_PATTERN)) {
            completeRaid();
        } else if (styledText.matches(RAID_FAILED_PATTERN)) {
            failedRaid();
        }
    }

    // One challenge in Nexus of Light does not display the scoreboard upon challenge completion
    // so we have to check for the chat message
    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent event) {
        if (inBuffRoom()) {
            Matcher matcher = event.getOriginalStyledText().stripAlignment().getMatcher(RAID_CHOOSE_BUFF_PATTERN);
            if (matcher.matches()) {
                String playerName = matcher.group(4);
                // if the player is nicknamed
                if (matcher.group(3) != null) {
                    playerName = StyledTextUtils.extractNameAndNick(event.getOriginalStyledText())
                            .key();
                    if (playerName == null) return;
                }

                String buff = matcher.group(5);

                partyRaidBuffs
                        .computeIfAbsent(playerName, k -> new ArrayList<>())
                        .add(buff);
            }
        }

        if (!inChallengeRoom()) return;

        if (event.getStyledText().matches(CHALLENGE_COMPLETED_PATTERN)) {
            completeChallenge();
        }
    }

    @SubscribeEvent
    public void onDamageDealtEvent(DamageDealtEvent event) {
        if (currentRaid == null) return;

        currentRoomDamage +=
                event.getDamages().values().stream().mapToLong(d -> d).sum();
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        // Only want to send the message once the user has returned to an actual world
        if (currentRaid != null && event.getNewState() == WorldState.WORLD) {
            currentRaid = null;
            currentRoom = null;
            completedCurrentChallenge = false;
            timeLeft = 0;
            challenges = CappedValue.EMPTY;
            roomTimers.clear();
            roomDamages.clear();
            partyRaidBuffs.clear();

            McUtils.sendMessageToClient(Component.literal(
                            "Raid tracking has been interrupted, you will not be able to see progress for the current raid")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }

    // This is called when the "Go to the exit" scoreboard line is displayed.
    // If we are in the final buff room when this is shown, then we are now in the boss intermission.
    // Otherwise, if we are in the intro or another buff room then we are now in an instructions room.
    public void tryEnterChallengeIntermission() {
        if (currentRoom == RaidRoomType.BUFF_3) {
            currentRoom = RaidRoomType.BOSS_INTERMISSION;
        } else if (inBuffRoom() || currentRoom == RaidRoomType.INTRO) {
            currentRoom = RaidRoomType.values()[currentRoom.ordinal() + 1];
        }
    }

    // Since all challenges use a different instructions message the easiest way to check for a challenge
    // beginning is from the scoreboard not showing any of the static messages and being in an instructions room.
    // At the time of making this there is no consistent way to check for entering a boss fight either, so if we
    // are in the boss intermission and this is called, then we have entered the boss fight.
    // So this method will be called when no other patterns matched the first line of the raid scoreboard segment.
    public void tryStartChallenge() {
        if (inInstructionsRoom()) {
            currentRoom = RaidRoomType.values()[currentRoom.ordinal() + 1];
            WynntilsMod.postEvent(new RaidChallengeEvent.Started(currentRaid, currentRoom));
            roomStartTime = System.currentTimeMillis();
        }
    }

    // This will only end the timer for the current room, but we are still in the room so currentRoom isn't updated.
    // It will be called multiple times after completing a challenge so we use completedCurrentChallenge to only
    // post the event and save timers once.
    public void completeChallenge() {
        if (!completedCurrentChallenge) {
            long roomTime = System.currentTimeMillis() - roomStartTime;
            roomTimers.put(currentRoom, roomTime);
            roomStartTime = System.currentTimeMillis();

            WynntilsMod.postEvent(new RaidChallengeEvent.Completed(currentRaid, currentRoom));

            completedCurrentChallenge = true;
        }
    }

    // Only check for entry to a buff room once after the challenge has been completed.
    public void enterBuffRoom() {
        if (completedCurrentChallenge) {
            // We put the damage dealt here instead of in completeChallenge as you are still in
            // the challenge room and can deal damage until you enter the buff room
            roomDamages.put(currentRoom, currentRoomDamage);
            currentRoomDamage = 0;

            currentRoom = RaidRoomType.values()[currentRoom.ordinal() + 1];

            completedCurrentChallenge = false;
        }
    }

    public void startBossFight() {
        if (currentRoom == RaidRoomType.BOSS_INTERMISSION) {
            currentRoom = RaidRoomType.BOSS_FIGHT;
            WynntilsMod.postEvent(new RaidChallengeEvent.Started(currentRaid, currentRoom));
            roomStartTime = System.currentTimeMillis();
        }
    }

    public void completeRaid() {
        if (currentRaid == null) return;

        // Add the boss time to room timers
        long bossTime = System.currentTimeMillis() - roomStartTime;
        roomTimers.put(RaidRoomType.BOSS_FIGHT, bossTime);

        WynntilsMod.postEvent(new RaidEndedEvent.Completed(
                currentRaid, getAllRoomTimes(), currentRaidTime(), getAllRoomDamages(), getRaidDamage()));

        // Need to add boss time to get correct intermission time since
        // currentRoom will still be boss room when getIntermissionTime() is called
        checkForNewPersonalBest(
                currentRaid, currentRaidTime() - (getIntermissionTime() + getRoomTime(RaidRoomType.BOSS_FIGHT)));

        currentRaid = null;
        currentRoom = null;
        completedCurrentChallenge = false;
        timeLeft = 0;
        currentRoomDamage = 0;
        challenges = CappedValue.EMPTY;
        roomTimers.clear();
        roomDamages.clear();
        partyRaidBuffs.clear();
    }

    public void failedRaid() {
        if (currentRaid == null) return;

        WynntilsMod.postEvent(new RaidEndedEvent.Failed(currentRaid, getAllRoomTimes(), currentRaidTime()));

        currentRaid = null;
        currentRoom = null;
        currentRoomDamage = 0;
        completedCurrentChallenge = false;
        timeLeft = 0;
        challenges = CappedValue.EMPTY;
        roomTimers.clear();
        roomDamages.clear();
        partyRaidBuffs.clear();
    }

    public void setTimeLeft(int seconds) {
        timeLeft = seconds;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setChallenges(CappedValue challenges) {
        this.challenges = challenges;
    }

    public CappedValue getChallenges() {
        return challenges;
    }

    public long getRaidBestTime(RaidKind raidKind) {
        return bestTimes.get().getOrDefault(raidKind.getName(), -1L);
    }

    public RaidKind getCurrentRaid() {
        return currentRaid;
    }

    public List<String> getRaidMajorIds(String playerName) {
        if (!partyRaidBuffs.containsKey(playerName)) return List.of();

        List<String> rawBuffNames = partyRaidBuffs.get(playerName);
        List<String> majorIds = new ArrayList<>();

        for (String rawBuffName : rawBuffNames) {
            String[] buffParts = rawBuffName.split(" ");
            if (buffParts.length < 2) continue;

            String buffName = buffParts[0];
            int buffTier = MathUtils.integerFromRoman(buffParts[1]);

            String majorId = this.currentRaid.majorIdFromBuff(buffName, buffTier);
            if (majorId == null) continue;

            majorIds.add(majorId);
        }

        return majorIds;
    }

    public RaidRoomType getCurrentRoom() {
        return currentRoom;
    }

    public long getRoomTime(RaidRoomType roomType) {
        if (roomType == currentRoom && !completedCurrentChallenge) {
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

        if ((inChallengeRoom() && !completedCurrentChallenge) || currentRoom == RaidRoomType.BOSS_FIGHT) {
            intermissionTime -= (currentTime - roomStartTime);
        }

        return intermissionTime;
    }

    public long getRaidDamage() {
        return currentRoomDamage
                + roomDamages.values().stream().mapToLong(d -> d).sum();
    }

    public long getRoomDamage(RaidRoomType roomType) {
        if (roomType == currentRoom) {
            return getCurrentRoomDamage();
        }

        return roomDamages.getOrDefault(roomType, -1L);
    }

    public long getCurrentRoomDamage() {
        return currentRoomDamage;
    }

    private boolean inInstructionsRoom() {
        return currentRoom == RaidRoomType.INSTRUCTIONS_1
                || currentRoom == RaidRoomType.INSTRUCTIONS_2
                || currentRoom == RaidRoomType.INSTRUCTIONS_3;
    }

    private boolean inChallengeRoom() {
        return currentRoom == RaidRoomType.CHALLENGE_1
                || currentRoom == RaidRoomType.CHALLENGE_2
                || currentRoom == RaidRoomType.CHALLENGE_3;
    }

    private boolean inBuffRoom() {
        return currentRoom == RaidRoomType.BUFF_1
                || currentRoom == RaidRoomType.BUFF_2
                || currentRoom == RaidRoomType.BUFF_3;
    }

    private boolean inBossFight() {
        return currentRoom == RaidRoomType.BOSS_FIGHT;
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

    private List<Long> getAllRoomDamages() {
        List<Long> allRoomDamages = new ArrayList<>();

        allRoomDamages.add(getRoomDamage(RaidRoomType.CHALLENGE_1));
        allRoomDamages.add(getRoomDamage(RaidRoomType.CHALLENGE_2));
        allRoomDamages.add(getRoomDamage(RaidRoomType.CHALLENGE_3));
        allRoomDamages.add(getRoomDamage(RaidRoomType.BOSS_FIGHT));

        return allRoomDamages;
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
