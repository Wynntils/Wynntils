/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.models.abilities.bossbars.AwakenedBar;
import com.wynntils.models.abilities.bossbars.BloodPoolBar;
import com.wynntils.models.abilities.bossbars.CommanderBar;
import com.wynntils.models.abilities.bossbars.CorruptedBar;
import com.wynntils.models.abilities.bossbars.FocusBar;
import com.wynntils.models.abilities.bossbars.HolyPowerBar;
import com.wynntils.models.abilities.bossbars.ManaBankBar;
import com.wynntils.models.abilities.bossbars.MomentumBar;
import com.wynntils.models.abilities.bossbars.OphanimBar;
import com.wynntils.utils.mc.StyledTextUtils;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;

public final class AbilityModel extends Model {
    private static final Pattern HUMMINGBIRD_SENT_PATTERN =
            Pattern.compile("§e((\uE008\uE002)|\uE001) You sent your hummingbirds to attack!$");
    private static final Pattern HUMMINGBIRD_RETURN_PATTERN =
            Pattern.compile("§e((\uE008\uE002)|\uE001) Your hummingbirds have returned to you!$");
    public static final TrackedBar manaBankBar = new ManaBankBar();

    public static final TrackedBar bloodPoolBar = new BloodPoolBar();

    public static final TrackedBar awakenedBar = new AwakenedBar();

    public static final TrackedBar focusBar = new FocusBar();

    public static final TrackedBar corruptedBar = new CorruptedBar();

    public static final OphanimBar ophanimBar = new OphanimBar();

    public static final TrackedBar holyPowerBar = new HolyPowerBar();

    public static final CommanderBar commanderBar = new CommanderBar();

    public static final MomentumBar momentumBar = new MomentumBar();

    public boolean hummingBirdsState = false;

    private static final List<TrackedBar> ALL_BARS = Arrays.asList(
            awakenedBar,
            bloodPoolBar,
            commanderBar,
            corruptedBar,
            focusBar,
            holyPowerBar,
            manaBankBar,
            momentumBar,
            ophanimBar);

    public AbilityModel() {
        super(List.of());

        ALL_BARS.forEach(Handlers.BossBar::registerBar);
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent.Match event) {
        StyledText message = StyledTextUtils.unwrap(event.getMessage().stripAlignment());
        if (message.matches(HUMMINGBIRD_RETURN_PATTERN)) {
            hummingBirdsState = false;
        } else if (message.matches(HUMMINGBIRD_SENT_PATTERN)) {
            hummingBirdsState = true;
        }
    }
}
