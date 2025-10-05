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
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.models.combat.type.DamageDealtEvent;
import com.wynntils.models.containers.containers.RaidRewardChestContainer;
import com.wynntils.models.containers.event.ValuableFoundEvent;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.AspectItem;
import com.wynntils.models.items.items.game.EmeraldItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.raid.bossbar.ParasiteOvertakenBar;
import com.wynntils.models.raid.event.RaidChallengeEvent;
import com.wynntils.models.raid.event.RaidEndedEvent;
import com.wynntils.models.raid.event.RaidNewBestTimeEvent;
import com.wynntils.models.raid.event.RaidStartedEvent;
import com.wynntils.models.raid.raids.NestOfTheGrootslangsRaid;
import com.wynntils.models.raid.raids.OrphionsNexusOfLightRaid;
import com.wynntils.models.raid.raids.RaidKind;
import com.wynntils.models.raid.raids.TheCanyonColossusRaid;
import com.wynntils.models.raid.raids.TheNamelessAnomalyRaid;
import com.wynntils.models.raid.scoreboard.RaidScoreboardPart;
import com.wynntils.models.raid.type.HistoricRaidInfo;
import com.wynntils.models.raid.type.RaidInfo;
import com.wynntils.models.raid.type.RaidRoomInfo;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
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
import net.neoforged.bus.api.SubscribeEvent;

public final class RaidModel extends Model {
    // These should be set to whatever the highest amount in any raid is.
    public static final Integer MAXIMUM_CHALLENGE_ROOMS = 3;
    public static final Integer MAXIMUM_BOSS_ROOMS = 2;

    private static final Pattern CHALLENGE_COMPLETED_PATTERN = Pattern.compile("\uDB00\uDC5E§a§lChallenge Completed");
    private static final Pattern RAID_COMPLETED_PATTERN = Pattern.compile("§f§lR§#4d4d4dff§laid Completed!");
    private static final Pattern RAID_FAILED_PATTERN = Pattern.compile("§4§kRa§c§lid Failed!");

    private static final int RAID_REWARD_CHEST_ASPECT_SLOTS_START = 11;
    private static final int RAID_REWARD_CHEST_ASPECT_SLOTS_END = 15;
    private static final int RAID_REWARD_CHEST_REWARD_SLOTS_START = 27;
    private static final int RAID_REWARD_CHEST_REWARD_SLOTS_END = 53;
    private static final Pattern REWARD_PULLS_PATTERN = Pattern.compile("§.(\\d+)§7 Reward Pulls");
    private static final Pattern ASPECT_PULLS_PATTERN = Pattern.compile("§.(\\d+)§7 Aspect Pulls");

    private static final Pattern RAID_CHOOSE_BUFF_PATTERN = Pattern.compile(
            "§#d6401eff(\\uE009\\uE002|\\uE001) §#fa7f63ff((§o)?(\\w+))§#d6401eff has chosen the §#fa7f63ff(\\w+ \\w+)§#d6401eff buff!");

    private static final ParasiteOvertakenBar PARASITE_OVERTAKEN_BAR = new ParasiteOvertakenBar();
    private static final Pattern PARASITE_OVERTAKEN_PATTERN = Pattern.compile(
            "§#d6401eff(?:\uE009\uE002|\uE001) §#fa7f63ff(?<player>.+?)§#d6401eff has been overtaken! Keep attacking §#ffc85fffThe Parasite§#d6401eff to save them!");

    @Persisted
    private final Storage<Map<String, Long>> bestTimes = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Integer> numRaidsWithoutMythicAspect = new Storage<>(0);

    @Persisted
    private final Storage<Integer> numAspectPullsWithoutMythicAspect = new Storage<>(0);

    @Persisted
    private final Storage<Integer> numRaidsWithoutMythicTome = new Storage<>(0);

    @Persisted
    private final Storage<Integer> numRewardPullsWithoutMythicTome = new Storage<>(0);

    @Persisted
    private final Storage<Integer> expectedNumAspectPulls = new Storage<>(-1);

    @Persisted
    private final Storage<Integer> expectedNumRewardPulls = new Storage<>(-1);

    @Persisted
    public final Storage<List<HistoricRaidInfo>> historicRaids = new Storage<>(new ArrayList<>());

