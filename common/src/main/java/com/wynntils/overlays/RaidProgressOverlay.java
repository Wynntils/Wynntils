/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;

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

    public RaidProgressOverlay() {
        super(
                new OverlayPosition(
                        120,
                        5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.MIDDLE_LEFT),
                150,
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
    public boolean isRenderedDefault() {
        return Models.Raid.getCurrentRaid() != null;
    }

    private void buildTemplates() {
        StringBuilder templateBuilder = new StringBuilder("{concat(\"§6§l§n\";current_raid;\"\n\";");
        StringBuilder previewBuilder = new StringBuilder("§6§l§nNest of the Grootslangs\n\n");

        for (int i = 0; i < Models.Raid.MAX_CHALLENGES; i++) {
            templateBuilder.append(getChallengeTemplate(i + 1));
        }

        for (int i = 0; i < Models.Raid.MAX_CHALLENGES; i++) {
            previewBuilder.append(getChallengePreview(i + 1));
        }

        templateBuilder.append(getBossTemplate());
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
        StringBuilder challengeBuilder = new StringBuilder("\"\n§dChallenge ")
                .append(challengeNum)
                .append(": \";if_str(eq(raid_room_time(\"challenge_")
                .append(challengeNum)
                .append("\");-1);\"§7");

        if (showMilliseconds.get()) {
            challengeBuilder.append("--:--.---");
        } else {
            challengeBuilder.append("--:--");
        }

        challengeBuilder
                .append("\";concat(\"§b\";leading_zeros(int(div(div(raid_room_time(\"challenge_")
                .append(challengeNum)
                .append("\");1000);60));2);\":\";leading_zeros(int(mod(div(raid_room_time(\"challenge_")
                .append(challengeNum)
                .append("\");1000);60));2)");

        if (showMilliseconds.get()) {
            challengeBuilder
                    .append(";\".\";leading_zeros(int(mod(raid_room_time(\"challenge_")
                    .append(challengeNum)
                    .append("\");1000));3)));");
        } else {
            challengeBuilder.append("));");
        }

        if (showDamage.get()) {
            challengeBuilder
                    .append("if_str(eq(raid_room_damage(\"challenge_")
                    .append(challengeNum)
                    .append("\");-1);\"\";concat(\" §f(§e\";format(raid_room_damage(\"challenge_")
                    .append(challengeNum)
                    .append("\"));\"§f)\"));");
        }

        return challengeBuilder.toString();
    }

    private String getChallengePreview(int challengeNum) {
        String challengePreview;

        if (showMilliseconds.get()) {
            challengePreview = "§dChallenge " + challengeNum + ": §b01:17.022";
        } else {
            challengePreview = "§dChallenge " + challengeNum + ": §b01:17";
        }

        if (showDamage.get()) {
            challengePreview += " §f(§e343k§f)";
        }

        challengePreview += "\n";

        return challengePreview;
    }

    private String getBossTemplate() {
        String bossTemplate;

        if (showMilliseconds.get()) {
            bossTemplate =
                    "\"\n\n§4Boss: \";if_str(eq(raid_room_time(\"boss_fight\");-1);\"§7--:--.--\";concat(\"§b\";leading_zeros(int(div(div(raid_room_time(\"boss_fight\");1000);60));2);\":\";leading_zeros(int(mod(div(raid_room_time(\"boss_fight\");1000);60));2);\".\";leading_zeros(int(mod(raid_room_time(\"boss_fight\");1000));3)));";
        } else {
            bossTemplate =
                    "\"\n\n§4Boss: \";if_str(eq(raid_room_time(\"boss_fight\");-1);\"§7--:--\";concat(\"§b\";leading_zeros(int(div(div(raid_room_time(\"boss_fight\");1000);60));2);\":\";leading_zeros(int(mod(div(raid_room_time(\"boss_fight\");1000);60));2)));";
        }

        if (showDamage.get()) {
            bossTemplate +=
                    "if_str(eq(raid_room_damage(\"boss_fight\");-1);\"\n\";concat(\" §f(§e\";format(raid_room_damage(\"boss_fight\"));\"§f)\n\"));";
        } else {
            bossTemplate += "\"\n\";";
        }

        return bossTemplate;
    }

    private String getBossPreview() {
        String bossPreview;

        if (showMilliseconds.get()) {
            bossPreview = "\n§4Boss: §7--:--.---";
        } else {
            bossPreview = "\n§4Boss: §7--:--";
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
