/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.lootrun.event.LootrunChallengeCountEvent;
import com.wynntils.models.lootrun.event.LootrunChallengeEvent;
import com.wynntils.models.lootrun.event.LootrunFinishedEventBuilder;
import com.wynntils.models.lootrun.event.LootrunStateEvent;
import com.wynntils.models.lootrun.scoreboard.LootrunScoreboardPart;
import com.wynntils.models.lootrun.type.LootrunDetails;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.models.lootrun.type.LootrunTaskType;
import com.wynntils.models.lootrun.type.LootrunningState;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;

/** A model dedicated to lootruns (the Wynncraft lootrun runs).
 * Don't confuse this with {@link com.wynntils.services.lootrunpaths.LootrunPathsService}.
 */
public class LootrunModel extends Model {
    private static final Pattern LOOTRUN_COMPLETED_PATTERN = Pattern.compile("\uDB00\uDC62§6§lLootrun Completed!");

    // Rewards
    private static final Pattern REWARD_PULLS_PATTERN = Pattern.compile("§.(\\d+)§7 Reward Pulls§r");
    private static final Pattern REWARD_REROLLS_PATTERN = Pattern.compile("§.(\\d+)§7 Reward Rerolls§r");
    private static final Pattern REWARD_SACRIFICES_PATTERN = Pattern.compile("§.(\\d+)§7 Reward Sacrifices§r");
    private static final Pattern LOOTRUN_EXPERIENCE_PATTERN = Pattern.compile("§.(\\d+)§7 Lootrun Experience§r");

    // Statistics
    private static final Pattern TIME_ELAPSED_PATTERN = Pattern.compile("§7Time Elapsed: §.(\\d+):(\\d+)");
    private static final Pattern MOBS_KILLED_PATTERN = Pattern.compile("§7Mobs Killed: §.(\\d+)");
    private static final Pattern CHESTS_OPENED_PATTERN = Pattern.compile("§7Chests Open: §.(\\d+)");
    private static final Pattern CHALLENGES_COMPLETED_PATTERN = Pattern.compile("§7Challenges Completed: §.(\\d+)");

    private static final Pattern LOOTRUN_FAILED_PATTERN = Pattern.compile("\uDB00\uDC6D§c§lLootrun Failed!");
    private static final Pattern CHALLENGE_COMPLETED_PATTERN = Pattern.compile("\uDB00\uDC5E§a§lChallenge Completed");
    private static final Pattern CHALLENGE_FAILED_PATTERN = Pattern.compile("\uDB00\uDC68§c§lChallenge Failed!");
    private static final Pattern PREPARE_TO_LOOTRUN_PATTERN = Pattern.compile("§7Prepare to Lootrun");

    // These patterns detect when rerolls/sacrifices are gained after completing a challenge.
    // (Gambling Beast, Warmth Devourer)
    private static final Pattern CHALLENGE_GET_SACRIFICE_PATTERN =
            Pattern.compile("\\[\\+(\\d+) Reward Sacrifices?\\]");
    private static final Pattern CHALLENGE_GET_REROLL_PATTERN = Pattern.compile("\\[\\+(\\d+) Reward Rerolls?\\]");

    private static final LootrunScoreboardPart LOOTRUN_SCOREBOARD_PART = new LootrunScoreboardPart();

    @Persisted
    private final Storage<Map<String, LootrunDetails>> lootrunDetailsStorage = new Storage<>(new TreeMap<>());

    private LootrunFinishedEventBuilder.Completed lootrunCompletedBuilder;
    private LootrunFinishedEventBuilder.Failed lootrunFailedBuilder;

    private CappedValue challenges = CappedValue.EMPTY;
    private int timeLeft = 0;
    private LootrunningState lootrunningState = LootrunningState.NOT_RUNNING;
    private LootrunTaskType taskType;

    public LootrunModel() {
        super(List.of());

        Handlers.Scoreboard.addPart(LOOTRUN_SCOREBOARD_PART);
    }

    @SubscribeEvent
    public void onCharacterChange(CharacterUpdateEvent event) {
        String id = Models.Character.getId();

        lootrunDetailsStorage.get().putIfAbsent(id, new LootrunDetails());
        lootrunDetailsStorage.touched();
    }