    private static final List<RaidKind> RAIDS = new ArrayList<>();
    private static final RaidScoreboardPart RAID_SCOREBOARD_PART = new RaidScoreboardPart();

    private final Map<String, List<String>> partyRaidBuffs = new HashMap<>();

    private int foundNumRewardPulls;
    private int expectedRaidRewardChestId = -2;
    private boolean foundMythicTome;
    private boolean foundMythicAspect;
    private boolean rewardChestIsOpened = false;
    private boolean hasProcessedRewards = true;
    private boolean rerollingRewards = false;

    private boolean completedCurrentChallenge = false;
    private boolean inBuffRoom = false;
    private boolean inIntermissionRoom = false;
    private boolean parasiteOvertaken = false;
    private CappedValue challenges = CappedValue.EMPTY;
    private int timeLeft = 0;
    private RaidInfo currentRaid;

    public RaidModel() {
        super(List.of());

        Handlers.BossBar.registerBar(PARASITE_OVERTAKEN_BAR);
        Handlers.Scoreboard.addPart(RAID_SCOREBOARD_PART);

        registerRaids();
    }

    @SubscribeEvent
    public void onTitle(TitleSetTextEvent event) {
        Component component = event.getComponent();
        StyledText styledText = StyledText.fromComponent(component);

        if (currentRaid == null) {
            RaidKind raidKind = getRaidFromTitle(styledText);

            if (raidKind != null) {
                currentRaid = new RaidInfo(raidKind);
                completedCurrentChallenge = false;
                parasiteOvertaken = false;

                WynntilsMod.postEvent(new RaidStartedEvent(raidKind));
            }
        } else if (styledText.matches(RAID_COMPLETED_PATTERN)) {
            completeRaid();
        } else if (styledText.matches(RAID_FAILED_PATTERN)) {
            failedRaid();
        }
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent.Match event) {
        StyledText styledText = event.getMessage();

        Matcher rewardPullMatcher = styledText.getMatcher(REWARD_PULLS_PATTERN);
        if (rewardPullMatcher.find()) {
            expectedNumRewardPulls.store(Integer.parseInt(rewardPullMatcher.group(1)));
            hasProcessedRewards = false;
            return;
        }

        Matcher aspectPullMatcher = styledText.getMatcher(ASPECT_PULLS_PATTERN);
        if (aspectPullMatcher.find()) {
            expectedNumAspectPulls.store(Integer.parseInt(aspectPullMatcher.group(1)));
            return;
        }

        StyledText unwrapped = StyledTextUtils.unwrap(styledText).stripAlignment();

        if (inBuffRoom) {
            Matcher matcher = unwrapped.getMatcher(RAID_CHOOSE_BUFF_PATTERN);
            if (matcher.matches()) {
                String playerName = matcher.group(4);
                // if the player is nicknamed
                if (matcher.group(3) != null) {
                    playerName = StyledTextUtils.extractNameAndNick(event.getMessage())
                            .key();
                    if (playerName == null) return;
                }

                String buff = matcher.group(5);

                partyRaidBuffs
                        .computeIfAbsent(playerName, k -> new ArrayList<>())
                        .add(buff);
            }

            return;
        }

        Matcher matcher = unwrapped.getMatcher(PARASITE_OVERTAKEN_PATTERN);
        if (matcher.matches()) {
            parasiteOvertaken = matcher.group("player").equals(McUtils.playerName());
            return;
        }

        if (inIntermissionRoom) return;

        // One challenge in Nexus of Light does not display the scoreboard upon challenge completion so we have to check
        // for the chat message
        if (styledText.matches(CHALLENGE_COMPLETED_PATTERN)) {
            completeChallenge();
        }
    }

