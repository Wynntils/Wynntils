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
    private static final String TEMPLATE_MILLISECONDS =
            """
            {concat("§6§l§n";current_raid;"

            §dChallenge 1: ";if_str(eq(raid_room_time("challenge_1");-1);"§7--:--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("challenge_1");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("challenge_1");1000);60));2);":";leading_zeros(int(mod(raid_room_time("challenge_1");1000));3)));"
            §dChallenge 2: ";if_str(eq(raid_room_time("challenge_2");-1);"§7--:--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("challenge_2");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("challenge_2");1000);60));2);":";leading_zeros(int(mod(raid_room_time("challenge_2");1000));3)));"
            §dChallenge 3: ";if_str(eq(raid_room_time("challenge_3");-1);"§7--:--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("challenge_3");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("challenge_3");1000);60));2);":";leading_zeros(int(mod(raid_room_time("challenge_3");1000));3)));"

            §4Boss: ";if_str(eq(raid_room_time("boss_fight");-1);"§7--:--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("boss_fight");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("boss_fight");1000);60));2);":";leading_zeros(int(mod(raid_room_time("boss_fight");1000));3)));"

            §5Total: ";concat("§b";leading_zeros(int(div(div(raid_time;1000);60));2);":";leading_zeros(int(mod(div(raid_time;1000);60));2);":";leading_zeros(int(mod(raid_time;1000));3)))}
            """;

    private static final String TEMPLATE_MILLISECONDS_INTERMISSION =
            """
            {concat("§6§l§n";current_raid;"

            §dChallenge 1: ";if_str(eq(raid_room_time("challenge_1");-1);"§7--:--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("challenge_1");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("challenge_1");1000);60));2);":";leading_zeros(int(mod(raid_room_time("challenge_1");1000));3)));"
            §dChallenge 2: ";if_str(eq(raid_room_time("challenge_2");-1);"§7--:--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("challenge_2");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("challenge_2");1000);60));2);":";leading_zeros(int(mod(raid_room_time("challenge_2");1000));3)));"
            §dChallenge 3: ";if_str(eq(raid_room_time("challenge_3");-1);"§7--:--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("challenge_3");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("challenge_3");1000);60));2);":";leading_zeros(int(mod(raid_room_time("challenge_3");1000));3)));"

            §4Boss: ";if_str(eq(raid_room_time("boss_fight");-1);"§7--:--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("boss_fight");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("boss_fight");1000);60));2);":";leading_zeros(int(mod(raid_room_time("boss_fight");1000));3)));"

            §8Intermission: ";concat("§b";leading_zeros(int(div(div(raid_intermission_time;1000);60));2);":";leading_zeros(int(mod(div(raid_intermission_time;1000);60));2);":";leading_zeros(int(mod(raid_intermission_time;1000));3));"
            §5Total: ";concat("§b";leading_zeros(int(div(div(raid_time;1000);60));2);":";leading_zeros(int(mod(div(raid_time;1000);60));2);":";leading_zeros(int(mod(raid_time;1000));3)))}
            """;

    private static final String TEMPLATE_SECONDS =
            """
            {concat("§6§l§n";current_raid;"

            §dChallenge 1: ";if_str(eq(raid_room_time("challenge_1");-1);"§7--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("challenge_1");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("challenge_1");1000);60));2)));"
            §dChallenge 2: ";if_str(eq(raid_room_time("challenge_2");-1);"§7--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("challenge_2");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("challenge_2");1000);60));2)));"
            §dChallenge 3: ";if_str(eq(raid_room_time("challenge_3");-1);"§7--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("challenge_3");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("challenge_3");1000);60));2)));"

            §4Boss: ";if_str(eq(raid_room_time("boss_fight");-1);"§7--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("boss_fight");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("boss_fight");1000);60));2)));"

            §5Total: ";concat("§b";leading_zeros(int(div(div(raid_time;1000);60));2);":";leading_zeros(int(mod(div(raid_time;1000);60));2)))}
            """;

    private static final String TEMPLATE_SECONDS_INTERMISSION =
            """
            {concat("§6§l§n";current_raid;"

            §dChallenge 1: ";if_str(eq(raid_room_time("challenge_1");-1);"§7--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("challenge_1");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("challenge_1");1000);60));2)));"
            §dChallenge 2: ";if_str(eq(raid_room_time("challenge_2");-1);"§7--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("challenge_2");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("challenge_2");1000);60));2)));"
            §dChallenge 3: ";if_str(eq(raid_room_time("challenge_3");-1);"§7--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("challenge_3");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("challenge_3");1000);60));2)));"

            §4Boss: ";if_str(eq(raid_room_time("boss_fight");-1);"§7--:--";concat("§b";leading_zeros(int(div(div(raid_room_time("boss_fight");1000);60));2);":";leading_zeros(int(mod(div(raid_room_time("boss_fight");1000);60));2)));"

            §8Intermission: ";concat("§b";leading_zeros(int(div(div(raid_intermission_time;1000);60));2);":";leading_zeros(int(mod(div(raid_intermission_time;1000);60));2));"
            §5Total: ";concat("§b";leading_zeros(int(div(div(raid_time;1000);60));2);":";leading_zeros(int(mod(div(raid_time;1000);60));2)))}
            """;

    private static final String PREVIEW_TEMPLATE_MILLISECONDS =
            """
            §6§l§nNest of the Grootslangs

            §dChallenge 1: §b01:17:022
            §dChallenge 2: §b02:04:185
            §dChallenge 3: §7--:--:--

            §4Boss: §7--:--:--

            §5Total: §b03:36:279
            """;

    private static final String PREVIEW_TEMPLATE_MILLISECONDS_INTERMISSION =
            """
            §6§l§nNest of the Grootslangs

            §dChallenge 1: §b01:17:022
            §dChallenge 2: §b02:04:185
            §dChallenge 3: §7--:--:--

            §4Boss: §7--:--:--

            §8Intermission: §700:15:072
            §5Total: §b03:36:279
            """;

    private static final String PREVIEW_TEMPLATE_SECONDS =
            """
            §6§l§nNest of the Grootslangs

            §dChallenge 1: §b01:17
            §dChallenge 2: §b02:04
            §dChallenge 3: §7--:--

            §4Boss: §7--:--

            §5Total: §b03:36
            """;

    private static final String PREVIEW_TEMPLATE_SECONDS_INTERMISSION =
            """
            §6§l§nNest of the Grootslangs

            §dChallenge 1: §b01:17
            §dChallenge 2: §b02:04
            §dChallenge 3: §7--:--

            §4Boss: §7--:--

            §8Intermission: §700:15
            §5Total: §b03:36
            """;

    @Persisted
    private final Config<Boolean> showIntermission = new Config<>(true);

    @Persisted
    private final Config<Boolean> showMilliseconds = new Config<>(true);

    public RaidProgressOverlay() {
        super(
                new OverlayPosition(
                        60,
                        5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.MIDDLE_LEFT),
                150,
                120);
    }

    @Override
    protected String getTemplate() {
        if (showIntermission.get()) {
            return showMilliseconds.get() ? TEMPLATE_MILLISECONDS_INTERMISSION : TEMPLATE_SECONDS_INTERMISSION;
        }

        return showMilliseconds.get() ? TEMPLATE_MILLISECONDS : TEMPLATE_SECONDS;
    }

    @Override
    protected String getPreviewTemplate() {
        if (showIntermission.get()) {
            return showMilliseconds.get()
                    ? PREVIEW_TEMPLATE_MILLISECONDS_INTERMISSION
                    : PREVIEW_TEMPLATE_SECONDS_INTERMISSION;
        }

        return showMilliseconds.get() ? PREVIEW_TEMPLATE_MILLISECONDS : PREVIEW_TEMPLATE_SECONDS;
    }

    @Override
    public boolean isRenderedDefault() {
        return Models.Raid.getCurrentRaid() != null;
    }
}
