/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import com.wynntils.models.beacons.type.Beacon;
import com.wynntils.models.lootrun.beacons.LootrunBeaconKind;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LootrunDetails {
    private Map<LootrunBeaconKind, Integer> selectedBeacons = new TreeMap<>();
    private LootrunBeaconKind lastTaskBeaconColor = null;
    private boolean lastTaskVibrantBeacon = false;
    private Beacon<LootrunBeaconKind> closestBeacon = null;
    private int redBeaconTaskCount = 0;
    private List<Integer> orangeBeaconCounts = new ArrayList<>();
    private int orangeAmount = -1;
    private int rainbowBeaconCount = 0;
    private int rainbowAmount = -1;
    private List<MissionType> missions = new ArrayList<>();
    private List<TrialType> trials = new ArrayList<>();
    private int sacrifices = 0;
    private int rerolls = 0;

    public Map<LootrunBeaconKind, Integer> getSelectedBeacons() {
        return Collections.unmodifiableMap(selectedBeacons);
    }

    public void setSelectedBeacons(Map<LootrunBeaconKind, Integer> selectedBeacons) {
        this.selectedBeacons = new TreeMap<>(selectedBeacons);
    }

    public void incrementBeaconCount(LootrunBeaconKind color) {
        selectedBeacons.put(color, selectedBeacons.getOrDefault(closestBeacon.beaconKind(), 0) + 1);
    }

    public LootrunBeaconKind getLastTaskBeaconColor() {
        return lastTaskBeaconColor;
    }

    public void setLastTaskBeaconColor(LootrunBeaconKind lastTaskBeaconColor) {
        this.lastTaskBeaconColor = lastTaskBeaconColor;
    }

    public boolean getLastTaskVibrantBeacon() {
        return lastTaskVibrantBeacon;
    }

    public void setLastTaskVibrantBeacon(boolean lastTaskVibrantBeacon) {
        this.lastTaskVibrantBeacon = lastTaskVibrantBeacon;
    }

    public Beacon<LootrunBeaconKind> getClosestBeacon() {
        return closestBeacon;
    }

    public void setClosestBeacon(Beacon<LootrunBeaconKind> closestBeacon) {
        this.closestBeacon = closestBeacon;
    }

    public int getRedBeaconTaskCount() {
        return redBeaconTaskCount;
    }

    public void setRedBeaconTaskCount(int redBeaconTaskCount) {
        this.redBeaconTaskCount = redBeaconTaskCount;
    }

    public List<Integer> getOrangeBeaconCounts() {
        return Collections.unmodifiableList(orangeBeaconCounts);
    }

    public void setOrangeBeaconCounts(List<Integer> orangeBeaconCounts) {
        this.orangeBeaconCounts = new ArrayList<>(orangeBeaconCounts);
    }

    public int getOrangeAmount() {
        return orangeAmount;
    }

    public void setOrangeAmount(int orangeAmount) {
        this.orangeAmount = orangeAmount;
    }

    public int getRainbowBeaconCount() {
        return rainbowBeaconCount;
    }

    public void setRainbowBeaconCount(int rainbowBeaconCount) {
        this.rainbowBeaconCount = rainbowBeaconCount;
    }

    public int getRainbowAmount() {
        return rainbowAmount;
    }

    public void setRainbowAmount(int rainbowAmount) {
        this.rainbowAmount = rainbowAmount;
    }

    public List<MissionType> getMissions() {
        return Collections.unmodifiableList(missions);
    }

    public void setMissions(List<MissionType> missions) {
        this.missions = new ArrayList<>(missions);
    }

    public void addMission(MissionType newMission) {
        missions.add(newMission);
    }

    public List<TrialType> getTrials() {
        return Collections.unmodifiableList(trials);
    }

    public void setTrials(List<TrialType> trials) {
        this.trials = new ArrayList<>(trials);
    }

    public void addTrial(TrialType newTrial) {
        trials.add(newTrial);
    }

    public int getSacrifices() {
        return sacrifices;
    }

    public void setSacrifices(int sacrifices) {
        this.sacrifices = sacrifices;
    }

    public int getRerolls() {
        return rerolls;
    }

    public void setRerolls(int rerolls) {
        this.rerolls = rerolls;
    }
}