    @SubscribeEvent
    public void onDamageDealtEvent(DamageDealtEvent event) {
        if (currentRaid == null) return;
        if (inIntermissionRoom || inBuffRoom) return;

        currentRaid.addDamageToCurrentRoom(
                event.getDamages().values().stream().mapToLong(d -> d).sum());
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        // Only want to send the message once the user has returned to an actual world
        if (currentRaid != null && event.getNewState() == WorldState.WORLD) {
            currentRaid = null;
            completedCurrentChallenge = false;
            timeLeft = 0;
            challenges = CappedValue.EMPTY;
            partyRaidBuffs.clear();
            parasiteOvertaken = false;

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
    public void onSlotClicked(ContainerClickEvent e) {
        if (e.getItemStack().isEmpty()) return;

        if (Models.Container.getCurrentContainer() instanceof RaidRewardChestContainer raidRewardChest) {
            if (e.getSlotNum() == raidRewardChest.REROLL_REWARDS_SLOT) {
                StyledText rerollLoreConfirm =
                        LoreUtils.getLore(e.getItemStack()).getFirst();

                if (rerollLoreConfirm.matches(raidRewardChest.REROLL_CONFIRM_PATTERN)) {
                    rerollingRewards = true;
                    hasProcessedRewards = false;
                }
            }
        }
    }

    @SubscribeEvent
    public void onSetContent(ContainerSetContentEvent.Post event) {
        if (event.getContainerId() != expectedRaidRewardChestId) return;
        ItemStack rerollItem = event.getItems().get(RaidRewardChestContainer.REROLL_REWARDS_SLOT);
        if (!rerollItem.isEmpty()) {
            StyledText rerollLoreConfirm = LoreUtils.getLore(rerollItem).getFirst();
            if (rerollLoreConfirm.matches(RaidRewardChestContainer.REROLL_CONFIRM_PATTERN)) return;
        }
        rewardChestIsOpened = true;

        if (hasProcessedRewards) {
            // Note: Logic here has been intentionally skipped. Only the first page of the rewarded aspects are parsed
            // and relevant as of right now (as it is extraordinarily unlikely that a user gets more mythic aspects
            // than the first page can hold)
            return;
        }
        hasProcessedRewards = true;
        if (expectedNumAspectPulls.get() == -1 || expectedNumRewardPulls.get() == -1) {
            WynntilsMod.warn(
                    "[RaidModel] Set content of raid reward chest, but did not detect number of expected pulls. Got expectedNumAspectPulls="
                            + expectedNumAspectPulls.get() + " and expectedNumRewardPulls="
                            + expectedNumRewardPulls.get()
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
        // This is when the user closes the chest after claiming rewards
        expectedRaidRewardChestId = -2; // Reset to null

        if (expectedNumRewardPulls.get() == -1 || expectedNumAspectPulls.get() == -1) {
            WynntilsMod.warn(
                    "[RaidModel] Failed to update dry raid counts after closing the reward chest. Did not detect number of expected pulls. Got expectedNumAspectPulls="
                            + expectedNumAspectPulls.get() + " and expectedNumRewardPulls="
                            + expectedNumRewardPulls.get()
                            + ". Probably, the player tried closing the chest before, which got cancelled and the contents of the chest got refreshed.");
            return;
        }

        if (foundMythicAspect) {
            numRaidsWithoutMythicAspect.store(0);
            numAspectPullsWithoutMythicAspect.store(0);
        } else {
            numRaidsWithoutMythicAspect.store(numRaidsWithoutMythicAspect.get() + 1);
            numAspectPullsWithoutMythicAspect.store(
                    numAspectPullsWithoutMythicAspect.get() + expectedNumAspectPulls.get());
        }

        if (expectedNumRewardPulls.get()
                        <= (RAID_REWARD_CHEST_REWARD_SLOTS_END - RAID_REWARD_CHEST_REWARD_SLOTS_START + 1)
                && foundNumRewardPulls != expectedNumRewardPulls.get()) {
            WynntilsMod.warn("[RaidModel] Expected user to receive " + expectedNumRewardPulls.get()
                    + " pulls based on raid summary in chat. However, detected "
                    + foundNumRewardPulls + " items in reward chest. Awarding "
                    + expectedNumRewardPulls.get() + " pulls based on raid summary chat message.");
        }

        if (foundMythicTome) {
            numRaidsWithoutMythicTome.store(0);
            numRewardPullsWithoutMythicTome.store(0);
        } else {
            numRaidsWithoutMythicTome.store(numRaidsWithoutMythicTome.get() + 1);
            numRewardPullsWithoutMythicTome.store(numRewardPullsWithoutMythicTome.get() + expectedNumRewardPulls.get());
        }

        expectedNumAspectPulls.store(-1);
        expectedNumRewardPulls.store(-1);

        rewardChestIsOpened = false;
    }

    @SubscribeEvent
    public void onMenuClosed(MenuEvent.MenuClosedEvent event) {
        if (!rewardChestIsOpened) return;
        if (!rerollingRewards) return;
        // This is when the server closes the chest to reroll the chest

        if (expectedNumRewardPulls.get() == -1 || expectedNumAspectPulls.get() == -1) {
            WynntilsMod.warn(
                    "[RaidModel] Failed to update dry raid counts after rerolling the reward chest. Did not detect number of expected pulls. Got expectedNumAspectPulls="
                            + expectedNumAspectPulls.get() + " and expectedNumRewardPulls="
                            + expectedNumRewardPulls.get()
                            + ".");
            return;
        }

        if (foundMythicAspect) {
            numRaidsWithoutMythicAspect.store(0);
            numAspectPullsWithoutMythicAspect.store(expectedNumAspectPulls.get());
        } else {
            numAspectPullsWithoutMythicAspect.store(
                    numAspectPullsWithoutMythicAspect.get() + expectedNumAspectPulls.get());
        }

        if (expectedNumRewardPulls.get()
                        <= (RAID_REWARD_CHEST_REWARD_SLOTS_END - RAID_REWARD_CHEST_REWARD_SLOTS_START + 1)
                && foundNumRewardPulls != expectedNumRewardPulls.get()) {
            WynntilsMod.warn("[RaidModel] Expected user to receive " + expectedNumRewardPulls.get()
                    + " pulls based on raid summary in chat. However, detected "
                    + foundNumRewardPulls + " items in reward chest. Awarding "
                    + expectedNumRewardPulls.get() + " pulls based on raid summary chat message.");
        }

        if (foundMythicTome) {
            numRaidsWithoutMythicTome.store(0);
            numRewardPullsWithoutMythicTome.store(expectedNumRewardPulls.get());
        } else {
            numRewardPullsWithoutMythicTome.store(numRewardPullsWithoutMythicTome.get() + expectedNumRewardPulls.get());
        }

        rewardChestIsOpened = false;
        rerollingRewards = false;
    }

    public void tryEnterChallengeIntermission() {
        if (currentRaid == null) return;
        if (inIntermissionRoom) return;

        inBuffRoom = false;
        inIntermissionRoom = true;
    }

    public void tryStartChallenge(StyledText challengeLine) {
        if (currentRaid == null) return;

        int challengeNum = currentRaid.completedChallengeCount() + 1;

        String roomName =
                currentRaid.getRaidKind().getChallengeName(challengeNum, challengeLine.getStringWithoutFormatting());

        if (roomName.isEmpty()) return;

        inIntermissionRoom = false;
        currentRaid.startChallenge(challengeNum, roomName);

        WynntilsMod.postEvent(new RaidChallengeEvent.Started(currentRaid));
    }

    public void completeChallenge() {
        // We need to check we are in a raid here as this uses both the scoreboard and chat message to trigger
        // and lootruns use the same chat message
        if (currentRaid == null) return;

        if (!completedCurrentChallenge) {
            currentRaid.completeCurrentChallenge();

            completedCurrentChallenge = true;

            WynntilsMod.postEvent(new RaidChallengeEvent.Completed(currentRaid));
        }
    }

    // Only check for entry to a buff room once after the challenge has been completed.
    public void enterBuffRoom() {
        if (currentRaid == null) return;
        if (inBuffRoom) return;

        if (completedCurrentChallenge) {
            completedCurrentChallenge = false;

            inIntermissionRoom = false;
            inBuffRoom = true;
        }
    }

    public void failedRaid() {
        if (currentRaid == null) return;

        WynntilsMod.postEvent(new RaidEndedEvent.Failed(currentRaid));
        historicRaids
                .get()
                .add(new HistoricRaidInfo(
                        currentRaid.getRaidKind().getRaidName(),
                        currentRaid.getRaidKind().getAbbreviation(),
                        currentRaid.getChallenges(),
                        System.currentTimeMillis()));
        historicRaids.touched();

        currentRaid = null;
        completedCurrentChallenge = false;
        timeLeft = 0;
        challenges = CappedValue.EMPTY;
        partyRaidBuffs.clear();
        parasiteOvertaken = false;
    }

    public boolean isParasiteOvertaken() {
        return parasiteOvertaken;
    }

    public void resetParasiteOvertaken() {
        parasiteOvertaken = false;
    }

    public RaidInfo getCurrentRaid() {
        return currentRaid;
    }

    public List<String> getRaidMajorIds(String playerName) {
        if (this.currentRaid == null) return List.of();
        if (!partyRaidBuffs.containsKey(playerName)) return List.of();

        List<String> rawBuffNames = partyRaidBuffs.get(playerName);
        List<String> majorIds = new ArrayList<>();

        for (String rawBuffName : rawBuffNames) {
            String[] buffParts = rawBuffName.split(" ");
            if (buffParts.length < 2) continue;

            String buffName = buffParts[0];
            int buffTier = MathUtils.integerFromRoman(buffParts[1]);

            String majorId = this.currentRaid.getRaidKind().majorIdFromBuff(buffName, buffTier);
            if (majorId == null) continue;

            majorIds.add(majorId);
        }

        return majorIds;
    }

    public RaidKind getRaidFromColor(CustomColor color) {
        return RAIDS.stream()
                .filter(raid -> raid.getRaidColor().equals(color))
                .findFirst()
                .orElse(null);
    }

    public String getCurrentRoomName() {
        if (currentRaid == null || inIntermissionRoom || inBuffRoom) return "";

        RaidRoomInfo currentRoom = currentRaid.getCurrentRoom();

        if (currentRoom == null) return "";

        return currentRoom.getRoomName();
    }

    public String getRoomName(int roomNum) {
        if (currentRaid == null) return "";

        if (isBossRoom(roomNum)) {
            return currentRaid.getRaidKind().getBossName(roomNum);
        } else {
            RaidRoomInfo room = currentRaid.getRoomByNumber(roomNum);

            if (room == null) return "";

            return room.getRoomName();
        }
    }

    public long currentRaidTime() {
        if (currentRaid == null) return -1L;

        return currentRaid.getTimeInRaid();
    }

    public long getIntermissionTime() {
        if (currentRaid == null) return -1L;

        return currentRaid.getIntermissionTime();
    }

    public long currentRoomTime() {
        if (currentRaid == null || currentRaid.getCurrentRoom() == null) return -1L;

        return currentRaid.getCurrentRoom().getRoomTotalTime();
    }

    public long getRoomTime(int roomNum) {
        if (currentRaid == null) return -1L;

        RaidRoomInfo roomInfo = currentRaid.getRoomByNumber(roomNum);
        if (roomInfo == null) return -1L;

        return roomInfo.getRoomTotalTime();
    }

    public long getRaidDamage() {
        if (currentRaid == null) return -1L;

        return currentRaid.getRaidDamage();
    }

    public long getCurrentRoomDamage() {
        if (currentRaid == null || currentRaid.getCurrentRoom() == null) return -1L;

        return currentRaid.getCurrentRoom().getRoomDamage();
    }

    public long getRoomDamage(int roomNum) {
        if (currentRaid == null) return -1L;

        RaidRoomInfo roomInfo = currentRaid.getRoomByNumber(roomNum);
        if (roomInfo == null) return -1L;

        return roomInfo.getRoomDamage();
    }

    public int getRaidChallengeCount() {
        if (currentRaid == null) return -1;

        return currentRaid.getRaidKind().getChallengeCount();
    }

    public boolean raidHasRoom(int roomNum) {
        if (currentRaid == null || roomNum < 1) return false;

        return roomNum <= currentRaid.getRaidKind().getChallengeCount();
    }

    public int getRaidBossCount() {
        if (currentRaid == null) return -1;

        return currentRaid.getRaidKind().getBossCount();
    }

    public boolean isBossRoom(int roomNum) {
        if (currentRaid == null || roomNum < 1) return false;

        int challengeCount = currentRaid.getRaidKind().getChallengeCount();

        if (roomNum <= challengeCount) return false;

        return roomNum <= currentRaid.getRaidKind().getBossCount() + challengeCount;
    }

    public boolean isInBuffRoom() {
        return inBuffRoom;
    }

    public boolean isInIntermissionRoom() {
        return inIntermissionRoom;
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

    public long getRaidBestTime(String raidName) {
        for (RaidKind raidKind : RAIDS) {
            if (raidKind.getRaidName().equalsIgnoreCase(raidName)
                    || raidKind.getAbbreviation().equalsIgnoreCase(raidName)) {
                return bestTimes.get().getOrDefault(raidKind.getRaidName(), -1L);
            }
        }

        return -1L;
    }

    private void completeRaid() {
        if (currentRaid == null) return;

        currentRaid.completeCurrentChallenge();

        WynntilsMod.postEvent(new RaidEndedEvent.Completed(currentRaid));
        historicRaids
                .get()
                .add(new HistoricRaidInfo(
                        currentRaid.getRaidKind().getRaidName(),
                        currentRaid.getRaidKind().getAbbreviation(),
                        currentRaid.getChallenges(),
                        System.currentTimeMillis()));
        historicRaids.touched();

        checkForNewPersonalBest();

        currentRaid = null;
        completedCurrentChallenge = false;
        timeLeft = 0;
        challenges = CappedValue.EMPTY;
        partyRaidBuffs.clear();
        parasiteOvertaken = false;
    }

    private void checkForNewPersonalBest() {
        long timeInRaid = currentRaid.getTimeInRaid() - currentRaid.getIntermissionTime();

        if (timeInRaid == 0) {
            WynntilsMod.warn("Completed raid time was 0, tracking failed.");
            return;
        }

        if (bestTimes.get().get(currentRaid.getRaidKind().getRaidName()) == null) {
            bestTimes.get().put(currentRaid.getRaidKind().getRaidName(), timeInRaid);
            bestTimes.touched();
        } else {
            long currentBestTime = bestTimes.get().get(currentRaid.getRaidKind().getRaidName());

            // New time is faster
            if (currentBestTime > timeInRaid) {
                bestTimes.get().put(currentRaid.getRaidKind().getRaidName(), timeInRaid);
                bestTimes.touched();

                WynntilsMod.postEvent(
                        new RaidNewBestTimeEvent(currentRaid.getRaidKind().getRaidName(), timeInRaid));
            }
        }
    }

    private void processAspectItemFind(ItemStack itemStack, int slotId) {
        if (itemStack.isEmpty()) return;

        Optional<AspectItem> aspectOptional = Models.Item.asWynnItem(itemStack, AspectItem.class);
        if (aspectOptional.isPresent()) {
            AspectItem aspectItem = aspectOptional.get();
            if (aspectItem.getGearTier() == GearTier.MYTHIC) {
                foundMythicAspect = true;
                WynntilsMod.postEvent(
                        new ValuableFoundEvent(itemStack, ValuableFoundEvent.ItemSource.RAID_REWARD_CHEST));
            }
            return;
        }

        WynntilsMod.warn("[RaidModel] Unexpectedly found item \""
                + StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting() + "\" at slot "
                + slotId + ", but this slot should be an aspect reward slot.");
    }

    private void processRewardItemFind(ItemStack itemStack, int slotId) {
        if (itemStack.isEmpty()) return;

        foundNumRewardPulls += 1;

        Optional<AspectItem> aspectOptional = Models.Item.asWynnItem(itemStack, AspectItem.class);
        if (aspectOptional.isPresent()) {
            AspectItem aspectItem = aspectOptional.get();
            WynntilsMod.warn("[RaidModel] User found aspect item \"" + aspectItem + "\" at slot " + slotId
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
                WynntilsMod.postEvent(
                        new ValuableFoundEvent(itemStack, ValuableFoundEvent.ItemSource.RAID_REWARD_CHEST));
            }
            return;
        }
    }

    private RaidKind getRaidFromTitle(StyledText title) {
        return RAIDS.stream()
                .filter(raid -> raid.getEntryTitle().equals(title))
                .findFirst()
                .orElse(null);
    }

    private void registerRaids() {
        registerRaid(new NestOfTheGrootslangsRaid());
        registerRaid(new OrphionsNexusOfLightRaid());
        registerRaid(new TheCanyonColossusRaid());
        registerRaid(new TheNamelessAnomalyRaid());
    }

    private void registerRaid(RaidKind raidKind) {
        RAIDS.add(raidKind);
    }
}
