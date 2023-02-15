/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChatMentionFeature extends UserFeature {
    @Config
    public boolean markMention = true;

    @Config
    public boolean dingMention = true;

    @Config
    ChatFormatting rewriteColorCode = ChatFormatting.YELLOW;

    @Config
    public String aliases = "";

    private Pattern pattern;

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        // rebuild pattern in case it has changed
        buildPattern();
    }

    private void buildPattern() {
        pattern = Pattern.compile(
                "(?<!\\[)\\b(" + McUtils.mc().getUser().getName()
                        + (aliases.length() > 0 ? "|" + aliases.replace(",", "|") : "") + ")\\b(?!:|])",
                Pattern.CASE_INSENSITIVE);
    }

    @SubscribeEvent()
    public void onChat(ChatMessageReceivedEvent e) {
        Component message = e.getMessage();

        Matcher looseMatcher = pattern.matcher(ComponentUtils.getUnformatted(message));

        if (looseMatcher.find()) {
            if (markMention) {
                e.setMessage(rewriteComponentTree(message));
            }
            if (dingMention) {
                McUtils.playSound(SoundEvents.NOTE_BLOCK_PLING.value());
            }
        }
    }

    private Component rewriteComponentTree(Component comp) {
        // Make a copy of the component without the siblings
        MutableComponent curr = MutableComponent.create(comp.getContents()).withStyle(comp.getStyle());
        // .getString() is used here as it gives formattingchars when those exist. It is needed for guild messages
        // because wynn still uses legacy coloring for it.
        String text = curr.getString();

        // if current component has text check it for mentions
        if (text != "") {
            Matcher match = pattern.matcher(text);

            // if we match then rewrite the component
            if (match.find()) {
                // get the start of the string before any mention and set curr to be it
                String before = text.substring(0, match.start());
                curr = Component.literal(before).withStyle(comp.getStyle());

                int nextStart = 0;

                do {
                    // get the bit in between of matches if there are multiple matches
                    if (nextStart != 0) {
                        String middle = text.substring(nextStart, match.start());
                        curr.append(Component.literal(middle)).withStyle(comp.getStyle());
                    }

                    // get the name and recolor it
                    String name = text.substring(match.start(), match.end());
                    curr.append(Component.literal(rewriteColorCode + name));

                    nextStart = match.end();
                } while (match.find());

                // finally add the rest of the text back
                String after = text.substring(nextStart);
                curr.append(Component.literal(after)).withStyle(comp.getStyle());
            }
        }

        // process and append siblings
        for (Component c : comp.getSiblings()) {
            curr.append(rewriteComponentTree(c));
        }

        return curr;
    }
}
