/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.models.combat.type.DamageDealtEvent;
import com.wynntils.models.containers.containers.RaidRewardChestContainer;
import com.wynntils.models.containers.event.MythicFoundEvent;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.AspectItem;
import com.wynntils.models.items.items.game.EmeraldItem;
import com.wynntils.models.items.items.game.TomeItem;
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
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;

public class RaidModel extends Model {
    public static final int MAX_CHALLENGES = 3;
    public static final int ROOM_DAMAGES_COUNT = 4;
    public static final int ROOM_TIMERS_COUNT = 5;
    private static final Pattern CHALLENGE_COMPLETED_PATTERN = Pattern.compile("\uDB00\uDC5F§a§lChallenge Completed");
    private static final Pattern RAID_COMPLETED_PATTERN = Pattern.compile("§f§lR§#4d4d4dff§laid Completed!");
    private static final Pattern RAID_FAILED_PATTERN = Pattern.compile("§4§kRa§c§lid Failed!");

    private static final Pattern REWARD_PULLS_PATTERN = Pattern.compile("§.(\\d+)§7 Reward Pulls");
    private static final Pattern ASPECT_PULLS_PATTERN = Pattern.compile("§.(\\d+)§7 Aspect Pulls");

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

    @Persisted
    private final Storage<Integer> numRaidsWithoutMythicAspect = new Storage<>(0);

    @Persisted
    private final Storage<Integer> numAspectPullsWithoutMythicAspect = new Storage<>(0);

    @Persisted
    private final Storage<Integer> numRaidsWithoutMythicTome = new Storage<>(0);

    @Persisted
    private final Storage<Integer> numRewardPullsWithoutMythicTome = new Storage<>(0);

    public static final int RAID_REWARD_CHEST_ASPECT_SLOTS_START = 11;
    public static final int RAID_REWARD_CHEST_ASPECT_SLOTS_END = 15;
    public static final int RAID_REWARD_CHEST_REWARD_SLOTS_START = 27;
    public static final int RAID_REWARD_CHEST_REWARD_SLOTS_END = 53;

    private int expectedNumAspectPulls = -1;
    private int expectedNumRewardPulls = -1;
    private int foundNumRewardPulls;
    private boolean foundMythicTome;
    private boolean foundMythicAspect;
    private boolean rewardChestIsOpened = false;

