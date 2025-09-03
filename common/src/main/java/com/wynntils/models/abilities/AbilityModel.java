/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.models.abilities.bossbars.AwakenedBar;
import com.wynntils.models.abilities.bossbars.BloodPoolBar;
import com.wynntils.models.abilities.bossbars.CommanderBar;
import com.wynntils.models.abilities.bossbars.CorruptedBar;
import com.wynntils.models.abilities.bossbars.FocusBar;
import com.wynntils.models.abilities.bossbars.HolyPowerBar;
import com.wynntils.models.abilities.bossbars.ManaBankBar;
import com.wynntils.models.abilities.bossbars.OphanimBar;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;

public final class AbilityModel extends Model {
    private static final Pattern HUMMINGBUIRD_SENT_PATTERN = Pattern.compile(
            "§e(:?(:?\uDAFF\uDFFC\uE008\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(:?\uDAFF\uDFFC\uE001\uDB00\uDC06)) You sent your hummingbirds to attack!");
    private static final Pattern HUMMINGBUIRD_RETURN_PATTERN = Pattern.compile(
            "§e(:?(:?\uDAFF\uDFFC\uE008\uDAFF\uDFFF\uE002\uDAFF\uDFFE)|(:?\uDAFF\uDFFC\uE001\uDB00\uDC06)) Your hummingbirds have returned to you!");
    public static final TrackedBar manaBankBar = new ManaBankBar();

    public static final TrackedBar bloodPoolBar = new BloodPoolBar();

    public static final TrackedBar awakenedBar = new AwakenedBar();

    public static final TrackedBar focusBar = new FocusBar();

    public static final TrackedBar corruptedBar = new CorruptedBar();

    public static final OphanimBar ophanimBar = new OphanimBar();

    public static final TrackedBar holyPowerBar = new HolyPowerBar();

    public static final CommanderBar commanderBar = new CommanderBar();

    public boolean hummingBirdsState = false;

    private static final List<TrackedBar> ALL_BARS = Arrays.asList(
            manaBankBar, bloodPoolBar, awakenedBar, focusBar, corruptedBar, ophanimBar, holyPowerBar, commanderBar);

    public AbilityModel() {
        super(List.of());

        ALL_BARS.forEach(Handlers.BossBar::registerBar);
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent event) {
        StyledText message = event.getStyledText();
        if (message.matches(HUMMINGBUIRD_RETURN_PATTERN)) {
            hummingBirdsState = false;
        } else if (message.matches(HUMMINGBUIRD_SENT_PATTERN)) {
            hummingBirdsState = true;
        }
    }
}