    @SubscribeEvent
    public void onTitle(TitleSetTextEvent event) {
        StyledText title = StyledText.fromComponent(event.getComponent());

        if (title.matches(PREPARE_TO_LOOTRUN_PATTERN)) {
            setLootrunLocation(LootrunLocation.fromBlockPos(McUtils.player().blockPosition()));
        }
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent.Match event) {
        if (event.getRecipientType() != RecipientType.INFO) return;
        StyledText styledText = event.getMessage();

        if (styledText.matches(LOOTRUN_COMPLETED_PATTERN)) {
            lootrunCompletedBuilder = new LootrunFinishedEventBuilder.Completed();
            lootrunFailedBuilder = null;
            return;
        }
        if (styledText.matches(LOOTRUN_FAILED_PATTERN)) {
            lootrunFailedBuilder = new LootrunFinishedEventBuilder.Failed();
            lootrunCompletedBuilder = null;
            return;
        }

        if (lootrunCompletedBuilder != null) {
            parseCompletedMessages(styledText);
        } else if (lootrunFailedBuilder != null) {
            parseFailedMessages(styledText);
        }

        Matcher matcher = CHALLENGE_GET_SACRIFICE_PATTERN.matcher(styledText.getString());
        if (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            LootrunDetails details = getCurrentLootrunDetails();
            details.setSacrifices(details.getSacrifices() + amount);
            lootrunDetailsStorage.touched();
            return;
        }

        matcher = CHALLENGE_GET_REROLL_PATTERN.matcher(styledText.getString());
        if (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            LootrunDetails details = getCurrentLootrunDetails();
            details.setRerolls(details.getRerolls() + amount);
            lootrunDetailsStorage.touched();
            return;
        }

        matcher = CHALLENGE_COMPLETED_PATTERN.matcher(styledText.getString());
        if (matcher.matches()) {
            WynntilsMod.postEvent(new LootrunChallengeEvent.Completed());
            return;
        }

        matcher = CHALLENGE_FAILED_PATTERN.matcher(styledText.getString());
        if (matcher.matches()) {
            WynntilsMod.postEvent(new LootrunChallengeEvent.Failed());
            return;
        }
    }

    @SubscribeEvent
    public void onWorldStateChanged(WorldStateEvent event) {
        // The world state event is sometimes late compared to lootrun events (beacons, scoreboard)
        // Resetting once when leaving the class is enough
        if (event.getNewState() == WorldState.WORLD) return;

        lootrunCompletedBuilder = null;
        lootrunFailedBuilder = null;

        lootrunningState = LootrunningState.NOT_RUNNING;
        taskType = null;

        challenges = CappedValue.EMPTY;
        timeLeft = 0;
    }

    public void setState(LootrunningState newState, LootrunTaskType taskType) {
        // If nothing changes, don't do anything.
        if (this.lootrunningState == newState) return;

        LootrunningState oldState = this.lootrunningState;
        this.lootrunningState = newState;
        this.taskType = taskType;

        if (newState == LootrunningState.NOT_RUNNING) {
            taskType = null;

            resetSacrifices();
            resetRerolls();

            timeLeft = 0;
            challenges = CappedValue.EMPTY;
        }

        WynntilsMod.postEvent(new LootrunStateEvent(newState, oldState));
    }

    public LootrunningState getState() {
        return lootrunningState;
    }

    public Optional<LootrunTaskType> getTaskType() {
        return Optional.ofNullable(taskType);
    }

    public void setLootrunLocation(LootrunLocation lootrunLocation) {
        getCurrentLootrunDetails().setLootrunLocation(lootrunLocation);
        lootrunDetailsStorage.touched();
    }

    public LootrunLocation getLootrunLocation() {
        return getCurrentLootrunDetails().getLootrunLocation();
    }

    public void setTimeLeft(int seconds) {
        timeLeft = seconds;
    }

    public int getCurrentTime() {
        return timeLeft;
    }

    public void setChallenges(CappedValue amount) {
        CappedValue oldChallenges = challenges;
        challenges = amount;

        if (oldChallenges == CappedValue.EMPTY) return;

        WynntilsMod.postEvent(new LootrunChallengeCountEvent(challenges, oldChallenges));
    }

