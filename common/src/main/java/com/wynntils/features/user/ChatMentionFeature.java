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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private Pattern pattern = buildPattern();

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        // rebuild pattern incase it has changed
        pattern = buildPattern();
    }

    private Pattern buildPattern() {
        return Pattern.compile(
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
                e.setMessage(rewriteComponentWithHighlight(message));
            }
            if (dingMention) {
                McUtils.playSound(SoundEvents.NOTE_BLOCK_PLING.value());
            }
        }
    }

    private Component rewriteComponentWithHighlight(Component comp) {
        // Create a new component with only the body and style of comp
        MutableComponent curr = MutableComponent.create(comp.getContents()).withStyle(comp.getStyle());
        // .getString() is used here as it gives formattingchars when those exist. It is needed for guild messages
        // because wynn still uses legacy coloring for it.
        String text = curr.getString();

        // if current component has no text just process its children and return it
        if (text == "") {
            for (Component c : comp.getSiblings()) {
                curr.append(rewriteComponentWithHighlight(c));
            }

            return curr;
        }

        // component has text -> check if it has the mention and modify message to highlight it
        Matcher match = pattern.matcher(text);

        int nextStart = 0;

        List<MutableComponent> comps = new ArrayList();

        // NOTE: Message is modified here!
        while (match.find()) {
            // Mention found -> split component and highlight the mention
            String before = text.substring(nextStart, match.start());
            String name = text.substring(match.start(), match.end());

            comps.add(Component.literal(before).withStyle(comp.getStyle()));
            // styling is not used as it breaks guild chat because wynn in their infinite wisdom decided to make
            // guild chat not use the proper styling
            comps.add(Component.literal(rewriteColorCode + name));

            nextStart = match.end();
        }

        MutableComponent afterComp =
                Component.literal(text.substring(nextStart)).withStyle(comp.getStyle());

        // process any siblings
        for (Component c : comp.getSiblings()) {
            afterComp.append(rewriteComponentWithHighlight(c));
        }

        // throw all of it together
        MutableComponent modifiedComp = afterComp;
        Collections.reverse(comps);

        for (MutableComponent c : comps) {
            c.append(modifiedComp);
            modifiedComp = c;
        }

        return modifiedComp;
    }
}
