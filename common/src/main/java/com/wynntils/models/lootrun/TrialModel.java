/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.lootrun.beacons.LootrunBeaconKind;
import com.wynntils.models.lootrun.event.LootrunChallengeEvent;
import com.wynntils.models.lootrun.event.LootrunStateEvent;
import com.wynntils.models.lootrun.type.LootrunTrialDetails;
import com.wynntils.models.lootrun.type.TrialType;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.neoforged.bus.api.SubscribeEvent;

public class TrialModel extends Model {
    private static final Pattern TRIAL_STARTED_PATTERN = Pattern.compile("\uDB00\uDC6D§b§lTrial Started");
    private static final Pattern TRIAL_NAME_PATTERN = Pattern.compile("(?:.+)?§7(?<trial>"
            + TrialType.trialTypes().stream().map(TrialType::getName).collect(Collectors.joining("|")) + ")");

    private boolean expectTrialStarted = false;

    private String currentTrial = "";
    private List<String> currentTrialObjective = new ArrayList<>();
    private List<CappedValue> currentTrialProgress = new ArrayList<>();

    @Persisted
    private final Storage<Map<String, LootrunTrialDetails>> lootrunTrialDetailsStorage = new Storage<>(new TreeMap<>());

    public TrialModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onCharacterChange(CharacterUpdateEvent event) {
        String id = Models.Character.getId();

        lootrunTrialDetailsStorage.get().putIfAbsent(id, new LootrunTrialDetails());
        lootrunTrialDetailsStorage.touched();
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent.Match event) {
        if (event.getRecipientType() != RecipientType.INFO) return;
        StyledText styledText = event.getMessage();

        Matcher matcher = TRIAL_STARTED_PATTERN.matcher(styledText.getString());
        if (matcher.find()) {
            expectTrialStarted = true;
            return;
        }

        if (expectTrialStarted) {
            matcher = TRIAL_NAME_PATTERN.matcher(styledText.getString());
            if (matcher.find()) {
                TrialType trial = TrialType.fromName(matcher.group("trial"));
                addTrial(trial);
            }
            expectTrialStarted = false;
        }
    }

    @SubscribeEvent
    private void onLootrunStateChanged(LootrunStateEvent event) {
        resetTrials();
    }

    @SubscribeEvent
    public void onChallengeFailed(LootrunChallengeEvent.Failed event) {
        LootrunBeaconKind color = Models.LootrunBeacon.getLastTaskBeaconColor();

        if (color == LootrunBeaconKind.CRIMSON) {
            addTrial(TrialType.FAILED);
        }
    }

    private void resetTrials() {
        getCurrentLootrunTrialDetails().setTrials(new ArrayList<>());
        lootrunTrialDetailsStorage.touched();
    }

    private void addTrial(TrialType trial) {
        if (!getCurrentLootrunTrialDetails().getTrials().contains(trial)) {
            getCurrentLootrunTrialDetails().addTrial(trial);
        }

        lootrunTrialDetailsStorage.touched();
    }

    public void setCurrentTrial(String currentTrial) {
        this.currentTrial = currentTrial;
    }

    public String getCurrentTrial() {
        return currentTrial;
    }

    public void setCurrentTrialObjective(List<String> currentTrialObjective) {
        this.currentTrialObjective = currentTrialObjective;
    }

    public String getCurrentTrialObjective(int index) {
        return index >= 0 && index < currentTrialObjective.size() ? currentTrialObjective.get(index) : "";
    }

    public void setCurrentTrialProgress(List<CappedValue> currentTrialProgress) {
        this.currentTrialProgress = currentTrialProgress;
    }

    public CappedValue getCurrentTrialProgress(int index) {
        return index >= 0 && index < currentTrialProgress.size() ? currentTrialProgress.get(index) : CappedValue.EMPTY;
    }

    public String getTrial(int index) {
        List<TrialType> trials = getCurrentLootrunTrialDetails().getTrials();

        if (index < 0 || index >= trials.size()) {
            return TrialType.UNKNOWN.getName();
        }

        TrialType trial = getCurrentLootrunTrialDetails().getTrials().get(index);
        return trial.getName();
    }

    private LootrunTrialDetails getCurrentLootrunTrialDetails() {
        return lootrunTrialDetailsStorage.get().getOrDefault(Models.Character.getId(), new LootrunTrialDetails());
    }
}
