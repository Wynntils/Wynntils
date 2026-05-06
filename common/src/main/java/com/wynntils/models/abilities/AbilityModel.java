/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.models.abilities.bossbars.AwakenedBar;
import com.wynntils.models.abilities.bossbars.BloodPoolBar;
import com.wynntils.models.abilities.bossbars.CommanderBar;
import com.wynntils.models.abilities.bossbars.CorruptedBar;
import com.wynntils.models.abilities.bossbars.DistortionBar;
import com.wynntils.models.abilities.bossbars.FocusBar;
import com.wynntils.models.abilities.bossbars.HolyPowerBar;
import com.wynntils.models.abilities.bossbars.ManaBankBar;
import com.wynntils.models.abilities.bossbars.MirrorImageBar;
import com.wynntils.models.abilities.bossbars.MomentumBar;
import com.wynntils.models.abilities.bossbars.NightcloakKnivesBar;
import com.wynntils.models.abilities.bossbars.OphanimBar;
import java.util.Arrays;
import java.util.List;

public final class AbilityModel extends Model {
    public static final TrackedBar awakenedBar = new AwakenedBar();

    public static final TrackedBar bloodPoolBar = new BloodPoolBar();

    public static final CommanderBar commanderBar = new CommanderBar();

    public static final TrackedBar corruptedBar = new CorruptedBar();

    public static final DistortionBar distortionBar = new DistortionBar();

    public static final TrackedBar focusBar = new FocusBar();

    public static final TrackedBar holyPowerBar = new HolyPowerBar();

    public static final TrackedBar manaBankBar = new ManaBankBar();

    public static final MirrorImageBar mirrorImageBar = new MirrorImageBar();

    public static final MomentumBar momentumBar = new MomentumBar();

    public static final NightcloakKnivesBar nightcloakKnivesBar = new NightcloakKnivesBar();

    public static final OphanimBar ophanimBar = new OphanimBar();

    private static final List<TrackedBar> ALL_BARS = Arrays.asList(
            awakenedBar,
            bloodPoolBar,
            commanderBar,
            corruptedBar,
            distortionBar,
            focusBar,
            holyPowerBar,
            manaBankBar,
            mirrorImageBar,
            momentumBar,
            nightcloakKnivesBar,
            ophanimBar);

    public AbilityModel() {
        super(List.of());

        ALL_BARS.forEach(Handlers.BossBar::registerBar);
    }
}
