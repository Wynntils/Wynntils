/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class MessageFilterFeature extends Feature {
    // List of Pair<Foreground, Background>
    // Ensures we only try relevant regexes for any given message

    // By policy, we should never filter out any promotional messages from Wynncraft.
    // We do not want to harm their ability to make money and sustain the site.

    private static final List<Pair<Pattern, Pattern>> WELCOME = List.of(
            Pair.of(Pattern.compile("^§7Loading Resource Pack\\.\\.\\.$"), null),
            Pair.of(Pattern.compile("^§6Thank you for using the WynnPack\\. Enjoy the game!$"), null),
            Pair.of(
                    Pattern.compile(
                            "^§cSelect a character! Each character is saved individually across all servers, you can come back at any time with /class and select another character!$"),
                    null),
            Pair.of(Pattern.compile("^ +§6§lWelcome to Wynncraft!$"), null),
            Pair.of(Pattern.compile("^ +§fplay\\.wynncraft\\.com §7-/-§f wynncraft\\.com$"), null));

    // Test in MessageFilterFeature_SYSTEM_INFO_FG
    private static final Pattern SYSTEM_INFO_FG =
            Pattern.compile("^§#a0aec0ff(\uE01B\uE002|\uE001) .+$", Pattern.DOTALL);
    // Test in MessageFilterFeature_SYSTEM_INFO_BG
    private static final Pattern SYSTEM_INFO_BG =
            Pattern.compile("^§#c0c0c0ff(\uE01B\uE002|\uE001) .+$", Pattern.DOTALL);
    private static final List<Pair<Pattern, Pattern>> SYSTEM_INFO = List.of(Pair.of(SYSTEM_INFO_FG, SYSTEM_INFO_BG));

    private static final List<Pair<Pattern, Pattern>> LEVEL_UP = List.of(
            Pair.of(
                    Pattern.compile("^§6.* is now (?:combat )?level .*(?: in §.*)?$"),
                    Pattern.compile("^(?:§8)?.* is now (?:combat )?level .*(?: in §.*)?$")),
            Pair.of(
                    Pattern.compile("^§8\\[§7!§8\\] §7Congratulations to (§r)?.* for reaching (combat )?§flevel .*!$"),
                    Pattern.compile("^(§8)?\\[!\\] Congratulations to (§r)?.* for reaching (combat )?§7level .*!$")));

    // Test in MessageFilterFeature_PARTY_FINDER_FG
    private static final Pattern PARTY_FINDER_FG = Pattern.compile(
            "^§5(\uE00A\uE002|\uE001) Party Finder:§d Hey (§o)?[\\w ]{1,20}(§r§d)?, over here! Join the §?[a-zA-Z0-9#' ]+§d queue and match up with §e\\d{1,2}§d other players?!$");
    // Test in MessageFilterFeature_PARTY_FINDER_BG
    private static final Pattern PARTY_FINDER_BG = Pattern.compile(
            "^§8(\uE00A\uE002|\uE001) Party Finder: Hey (§o)?[\\w ]{1,20}(§r§8)?, over here! Join the [a-zA-Z' ]+ queue and match up with \\d{1,2} other players?!$");
    private static final List<Pair<Pattern, Pattern>> PARTY_FINDER = List.of(Pair.of(PARTY_FINDER_FG, PARTY_FINDER_BG));

    @Persisted
    private final Config<Boolean> hideWelcome = new Config<>(false);

    @Persisted
    private final Config<Boolean> hideSystemInfo = new Config<>(false);

    @Persisted
    private final Config<Boolean> hideLevelUp = new Config<>(false);

    @Persisted
    private final Config<Boolean> hidePartyFinder = new Config<>(false);

    public MessageFilterFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(
                        ConfigProfile.NEW_PLAYER, ConfigProfile.LITE, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onMessage(ChatMessageEvent.Match e) {
        StyledText msg = e.getMessage();
        MessageType messageType = e.getMessageType();

        if (hideWelcome.get() && processFilter(msg, messageType, WELCOME)) {
            e.cancelChat();
            return;
        }

        if (hideLevelUp.get() && processFilter(msg, messageType, LEVEL_UP)) {
            e.cancelChat();
            return;
        }

        StyledText unwrapped = StyledTextUtils.unwrap(msg).stripAlignment();

        if (hideSystemInfo.get() && processFilter(unwrapped, messageType, SYSTEM_INFO)) {
            e.cancelChat();
            return;
        }

        if (hidePartyFinder.get() && processFilter(unwrapped, messageType, PARTY_FINDER)) {
            e.cancelChat();
            return;
        }
    }

    private boolean processFilter(StyledText msg, MessageType messageType, List<Pair<Pattern, Pattern>> patternMap) {
        for (Pair<Pattern, Pattern> pair : patternMap) {
            Pattern pattern = getPattern(pair, messageType);
            if (pattern == null) {
                continue;
            }
            if (msg.getMatcher(pattern).find()) {
                return true;
            }
        }
        return false; // Failed to match any patterns
    }

    /**
     * Returns the pattern by the given messageType. If that pattern does not exist (null), returns the pattern for the
     * other messageType instead.
     */
    private Pattern getPattern(Pair<Pattern, Pattern> p, MessageType messageType) {
        return switch (messageType) {
            case FOREGROUND -> p.a();
            case BACKGROUND -> p.b();
        };
    }
}
