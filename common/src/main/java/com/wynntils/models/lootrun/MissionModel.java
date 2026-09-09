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
import com.wynntils.models.lootrun.event.LootrunStartedEvent;
import com.wynntils.models.lootrun.event.LootrunStateEvent;
import com.wynntils.models.lootrun.type.LootrunMissionDetails;
import com.wynntils.models.lootrun.type.MissionType;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.neoforged.bus.api.SubscribeEvent;

public class MissionModel extends Model {
    private static final Pattern MISSION_COMPLETED_PATTERN =
            Pattern.compile("(?:[^\u0000-\u007F]+)?§b§lMission Completed");

    // Some missions don't have a mission completed message, so we also look for "active" missions
    // (missions that apply effects on challenge completion)
    private static final Pattern COMPLETED_MISSION_PATTERN = Pattern.compile("(?:[^\\u0000-\\u007F]+)?§.(?<mission>"
            + MissionType.missionTypes().stream().map(MissionType::getName).collect(Collectors.joining("|")) + ")");
    private static final Pattern ACTIVE_MISSION_PATTERN = Pattern.compile("[À\\s]*?§b§l(?<mission>"
            + MissionType.missionTypes().stream().map(MissionType::getName).collect(Collectors.joining("|")) + ")");

    private String currentMission = "";
    private List<String> currentMissionObjective = new ArrayList<>();
    private List<CappedValue> currentMissionProgress = new ArrayList<>();

    private boolean expectMissionComplete = false;

    @Persisted
    private final Storage<Map<String, LootrunMissionDetails>> lootrunMissionDetailsStorage =
            new Storage<>(new TreeMap<>());

    public MissionModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onCharacterChange(CharacterUpdateEvent event) {
        String id = Models.Character.getId();

        lootrunMissionDetailsStorage.get().putIfAbsent(id, new LootrunMissionDetails());
        lootrunMissionDetailsStorage.touched();
    }

    @SubscribeEvent
    public void onLootrunStarted(LootrunStartedEvent event) {
        // Ensure we start with fresh details
        lootrunMissionDetailsStorage.get().put(Models.Character.getId(), new LootrunMissionDetails());
        lootrunMissionDetailsStorage.touched();
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent.Match event) {
        if (event.getRecipientType() != RecipientType.INFO) return;
        StyledText styledText = event.getMessage();

        Matcher matcher = MISSION_COMPLETED_PATTERN.matcher(styledText.getString());
        if (matcher.matches()) {
            expectMissionComplete = true;
            return;
        }

        if (expectMissionComplete) {
            matcher = COMPLETED_MISSION_PATTERN.matcher(styledText.getString());
            if (matcher.matches()) {
                MissionType mission = MissionType.fromName(matcher.group("mission"));
                addMission(mission);
                return;
            }
        }

        matcher = ACTIVE_MISSION_PATTERN.matcher(styledText.getString());
        if (matcher.find()) {
            MissionType mission = MissionType.fromName(matcher.group("mission"));
            addMission(mission);
        }
    }

    @SubscribeEvent
    private void onLootrunStateChanged(LootrunStateEvent event) {
        resetMissions();
    }

    @SubscribeEvent
    public void onChallengeFailed(LootrunChallengeEvent.Failed event) {
        LootrunBeaconKind color = Models.LootrunBeacon.getLastTaskBeaconColor();

        if (color == LootrunBeaconKind.GRAY) {
            addMission(MissionType.FAILED);
        }
    }

    private void resetMissions() {
        getCurrentLootrunMissionDetails().setMissions(new ArrayList<>());
        lootrunMissionDetailsStorage.touched();
    }

    private void addMission(MissionType mission) {
        if (!getCurrentLootrunMissionDetails().getMissions().contains(mission)) {
            getCurrentLootrunMissionDetails().addMission(mission);
        }

        int rerolls = mission.getRerolls();
        if (rerolls > 0) {
            Models.Lootrun.setRerolls(Models.Lootrun.getRerolls() + rerolls);
        }

        int sacrifices = mission.getSacrifices();
        if (sacrifices > 0) {
            Models.Lootrun.setSacrifices(Models.Lootrun.getSacrifices() + sacrifices);
        }

        lootrunMissionDetailsStorage.touched();
        expectMissionComplete = false;
    }

    public void setCurrentMission(String currentMission) {
        this.currentMission = currentMission;
    }

    public String getCurrentMission(boolean colored) {
        return colored ? MissionType.fromName(currentMission).getColoredName() : currentMission;
    }

    public void setCurrentMissionObjective(List<String> currentMissionObjective) {
        this.currentMissionObjective = currentMissionObjective;
    }

    public String getCurrentMissionObjective(int index) {
        return index >= 0 && index < currentMissionObjective.size() ? currentMissionObjective.get(index) : "";
    }

    public void setCurrentMissionProgress(List<CappedValue> currentMissionProgress) {
        this.currentMissionProgress = currentMissionProgress;
    }

    public CappedValue getCurrentMissionProgress(int index) {
        return index >= 0 && index < currentMissionProgress.size()
                ? currentMissionProgress.get(index)
                : CappedValue.EMPTY;
    }

    public String getMissionStatus(int index, boolean colored) {
        List<MissionType> missions = getCurrentLootrunMissionDetails().getMissions();
        if (index < 0 || index >= missions.size()) {
            return colored ? MissionType.UNKNOWN.getColoredName() : MissionType.UNKNOWN.getName();
        }

        MissionType mission = getCurrentLootrunMissionDetails().getMissions().get(index);
        return colored ? mission.getColoredName() : mission.getName();
    }

    private LootrunMissionDetails getCurrentLootrunMissionDetails() {
        return lootrunMissionDetailsStorage.get().getOrDefault(Models.Character.getId(), new LootrunMissionDetails());
    }
}
