/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.containers.type.MythicFind;
import com.wynntils.models.lootrun.beacons.LootrunBeaconKind;
import com.wynntils.models.lootrun.type.TaskLocation;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CappedValue;
import java.util.Comparator;
import java.util.List;
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class LootrunFunctions {

    @TemplateFunction(name = "dry_streak", aliases = { "dry_s" })
    public int dryStreakFunction() {
        return Models.LootChest.getDryCount();
    }

    @TemplateFunction(name = "dry_boxes", aliases = { "dry_b", "dry_boxes_count" })
    public int dryBoxesFunction() {
        return Models.LootChest.getDryBoxes();
    }

    @TemplateFunction(name = "dry_pulls", aliases = { "dry_p", "dry_pulls_count" })
    public int dryPullsFunction() {
        return Models.Lootrun.dryPulls.get();
    }

    @TemplateFunction(name = "highest_dry_streak")
    public int highestDryStreakFunction() {
        return Models.LootChest.getMythicFinds().stream().max(Comparator.comparing(MythicFind::dryCount)).map(MythicFind::dryCount).orElse(0);
    }

    @TemplateFunction(name = "last_dry_streak")
    public int lastDryStreakFunction() {
        List<MythicFind> mythicFinds = Models.LootChest.getMythicFinds();
        if (mythicFinds.isEmpty())
            return 0;
        return mythicFinds.getLast().dryCount();
    }

    @TemplateFunction(name = "last_mythic")
    public String lastMythicFunction() {
        List<MythicFind> mythicFinds = Models.LootChest.getMythicFinds();
        if (mythicFinds.isEmpty())
            return "";
        return mythicFinds.getLast().itemName();
    }

    @TemplateFunction(name = "chest_opened", aliases = { "chest_count" })
    public int chestOpenedFunction() {
        return Models.LootChest.getOpenedChestCount();
    }

    @TemplateFunction(name = "lootrun_state")
    public String lootrunStateFunction() {
        return Models.Lootrun.getState().toString();
    }

    @TemplateFunction(name = "lootrun_beacon_count")
    public int lootrunBeaconCountFunction(String color) {
        LootrunBeaconKind lootrunBeaconKind = LootrunBeaconKind.fromName(color);
        if (lootrunBeaconKind == null)
            return -1;
        return Models.Lootrun.getBeaconCount(lootrunBeaconKind);
    }

    @TemplateFunction(name = "lootrun_mission")
    public String lootrunMissionFunction(boolean colored, int index) {
        int missionIndex = index;
        return Models.Lootrun.getMissionStatus(missionIndex, colored);
    }

    @TemplateFunction(name = "lootrun_current_mission")
    public String lootrunCurrentMissionFunction(boolean colored) {
        return Models.Lootrun.getCurrentMission(colored);
    }

    @TemplateFunction(name = "lootrun_current_mission_objective")
    public String lootrunCurrentMissionObjectiveFunction(int index) {
        int missionIndex = index;
        return Models.Lootrun.getCurrentMissionObjective(missionIndex);
    }

    @TemplateFunction(name = "lootrun_current_mission_progress")
    public CappedValue lootrunCurrentMissionProgressFunction(int index) {
        int missionIndex = index;
        return Models.Lootrun.getCurrentMissionProgress(missionIndex);
    }

    @TemplateFunction(name = "lootrun_trial")
    public String lootrunTrialFunction(int index) {
        int trialIndex = index;
        return Models.Lootrun.getTrial(trialIndex);
    }

    @TemplateFunction(name = "lootrun_current_trial")
    public String lootrunCurrentTrialFunction() {
        return Models.Lootrun.getCurrentTrial();
    }

    @TemplateFunction(name = "lootrun_current_trial_objective")
    public String lootrunCurrentTrialObjectiveFunction(int index) {
        int trialIndex = index;
        return Models.Lootrun.getCurrentTrialObjective(trialIndex);
    }

    @TemplateFunction(name = "lootrun_current_trial_progress")
    public CappedValue lootrunCurrentTrialProgressFunction(int index) {
        int trialIndex = index;
        return Models.Lootrun.getCurrentTrialProgress(trialIndex);
    }

    @TemplateFunction(name = "lootrun_task_name")
    public String lootrunTaskNameFunction(String color) {
        LootrunBeaconKind lootrunBeaconKind = LootrunBeaconKind.fromName(color);
        if (lootrunBeaconKind == null)
            return "";
        TaskLocation taskLocation = Models.Lootrun.getTaskForColor(lootrunBeaconKind);
        if (taskLocation == null)
            return "";
        return taskLocation.name();
    }

    @TemplateFunction(name = "lootrun_task_location")
    public Location lootrunTaskLocationFunction(String color) {
        LootrunBeaconKind lootrunBeaconKind = LootrunBeaconKind.fromName(color);
        if (lootrunBeaconKind == null)
            return Location.ZERO;
        TaskLocation taskLocation = Models.Lootrun.getTaskForColor(lootrunBeaconKind);
        if (taskLocation == null)
            return Location.ZERO;
        return taskLocation.location();
    }

    @TemplateFunction(name = "lootrun_task_type")
    public String lootrunTaskTypeFunction(String color) {
        LootrunBeaconKind lootrunBeaconKind = LootrunBeaconKind.fromName(color);
        if (lootrunBeaconKind == null)
            return "";
        TaskLocation taskLocation = Models.Lootrun.getTaskForColor(lootrunBeaconKind);
        if (taskLocation == null)
            return "";
        return EnumUtils.toNiceString(taskLocation.taskType());
    }

    @TemplateFunction(name = "lootrun_beacon_vibrant")
    public boolean lootrunBeaconVibrantFunction(String color) {
        LootrunBeaconKind lootrunBeaconKind = LootrunBeaconKind.fromName(color);
        if (lootrunBeaconKind == null)
            return false;
        return Models.Lootrun.isBeaconVibrant(lootrunBeaconKind);
    }

    @TemplateFunction(name = "lootrun_time")
    public int lootrunTimeFunction() {
        return Models.Lootrun.getCurrentTime();
    }

    @TemplateFunction(name = "lootrun_challenges")
    public CappedValue lootrunChallengesFunction() {
        return Models.Lootrun.getChallenges();
    }

    @TemplateFunction(name = "lootrun_last_selected_beacon_color")
    public String lootrunLastSelectedBeaconColorFunction() {
        LootrunBeaconKind lootrunBeaconKind = Models.Lootrun.getLastTaskBeaconColor();
        if (lootrunBeaconKind == null)
            return "";
        return EnumUtils.toNiceString(lootrunBeaconKind);
    }

    @TemplateFunction(name = "lootrun_last_selected_beacon_vibrant")
    public boolean lootrunLastSelectedBeaconVibrantFunction() {
        return Models.Lootrun.wasLastBeaconVibrant();
    }

    @TemplateFunction(name = "lootrun_red_beacon_challenge_count")
    public int lootrunRedBeaconChallengeCountFunction() {
        return Models.Lootrun.getRedBeaconTaskCount();
    }

    @TemplateFunction(name = "lootrun_orange_beacon_count")
    public int lootrunOrangeBeaconCountFunction() {
        return Models.Lootrun.getActiveOrangeBeacons();
    }

    @TemplateFunction(name = "lootrun_next_orange_expire")
    public int lootrunNextOrangeExpireFunction() {
        return Models.Lootrun.getChallengesTillNextOrangeExpires();
    }

    @TemplateFunction(name = "lootrun_rainbow_beacon_count")
    public int lootrunRainbowBeaconCountFunction() {
        return Models.Lootrun.getActiveRainbowBeacons();
    }

    @TemplateFunction(name = "lootrun_sacrifices")
    public int lootrunSacrificesFunction() {
        return Models.Lootrun.getSacrifices();
    }

    @TemplateFunction(name = "lootrun_rerolls")
    public int lootrunRerollsFunction() {
        return Models.Lootrun.getRerolls();
    }

    @TemplateFunction(name = "chests_opened_this_session", aliases = { "session_chests" })
    public int chestsOpenedThisSessionFunction() {
        return chestsOpenedThisSessionFunction(1, false);
    }

    @TemplateFunction(name = "chests_opened_this_session", aliases = { "session_chests" })
    public int chestsOpenedThisSessionFunction(int tier, boolean exact) {
        return Models.LootChest.getLootChestOpenedThisSession(tier, exact);
    }
}