    private boolean hasProcessedRewards = true;
    private int expectedRaidRewardChestId = -2;

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

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent event) {
        StyledText styledText = event.getOriginalStyledText();

        Matcher rewardPullMatcher = styledText.getMatcher(REWARD_PULLS_PATTERN);
        if (rewardPullMatcher.find()) {
            expectedNumRewardPulls = Integer.parseInt(rewardPullMatcher.group(1));
            hasProcessedRewards = false;
            return;
        }
        Matcher aspectPullMatcher = styledText.getMatcher(ASPECT_PULLS_PATTERN);
        if (aspectPullMatcher.find()) {
            expectedNumAspectPulls = Integer.parseInt(aspectPullMatcher.group(1));
            return;
        }

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

        // One challenge in Nexus of Light does not display the scoreboard upon challenge completion so we have to check
        // for the chat message
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

    // Process raid rewards
    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent.Pre e) {
        if (Models.Container.getCurrentContainer() instanceof RaidRewardChestContainer raidRewardChest) {
            expectedRaidRewardChestId = raidRewardChest.getContainerId();
        } else {
            expectedRaidRewardChestId = -2;
        }
    }

    @SubscribeEvent
    public void onSetContent(ContainerSetContentEvent.Post event) {
        if (event.getContainerId() != expectedRaidRewardChestId) return;
        rewardChestIsOpened = true;

        if (hasProcessedRewards) {
            // Note: Logic here has been intentionally skipped. Only the first page of the rewarded aspects are parsed
            // and relevant as of right now (as it is extraordinarily unlikely that a user gets more mythic aspects
            // than the first page can hold)
            return;
        }
        hasProcessedRewards = true;
        if (expectedNumAspectPulls == -1 || expectedNumRewardPulls == -1) {
            WynntilsMod.warn(
                    "[RaidModel] Set content of raid reward chest, but did not detect number of expected pulls. Got expectedNumAspectPulls="
                            + expectedNumAspectPulls + " and expectedNumRewardPulls=" + expectedNumRewardPulls
                            + ". Probably, the player tried closing the chest before, which got cancelled and the contents of the chest got refreshed. Ignoring contents of this raid chest.");
            return;
        }

        List<ItemStack> items = event.getItems();

        // Aspects only appear in slots 11..15 and item rewards only appear in slots 27..53
        foundMythicAspect = false;
        for (int i = RAID_REWARD_CHEST_ASPECT_SLOTS_START; i <= RAID_REWARD_CHEST_ASPECT_SLOTS_END; i++) {
            processAspectItemFind(items.get(i), i);
        }

        foundMythicTome = false;
        foundNumRewardPulls = 0;
        for (int i = RAID_REWARD_CHEST_REWARD_SLOTS_START; i <= RAID_REWARD_CHEST_REWARD_SLOTS_END; i++) {
            processRewardItemFind(items.get(i), i);
        }
    }

    @SubscribeEvent
    public void onContainerClose(ContainerCloseEvent.Post event) {
        if (!rewardChestIsOpened) return;
        expectedRaidRewardChestId = -2; // Reset to null

        if (expectedNumRewardPulls == -1 || expectedNumAspectPulls == -1) {
            WynntilsMod.warn(
                    "[RaidModel] Failed to update dry raid counts after closing the reward chest. Did not detect number of expected pulls. Got expectedNumAspectPulls="
                            + expectedNumAspectPulls + " and expectedNumRewardPulls=" + expectedNumRewardPulls
                            + ". Probably, the player tried closing the chest before, which got cancelled and the contents of the chest got refreshed.");
            return;
        }

        if (foundMythicAspect) {
            numRaidsWithoutMythicAspect.store(0);
            numAspectPullsWithoutMythicAspect.store(0);
        } else {
            numRaidsWithoutMythicAspect.store(numRaidsWithoutMythicAspect.get() + 1);
            numAspectPullsWithoutMythicAspect.store(numAspectPullsWithoutMythicAspect.get() + expectedNumAspectPulls);
        }

        if (expectedNumRewardPulls <= (RAID_REWARD_CHEST_REWARD_SLOTS_END - RAID_REWARD_CHEST_REWARD_SLOTS_START + 1)
                && foundNumRewardPulls != expectedNumRewardPulls) {
            WynntilsMod.warn("[RaidModel] Expected user to receive " + expectedNumRewardPulls
                    + " pulls based on raid summary in chat. However, detected "
                    + foundNumRewardPulls + " items in reward chest. Awarding "
                    + expectedNumRewardPulls + " pulls based on raid summary chat message.");
        }

        if (foundMythicTome) {
            numRaidsWithoutMythicTome.store(0);
            numRewardPullsWithoutMythicTome.store(0);
        } else {
            numRaidsWithoutMythicTome.store(numRaidsWithoutMythicTome.get() + 1);
            numRewardPullsWithoutMythicTome.store(numRewardPullsWithoutMythicTome.get() + expectedNumRewardPulls);
        }

        expectedNumAspectPulls = -1;
        expectedNumRewardPulls = -1;
        rewardChestIsOpened = false;
    }

    private void processAspectItemFind(ItemStack itemStack, int slotId) {
        if (itemStack.getItem() == Items.AIR) return;

        Optional<AspectItem> aspectOptional = Models.Item.asWynnItem(itemStack, AspectItem.class);
        if (aspectOptional.isPresent()) {
            AspectItem aspectItem = aspectOptional.get();
            if (aspectItem.getGearTier() == GearTier.MYTHIC) {
                foundMythicAspect = true;
                WynntilsMod.postEvent(new MythicFoundEvent(itemStack, MythicFoundEvent.MythicSource.RAID_REWARD_CHEST));
            }
            return;
        }

        WynntilsMod.warn("[RaidModel] Unexpectedly found item \""
                + StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting() + "\" at slot "
                + slotId + ", but this slot should be an aspect reward slot.");
    }

    private void processRewardItemFind(ItemStack itemStack, int slotId) {
        if (itemStack.getItem() == Items.AIR) return;

        foundNumRewardPulls += 1;

        Optional<AspectItem> aspectOptional = Models.Item.asWynnItem(itemStack, AspectItem.class);
        if (aspectOptional.isPresent()) {
            AspectItem aspectItem = aspectOptional.get();
            WynntilsMod.warn("[RaidModel] User found aspect item \"" + aspectItem.toString() + "\" at slot " + slotId
                    + ", but this slot should be a reward item slot.");
            return;
        }

        Optional<EmeraldItem> emeraldOptional = Models.Item.asWynnItem(itemStack, EmeraldItem.class);
        if (emeraldOptional.isPresent()) {
            // Can track the number of emeralds the player receives for completing the raid
            return;
        }

        Optional<TomeItem> tomeItemOptional = Models.Item.asWynnItemProperty(itemStack, TomeItem.class);
        if (tomeItemOptional.isPresent()) {
            TomeItem tomeItem = tomeItemOptional.get();
            if (tomeItem.getGearTier() == GearTier.MYTHIC) {
                foundMythicTome = true;
                WynntilsMod.postEvent(new MythicFoundEvent(itemStack, MythicFoundEvent.MythicSource.RAID_REWARD_CHEST));
            }
            return;
        }
    }

    // This is called when the "Go to the exit" scoreboard line is displayed.
    // If we are in the final buff room when this is shown, then we are now in the
    // boss intermission.
    // Otherwise, if we are in the intro or another buff room then we are now in an
    // instructions room.
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

    public int getRaidsWithoutMythicAspect() {
        return numRaidsWithoutMythicAspect.get();
    }

    public int getAspectPullsWithoutMythicAspect() {
        return numAspectPullsWithoutMythicAspect.get();
    }

    public int getRaidsWithoutMythicTome() {
        return numRaidsWithoutMythicTome.get();
    }

    public int getRewardPullsWithoutMythicTome() {
        return numRewardPullsWithoutMythicTome.get();
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
