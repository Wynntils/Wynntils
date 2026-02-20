/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.utils.mc.StyledTextUtils;
import java.util.List;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class MessageFilterFeature extends Feature {
    // By policy, we should never filter out any promotional messages from Wynncraft.
    // We do not want to harm their ability to make money and sustain the site.

    private static final List<Pattern> WELCOME = List.of(
            Pattern.compile("^§7Loading Resource Pack\\.\\.\\.$"),
            Pattern.compile("^§6Thank you for using the WynnPack\\. Enjoy the game!$"),
            Pattern.compile(
                    "^§cSelect a character! Each character is saved individually across all servers, you can come back at any time with /class and select another character!$"),
            Pattern.compile("^ +§6§lWelcome to Wynncraft!$"),
            Pattern.compile("^ +§fplay\\.wynncraft\\.com §7-/-§f wynncraft\\.com$"));

    // Test in MessageFilterFeature_SYSTEM_INFO
    private static final Pattern SYSTEM_INFO = Pattern.compile("^§#a0aec0ff(\uE01B\uE002|\uE001) .+$", Pattern.DOTALL);

    private static final List<Pattern> LEVEL_UP = List.of(
            Pattern.compile("^§6.* is now (?:combat )?level .*(?: in §.*)?$"),
            Pattern.compile("^§8\\[§7!§8\\] §7Congratulations to (§r)?.* for reaching (combat )?§flevel .*!$"));

    // Test in MessageFilterFeature_PARTY_FINDER
    private static final Pattern PARTY_FINDER = Pattern.compile(
            "^§5(\uE00A\uE002|\uE001) Party Finder:§d Hey (§o)?[\\w ]{1,20}(§r§d)?, over here! Join the §?[a-zA-Z0-9#' ]+§d queue and match up with §e\\d{1,2}§d other players?!$");

    @Persisted
    private final Config<Boolean> hideWelcome = new Config<>(false);

    @Persisted
    private final Config<Boolean> hideSystemInfo = new Config<>(false);

    @Persisted
    private final Config<Boolean> hideLevelUp = new Config<>(false);

    @Persisted
    private final Config<Boolean> hidePartyFinder = new Config<>(false);

    public MessageFilterFeature() {
        super(ProfileDefault.onlyDefault());
    }

    @SubscribeEvent
    public void onMessage(ChatMessageEvent.Match e) {
        StyledText msg = e.getMessage();

        if (hideWelcome.get() && processFilter(msg, WELCOME)) {
            e.cancelChat();
            return;
        }

        if (hideLevelUp.get() && processFilter(msg, LEVEL_UP)) {
            e.cancelChat();
            return;
        }

        StyledText unwrapped = StyledTextUtils.unwrap(msg).stripAlignment();

        if (hideSystemInfo.get() && processFilter(unwrapped, List.of(SYSTEM_INFO))) {
            e.cancelChat();
            return;
        }

        if (hidePartyFinder.get() && processFilter(unwrapped, List.of(PARTY_FINDER))) {
            e.cancelChat();
            return;
        }
    }

    private boolean processFilter(StyledText msg, List<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (msg.getMatcher(pattern).find()) {
                return true;
            }
        }
        return false; // Failed to match any patterns
    }
}
