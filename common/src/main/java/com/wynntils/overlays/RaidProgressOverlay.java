/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.handlers.scoreboard.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.models.raid.scoreboard.RaidScoreboardPart;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public class RaidProgressOverlay extends TextOverlay {
    private String template;

    private String previewTemplate;

    @Persisted
    public final Config<Boolean> showIntermission = new Config<>(false);

    @Persisted
    public final Config<Boolean> showMilliseconds = new Config<>(true);

    @Persisted
    public final Config<Boolean> totalIntermission = new Config<>(false);

    @Persisted
    public final Config<Boolean> showDamage = new Config<>(true);

    @Persisted
    private final Config<Boolean> disableRaidInfoOnScoreboard = new Config<>(false);

    public RaidProgressOverlay() {
        super(
                new OverlayPosition(
                        120,
                        5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.MIDDLE_LEFT),
                200,
                120);

        buildTemplates();
    }

    @Override
    protected String getTemplate() {
        return template;
    }

    @Override
    protected String getPreviewTemplate() {
        return previewTemplate;
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        buildTemplates();
    }

    @Override
    public boolean isVisible() {
        return Models.Raid.getCurrentRaid() != null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScoreboardSegmentChange(ScoreboardSegmentAdditionEvent event) {
        if (disableRaidInfoOnScoreboard.get() && event.getSegment().getScoreboardPart() instanceof RaidScoreboardPart) {
            event.setCanceled(true);
        }
    }

    private void buildTemplates() {
        StringBuilder templateBuilder = new StringBuilder("{concat(\"§6§l§n\";current_raid;\"\n\";");
        StringBuilder previewBuilder = new StringBuilder("§6§l§nNest of the Grootslangs\n\n");

        for (int i = 0; i < Models.Raid.MAXIMUM_CHALLENGE_ROOMS; i++) {
            templateBuilder.append(getChallengeTemplate(i + 1));
        }

        // Not using MAXIMUM_CHALLENGE_ROOMS as the preview template expects 3
        for (int i = 0; i < 3; i++) {
            previewBuilder.append(getChallengePreview(i + 1));
        }

        templateBuilder.append("\"\n\";");
        for (int i = 0; i < Models.Raid.MAXIMUM_BOSS_ROOMS; i++) {
            templateBuilder.append(getBossTemplate(i + 1));
        }
        templateBuilder.append("\"\n\";");
        previewBuilder.append(getBossPreview());

        if (showIntermission.get()) {
            templateBuilder.append(getIntermissionTemplate());
            previewBuilder.append(getIntermissionPreview());
        }

        templateBuilder.append(getTotalTemplate());
        previewBuilder.append(getTotalPreview());

        template = templateBuilder.toString();
        previewTemplate = previewBuilder.toString();
    }

    private String getChallengeTemplate(int challengeNum) {
        StringBuilder challengeBuilder = new StringBuilder("if_str(and(raid_has_room(")
                .append(challengeNum)
                .append(");not(raid_is_boss_room(")
                .append(challengeNum)
                .append(")));concat(\"\n§d\";if_str(eq_str(raid_room_name(")
                .append(challengeNum)
                .append(");\"\");\"Challenge ")
                .append(challengeNum)
                .append("\";raid_room_name(")
                .append(challengeNum)
                .append("));\": \";if_str(eq(raid_room_time(")
                .append(challengeNum)
                .append(");-1);\"§7");

        if (showMilliseconds.get()) {
            challengeBuilder.append("--:--.---");
        } else {
            challengeBuilder.append("--:--");
        }

        challengeBuilder
                .append("\";concat(\"§b\";leading_zeros(int(div(div(raid_room_time(")
                .append(challengeNum)
                .append(");1000);60));2);\":\";leading_zeros(int(mod(div(raid_room_time(")
                .append(challengeNum)
                .append(");1000);60));2)");

        if (showMilliseconds.get()) {
            challengeBuilder
                    .append(";\".\";leading_zeros(int(mod(raid_room_time(")
                    .append(challengeNum)
                    .append(");1000));3)))");
        } else {
            challengeBuilder.append("))");
        }

        if (showDamage.get()) {
            challengeBuilder
                    .append(";if_str(eq(raid_room_damage(")
                    .append(challengeNum)
                    .append(");-1);\"\";concat(\" §f(§e\";format(raid_room_damage(")
                    .append(challengeNum)
                    .append("));\"§f)\")));");
        } else {
            challengeBuilder.append(");");
        }

        challengeBuilder.append("\"\");");

        return challengeBuilder.toString();
    }

    private String getChallengePreview(int challengeNum) {
        String challengePreview;
        String challengeName =
                switch (challengeNum) {
                    case 1 -> "Slimey Platform";
                    case 2 -> "Gathering Room";
                    default -> "Hammer Room";
                };

        if (showMilliseconds.get()) {
            challengePreview = "§d" + challengeName + ": §b01:17.022";
        } else {
            challengePreview = "§d" + challengeName + ": §b01:17";
        }

        if (showDamage.get()) {
            challengePreview += " §f(§e343k§f)";
        }

        challengePreview += "\n";

        return challengePreview;
    }

    private String getBossTemplate(int bossNum) {
        String realBossNum = "int(add(current_raid_challenge_count;" + bossNum + "))";

        StringBuilder bossBuilder = new StringBuilder("if_str(raid_is_boss_room(")
                .append(realBossNum)
                .append(");concat(\"\n§4\";if_str(eq_str(raid_room_name(")
                .append(realBossNum)
                .append(");\"The ##### Anomaly\");\"The &k##### &r&4Anomaly\";raid_room_name(")
                .append(realBossNum)
                .append("));\": \";if_str(eq(raid_room_time(")
                .append(realBossNum)
                .append(");-1);\"§7");

        if (showMilliseconds.get()) {
            bossBuilder.append("--:--.---");
        } else {
            bossBuilder.append("--:--");
        }

        bossBuilder
                .append("\";concat(\"§b\";leading_zeros(int(div(div(raid_room_time(")
                .append(realBossNum)
                .append(");1000);60));2);\":\";leading_zeros(int(mod(div(raid_room_time(")
                .append(realBossNum)
                .append(");1000);60));2)");

        if (showMilliseconds.get()) {
            bossBuilder
                    .append(";\".\";leading_zeros(int(mod(raid_room_time(")
                    .append(realBossNum)
                    .append(");1000));3)))");
        } else {
            bossBuilder.append("))");
        }

        if (showDamage.get()) {
            bossBuilder
                    .append(";if_str(eq(raid_room_damage(")
                    .append(realBossNum)
                    .append(");-1);\"\";concat(\" §f(§e\";format(raid_room_damage(")
                    .append(realBossNum)
                    .append("));\"§f)\")));");
        } else {
            bossBuilder.append(");");
        }

        bossBuilder.append("\"\");");

        return bossBuilder.toString();
    }

    private String getBossPreview() {
        String bossPreview;

        if (showMilliseconds.get()) {
            bossPreview = "\n§4Grootslang Wyrmling: §7--:--.---";
        } else {
            bossPreview = "\n§4Grootslang Wyrmling: §7--:--";
        }

        if (showDamage.get()) {
            bossPreview += " §f(§e343k§f)";
        }

        bossPreview += "\n";

        return bossPreview;
    }

    private String getIntermissionTemplate() {
        if (showMilliseconds.get()) {
            return "\"\n§8Intermission: \";concat(\"§b\";leading_zeros(int(div(div(raid_intermission_time;1000);60));2);\":\";leading_zeros(int(mod(div(raid_intermission_time;1000);60));2);\".\";leading_zeros(int(mod(raid_intermission_time;1000));3));";
        }

        return "\"\n§8Intermission: \";concat(\"§b\";leading_zeros(int(div(div(raid_intermission_time;1000);60));2);\":\";leading_zeros(int(mod(div(raid_intermission_time;1000);60));2));";
    }

    private String getIntermissionPreview() {
        if (showMilliseconds.get()) {
            return "\n§8Intermission: §700:15.072";
        }

        return "\n§8Intermission: §700:15";
    }

    private String getTotalTemplate() {
        String timeToUse = totalIntermission.get() ? "raid_time" : "sub(raid_time;raid_intermission_time)";
        String totalTemplate;

        if (showMilliseconds.get()) {
            totalTemplate = "\"\n§5Total: \";concat(\"§b\";leading_zeros(int(div(div(" + timeToUse
                    + ";1000);60));2);\":\";leading_zeros(int(mod(div(" + timeToUse
                    + ";1000);60));2);\".\";leading_zeros(int(mod(" + timeToUse + ";1000));3))";
        } else {
            totalTemplate = "\"\n§5Total: \";concat(\"§b\";leading_zeros(int(div(div(" + timeToUse
                    + ";1000);60));2);\":\";leading_zeros(int(mod(div(" + timeToUse + ";1000);60));2))";
        }

        if (showDamage.get()) {
            totalTemplate += ";if_str(eq(raid_damage;-1);\"\";concat(\" §f(§e\";format(raid_damage);\"§f)\")))}";
        } else {
            totalTemplate += ")}";
        }

        return totalTemplate;
    }

    private String getTotalPreview() {
        String totalPreview;

        if (totalIntermission.get()) {
            if (showMilliseconds.get()) {
                totalPreview = "\n§5Total: §b03:36.279";
            } else {
                totalPreview = "\n§5Total: §b03:36";
            }
        } else {
            if (showMilliseconds.get()) {
                totalPreview = "\n§5Total: §b03:21.207";
            } else {
                totalPreview = "\n§5Total: §b03:21";
            }
        }

        if (showDamage.get()) {
            totalPreview += " §f(§e1.3M§f)";
        }

        return totalPreview;
    }
}
