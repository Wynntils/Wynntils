/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.containers.type.MythicFind;
import com.wynntils.models.lootrun.beacons.LootrunBeaconKind;
import com.wynntils.models.lootrun.type.TaskLocation;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CappedValue;

import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class LootrunFunctions {

    @TemplateFunction(name = "dry_streak", aliases = "dry_s")
    public static int dryStreakFunction() {
        return Models.LootChest.getDryCount();
    }

    @TemplateFunction(name = "dry_boxes", aliases = {"dry_b", "dry_boxes_count"})
    public static int dryBoxesFunction() {
        return Models.LootChest.getDryBoxes();
    }

    @TemplateFunction(name = "dry_pulls", aliases = {"dry_p", "dry_pulls_count"})
    public static int dryPullsFunction() {
        return Models.Lootrun.dryPulls.get();
    }

    @TemplateFunction(name = "highest_dry_streak")
    public static int highestDryStreakFunction() {
        return Models.LootChest.getMythicFinds().stream().max(Comparator.comparing(MythicFind::dryCount)).map(MythicFind::dryCount).orElse(0);
    }

    @TemplateFunction(name = "last_dry_streak")
    public static int lastDryStreakFunction() {
        List<MythicFind> mythicFinds = Models.LootChest.getMythicFinds();

        if (mythicFinds.isEmpty()) return 0;

        return mythicFinds.getLast().dryCount();
    }

    @TemplateFunction(name = "last_mythic")
    public static String lastMythicFunction() {
        List<MythicFind> mythicFinds = Models.LootChest.getMythicFinds();

        if (mythicFinds.isEmpty()) return "";

        return mythicFinds.getLast().itemName();
    }

    @TemplateFunction(name ="chest_opened", aliases = "chest_count")
    public static int chestOpenedFunction() {
        return Models.LootChest.getOpenedChestCount();
    }

    @TemplateFunction(name = "lootrun_state")
    public static String lootrunStateFunction() {
        return Models.Lootrun.getState().toString();

    }

    @TemplateFunction(name = "lootrun_beacon_count")
    public static int lootrunBeaconCountFunction(String color) {
        LootrunBeaconKind lootrunBeaconKind = LootrunBeaconKind.fromName(color);
        if (lootrunBeaconKind == null) return -1;

        return Models.Lootrun.getBeaconCount(lootrunBeaconKind);
    }

    @TemplateFunction(name = "lootrun_mission")
    public static String lootrunMissionFunction(int missionIndex, boolean colored) {
        return Models.Lootrun.getMissionStatus(missionIndex, colored);
    }

    @TemplateFunction(name = "lootrun_trial")
    public static String lootrunTrialFunction(int trialIndex) {
        return Models.Lootrun.getTrial(trialIndex);
    }

    @TemplateFunction(name = "lootrun_task_name")
    public static String lootrunTaskNameFunction(String color) {
        LootrunBeaconKind lootrunBeaconKind = LootrunBeaconKind.fromName(color);
        if (lootrunBeaconKind == null) return "";

        TaskLocation taskLocation = Models.Lootrun.getTaskForColor(lootrunBeaconKind);
        if (taskLocation == null) return "";

        return taskLocation.name();
    }

    @TemplateFunction(name = "lootrun_task_location")
    public static Location lootrunTaskLocationFunction(String color) {
        LootrunBeaconKind lootrunBeaconKind = LootrunBeaconKind.fromName(color);
        if (lootrunBeaconKind == null) return new Location(0, 0, 0);

        TaskLocation taskLocation = Models.Lootrun.getTaskForColor(lootrunBeaconKind);
        if (taskLocation == null) return new Location(0, 0, 0);

        return taskLocation.location();
    }

    @TemplateFunction(name = "lootrun_task_type")
    public static String lootrunTaskTypeFunction(String color) {

        LootrunBeaconKind lootrunBeaconKind = LootrunBeaconKind.fromName(color);
        if (lootrunBeaconKind == null) return "";

        TaskLocation taskLocation = Models.Lootrun.getTaskForColor(lootrunBeaconKind);
        if (taskLocation == null) return "";

        return EnumUtils.toNiceString(taskLocation.taskType());
    }

    @TemplateFunction(name = "lootrun_beacon_vibrant")
    public static boolean lootrunBeaconVibrantFunction(String color) {
        LootrunBeaconKind lootrunBeaconKind = LootrunBeaconKind.fromName(color);
        if (lootrunBeaconKind == null) return false;

        return Models.Lootrun.isBeaconVibrant(lootrunBeaconKind);
    }

    @TemplateFunction(name = "lootrun_time")
    public static int lootrunTimeFunction() {
        return Models.Lootrun.getCurrentTime();
    }

    @TemplateFunction(name = "lootrun_challenges")
    public static CappedValue lootrunChallengesFunction() {
        return Models.Lootrun.getChallenges();
    }

    @TemplateFunction(name = "lootrun_last_selected_beacon_color")
    public static String lootrunLastSelectedBeaconColorFunction() {
        LootrunBeaconKind lootrunBeaconKind = Models.Lootrun.getLastTaskBeaconColor();
        if (lootrunBeaconKind == null) return "";

        return EnumUtils.toNiceString(lootrunBeaconKind);
    }

    @TemplateFunction(name = "lootrun_last_selected_beacon_vibrant")
    public static boolean lootrunLastSelectedBeaconVibrant() {
        return Models.Lootrun.wasLastBeaconVibrant();
    }

    @TemplateFunction(name = "lootrun_red_beacon_challenge_count")
    public static int lootrunRedBeaconChallengeCountFunction() {
        return Models.Lootrun.getRedBeaconTaskCount();
    }

    @TemplateFunction(name = "lootrun_orange_beacon_count")
    public static int lootrunOrangeBeaconCountFunction() {
        return Models.Lootrun.getActiveOrangeBeacons();
    }

    @TemplateFunction(name = "lootrun_next_orange_expire")
    public static int lootrunNextOrangeExpireFunction() {
        return Models.Lootrun.getChallengesTillNextOrangeExpires();
    }


    @TemplateFunction(name = "lootrun_rainbow_beacon_count")
    public static int lootrunRainbowBeaconCountFunction() {
        return Models.Lootrun.getActiveRainbowBeacons();

    }

    @TemplateFunction(name = "lootrun_sacrifices")
    public static int lootrunSacrificesFunction() {
        return Models.Lootrun.getSacrifices();

    }

    @TemplateFunction(name = "lootrun_rerolls")
    public static int lootrunRerollsFunction() {
        return Models.Lootrun.getRerolls();
    }


    @TemplateFunction(name = "chests_opened_this_session", aliases = "session_chests")
    public static int chestsOpenedThisSessionFunction(int tier, boolean exact) {
        return Models.LootChest.getLootChestOpenedThisSession(tier, exact);
    }

    @TemplateFunction(name = "chests_opened_this_session", aliases = "session_chests")
    public static int chestsOpenedThisSessionFunction() {
        return chestsOpenedThisSessionFunction(1, false);
    }
}