    public CappedValue getChallenges() {
        return challenges;
    }

    public void setSacrifices(int sacrifices) {
        getCurrentLootrunDetails().setSacrifices(sacrifices);
        lootrunDetailsStorage.touched();
    }

    public int getSacrifices() {
        return getCurrentLootrunDetails().getSacrifices();
    }

    public void setRerolls(int rerolls) {
        getCurrentLootrunDetails().setRerolls(rerolls);
        lootrunDetailsStorage.touched();
    }

    public int getRerolls() {
        return getCurrentLootrunDetails().getRerolls();
    }

    private void resetSacrifices() {
        getCurrentLootrunDetails().setSacrifices(0);
        lootrunDetailsStorage.touched();
    }

    private void resetRerolls() {
        getCurrentLootrunDetails().setRerolls(0);
        lootrunDetailsStorage.touched();
    }

    private void parseCompletedMessages(StyledText styledText) {
        Matcher matcher = styledText.getMatcher(REWARD_PULLS_PATTERN);
        if (matcher.find()) {
            int pulls = Integer.parseInt(matcher.group(1));
            lootrunCompletedBuilder.setRewardPulls(pulls);

            matcher = styledText.getMatcher(TIME_ELAPSED_PATTERN);
            if (matcher.find()) {
                lootrunCompletedBuilder.setTimeElapsed(
                        Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2)));
                return;
            }

            WynntilsMod.warn("Found lootrun pulls but no time elapsed: " + styledText);
        }

        matcher = styledText.getMatcher(REWARD_REROLLS_PATTERN);
        if (matcher.find()) {
            lootrunCompletedBuilder.setRewardRerolls(Integer.parseInt(matcher.group(1)));

            matcher = styledText.getMatcher(MOBS_KILLED_PATTERN);
            if (matcher.find()) {
                lootrunCompletedBuilder.setMobsKilled(Integer.parseInt(matcher.group(1)));
                return;
            }

            WynntilsMod.warn("Found lootrun rerolls but no mobs killed: " + styledText);
        }

        matcher = styledText.getMatcher(REWARD_SACRIFICES_PATTERN);
        if (matcher.find()) {
            lootrunCompletedBuilder.setRewardSacrifices(Integer.parseInt(matcher.group(1)));

            matcher = styledText.getMatcher(CHESTS_OPENED_PATTERN);
            if (matcher.find()) {
                lootrunCompletedBuilder.setChestsOpened(Integer.parseInt(matcher.group(1)));
                return;
            }

            WynntilsMod.warn("Found lootrun sacrifices but no chests opened: " + styledText);
        }

        matcher = styledText.getMatcher(LOOTRUN_EXPERIENCE_PATTERN);
        if (matcher.find()) {
            lootrunCompletedBuilder.setExperienceGained(Integer.parseInt(matcher.group(1)));

            matcher = styledText.getMatcher(CHALLENGES_COMPLETED_PATTERN);
            if (matcher.find()) {
                lootrunCompletedBuilder.setChallengesCompleted(Integer.parseInt(matcher.group(1)));
                WynntilsMod.postEvent(lootrunCompletedBuilder.build());
                lootrunCompletedBuilder = null;
                return;
            }

            WynntilsMod.warn("Found lootrun experience but no challenges completed: " + styledText);
        }
    }

    private void parseFailedMessages(StyledText styledText) {
        Matcher matcher = styledText.getMatcher(TIME_ELAPSED_PATTERN);
        if (matcher.find()) {
            lootrunFailedBuilder.setTimeElapsed(
                    Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2)));
            return;
        }

        matcher = styledText.getMatcher(CHALLENGES_COMPLETED_PATTERN);
        if (matcher.find()) {
            lootrunFailedBuilder.setChallengesCompleted(Integer.parseInt(matcher.group(1)));
            WynntilsMod.postEvent(lootrunFailedBuilder.build());
            lootrunFailedBuilder = null;
            return;
        }
    }

    private LootrunDetails getCurrentLootrunDetails() {
        return lootrunDetailsStorage.get().getOrDefault(Models.Character.getId(), new LootrunDetails());
    }
}
