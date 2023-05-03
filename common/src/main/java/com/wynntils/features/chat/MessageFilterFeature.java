/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.text.CodedString;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class MessageFilterFeature extends Feature {
    // List of Pair<Foreground, Background>
    // Ensures we only try relevant regexes for any given message

    private static final List<Pair<Pattern, Pattern>> WELCOME = List.of(
            Pair.of(Pattern.compile("^§7Loading Resource Pack\\.\\.\\.$"), null),
            Pair.of(Pattern.compile("^§6Thank you for using the WynnPack\\. Enjoy the game!$"), null),
            Pair.of(
                    Pattern.compile(
                            "^§cSelect a character! Each character is saved individually across all servers, you can come back at any time with /class and select another character!$"),
                    null),
            Pair.of(Pattern.compile("^ +§6§lWelcome to Wynncraft!$"), null),
            Pair.of(Pattern.compile("^ +§fplay\\.wynncraft\\.com §7-/-§f wynncraft\\.com$"), null));

    private static final List<Pair<Pattern, Pattern>> SYSTEM_INFO =
            List.of(Pair.of(Pattern.compile("^(§r)?§.\\[Info\\] .*$"), Pattern.compile("^(§r§8)?\\[Info\\] .*$")));

    private static final List<Pair<Pattern, Pattern>> LEVEL_UP = List.of(
            Pair.of(
                    Pattern.compile("^§6.* is now (?:combat )?level .*(?: in §.*)?$"),
                    Pattern.compile("^(?:§r§8)?.* is now (?:combat )?level .*(?: in §.*)?$")),
            Pair.of(
                    Pattern.compile(
                            "^§8\\[§r§7!§r§8\\] §r§7Congratulations to §r.* for reaching (combat )?§r§flevel .*!$"),
                    Pattern.compile("^(§r§8)?\\[!\\] Congratulations to §r.* for reaching (combat )?§r§7level .*!$")));

    @RegisterConfig
    public final Config<Boolean> hideWelcome = new Config<>(false);

    @RegisterConfig
    public final Config<Boolean> hideSystemInfo = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> hideLevelUp = new Config<>(false);

    @RegisterConfig
    public final Config<Boolean> hidePartyFinder = new Config<>(false);

    @SubscribeEvent
    public void onMessage(ChatMessageReceivedEvent e) {
        CodedString msg = e.getOriginalCodedString();
        MessageType messageType = e.getMessageType();

        if (hideWelcome.get()) {
            e.setCanceled(processFilter(msg, messageType, WELCOME));
            return;
        }

        if (hideSystemInfo.get()) {
            e.setCanceled(processFilter(msg, messageType, SYSTEM_INFO));
            return;
        }

        if (hideLevelUp.get()) {
            e.setCanceled(processFilter(msg, messageType, LEVEL_UP));
            return;
        }

        if (hidePartyFinder.get()) {
            // §5Party Finder:§r§d Hey v8j, over here! Join the §r§bThe Nameless Anomaly§r§d queue and match up with
            // §r§e3 other players§r§d!
            // Matches until the username
            e.setCanceled(msg.startsWith(ChatFormatting.DARK_PURPLE + "Party Finder:" + ChatFormatting.RESET
                    + ChatFormatting.LIGHT_PURPLE + " Hey "));
            return;
        }
    }

    private boolean processFilter(CodedString msg, MessageType messageType, List<Pair<Pattern, Pattern>> patternMap) {
        for (Pair<Pattern, Pattern> pair : patternMap) {
            Pattern pattern = getPattern(pair, messageType);
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
        Pattern returnable =
                switch (messageType) {
                    case FOREGROUND -> p.a();
                    case BACKGROUND -> p.b();
                };

        if (returnable == null) {
            return switch (messageType) {
                case FOREGROUND -> p.b();
                case BACKGROUND -> p.a();
            };
        }

        return returnable;
    }
}
