/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.CHAT)
public class ChatMentionFeature extends Feature {
    @RegisterConfig
    public final Config<Boolean> markMention = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> dingMention = new Config<>(true);

    @RegisterConfig
    public final Config<ChatFormatting> mentionColor = new Config<>(ChatFormatting.YELLOW);

    @RegisterConfig
    public final Config<String> aliases = new Config<>("");

    private Pattern mentionPattern;

    public ChatMentionFeature() {
        mentionPattern = buildPattern();
    }

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        // rebuild pattern in case it has changed
        mentionPattern = buildPattern();
    }

    private Pattern buildPattern() {
        return Pattern.compile(
                "(?<!\\[)\\b(" + McUtils.mc().getUser().getName()
                        + (!aliases.get().isEmpty() ? "|" + aliases.get().replace(",", "|") : "") + ")\\b(?!:|])",
                Pattern.CASE_INSENSITIVE);
    }

    @SubscribeEvent
    public void onChat(ChatMessageReceivedEvent e) {
        Component message = e.getMessage();

        Matcher looseMatcher = mentionPattern.matcher(ComponentUtils.getUnformatted(message));

        if (looseMatcher.find()) {
            if (markMention.get()) {
                e.setMessage(rewriteComponentTree(message));
            }
            if (dingMention.get()) {
                McUtils.playSoundUI(SoundEvents.NOTE_BLOCK_PLING.value());
            }
        }
    }

    private Component rewriteComponentTree(Component comp) {
        // Make a copy of the component without the siblings
        MutableComponent curr = MutableComponent.create(comp.getContents()).withStyle(comp.getStyle());
        // .getString() is used here as it gives formattingchars when those exist. It is needed for guild messages
        // because wynn still uses legacy coloring for it.
        String text = curr.getString();

        // if current component has text check it for mentions and rewrite the component if necessary
        if (!text.isEmpty()) {
            curr = rewriteMentions(curr, text, comp.getStyle());
        }

        // process and append siblings
        for (Component c : comp.getSiblings()) {
            curr.append(rewriteComponentTree(c));
        }

        return curr;
    }

    private MutableComponent rewriteMentions(MutableComponent curr, String text, Style style) {
        Matcher match = mentionPattern.matcher(text);

        // if we match then rewrite the component if there are no matches the function will just return
        if (match.find()) {
            // get the start of the string before any mention and set curr to be it
            curr = Component.literal(text.substring(0, match.start())).withStyle(style);

            // do the name of the first mention
            curr.append(Component.literal(mentionColor.get() + match.group(0)));

            // Store the point at which this match ended
            int lastEnd = match.end();

            // Handle any additional mentions within the message
            while (match.find()) {
                // get the bit in between of matches
                curr.append(Component.literal(text.substring(lastEnd, match.start())))
                        .withStyle(style);

                // get the name and recolor it
                curr.append(Component.literal(mentionColor.get() + match.group(0)));

                // set the starting point for the next mentions before variable
                lastEnd = match.end();
            }

            // finally add any text after the mentions back onto the component tree
            curr.append(Component.literal(text.substring(lastEnd))).withStyle(style);
        }

        return curr;
    }
}
