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
    private final Config<Boolean> showIntermission = new Config<>(true);

    @Persisted
    private final Config<Boolean> showMilliseconds = new Config<>(true);

    @Persisted
    private final Config<Boolean> totalIntermission = new Config<>(true);

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
        if (showMilliseconds.get()) {
            return "\"\n§dChallenge " + challengeNum + ": \";if_str(eq(raid_room_time(\"challenge_" + challengeNum
                    + "\");-1);\"§7--:--.---\";concat(\"§b\";leading_zeros(int(div(div(raid_room_time(\"challenge_"
                    + challengeNum + "\");1000);60));2);\":\";leading_zeros(int(mod(div(raid_room_time(\"challenge_"
                    + challengeNum + "\");1000);60));2);\".\";leading_zeros(int(mod(raid_room_time(\"challenge_"
                    + challengeNum + "\");1000));3)));";
        }

        return "\"\n§dChallenge " + challengeNum + ": \";if_str(eq(raid_room_time(\"challenge_" + challengeNum
                + "\");-1);\"§7--:--\";concat(\"§b\";leading_zeros(int(div(div(raid_room_time(\"challenge_"
                + challengeNum + "\");1000);60));2);\":\";leading_zeros(int(mod(div(raid_room_time(\"challenge_"
                + challengeNum + "\");1000);60));2)));";
    }

    private String getChallengePreview(int challengeNum) {
        if (showMilliseconds.get()) {
            return "§dChallenge " + challengeNum + ": §b01:17.022\n";
        }

        return "§dChallenge " + challengeNum + ": §b01:17\n";
    }

    private String getBossTemplate() {
        if (showMilliseconds.get()) {
            return "\"\n\n§4Boss: \";if_str(eq(raid_room_time(\"boss_fight\");-1);\"§7--:--.--\";concat(\"§b\";leading_zeros(int(div(div(raid_room_time(\"boss_fight\");1000);60));2);\":\";leading_zeros(int(mod(div(raid_room_time(\"boss_fight\");1000);60));2);\".\";leading_zeros(int(mod(raid_room_time(\"boss_fight\");1000));3)));\"\n\";";
        }

        return "\"\n\n§4Boss: \";if_str(eq(raid_room_time(\"boss_fight\");-1);\"§7--:--\";concat(\"§b\";leading_zeros(int(div(div(raid_room_time(\"boss_fight\");1000);60));2);\":\";leading_zeros(int(mod(div(raid_room_time(\"boss_fight\");1000);60));2)));\"\n\";";
    }

    private String getBossPreview() {
        if (showMilliseconds.get()) {
            return "\n§4Boss: §7--:--.---\n";
        }

        return "\n§4Boss: §7--:--\n";
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

        if (showMilliseconds.get()) {
            return "\"\n§5Total: \";concat(\"§b\";leading_zeros(int(div(div(" + timeToUse
                    + ";1000);60));2);\":\";leading_zeros(int(mod(div(" + timeToUse
                    + ";1000);60));2);\".\";leading_zeros(int(mod(" + timeToUse + ";1000));3)))}";
        }

        return "\"\n§5Total: \";concat(\"§b\";leading_zeros(int(div(div(" + timeToUse
                + ";1000);60));2);\":\";leading_zeros(int(mod(div(" + timeToUse + ";1000);60));2)))}";
    }

    private String getTotalPreview() {
        if (totalIntermission.get()) {
            if (showMilliseconds.get()) {
                return "\n§5Total: §b03:36.279";
            }

            return "\n§5Total: §b03:36";
        } else {
            if (showMilliseconds.get()) {
                return "\n§5Total: §b03:21.207";
            }

            return "\n§5Total: §b03:21";
        }
    }
}
