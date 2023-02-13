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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChatMentionFeature extends UserFeature {
    @Config
    public String aliases = "";

    private Pattern pattern = Pattern.compile(
            "(?<!\\[)\\b(" + McUtils.mc().getUser().getName()
                    + (aliases.length() > 0 ? "|" + aliases.replace(",", "|") : "") + ")\\b(?!:|])",
            Pattern.CASE_INSENSITIVE);

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
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
            e.setMessage(recurseMark(message));
            McUtils.playSound(SoundEvents.NOTE_BLOCK_PLING.value());
        }
    }

    private Component recurseMark(Component comp) {
        MutableComponent curr = MutableComponent.create(comp.getContents()).withStyle(comp.getStyle());
        // .getString() is used here as it gives formattingchars when those exist. It is needed for guild messages
        // because wynn still uses legacy coloring for it.
        String text = curr.getString();

        // case: component has no text
        if (text == "") {
            List<Component> sib = comp.getSiblings();
            if (sib.size() == 0) {
                // no siblings -> this is an end component return self
                return comp;
            } else {
                // some siblings -> check them
                for (Component c : sib) {
                    curr.append(recurseMark(c));
                }
            }
        } else {
            Matcher match = pattern.matcher(text);

            int nextStart = 0;

            List<MutableComponent> comps = new ArrayList();

            String lastcol = "";

            while (match.find()) {
                String before = text.substring(nextStart, match.start());
                String name = text.substring(match.start(), match.end());

                comps.add(Component.literal(before).withStyle(comp.getStyle()));
                // styling is not used as it breaks guild chat because wynn in their infinite wisdom decided to make
                // guild chat not use the propper styling
                comps.add(Component.literal("§e" + name));

                nextStart = match.end();
            }

            MutableComponent afterComp =
                    Component.literal(text.substring(nextStart)).withStyle(comp.getStyle());

            // process any siblings
            List<Component> sib = comp.getSiblings();
            if (sib.size() != 0) {
                for (Component c : sib) {
                    afterComp.append(recurseMark(c));
                }
            }

            // throw all of it together
            MutableComponent comp1 = afterComp;
            Collections.reverse(comps);

            for (MutableComponent c : comps) {
                c.append(comp1);
                comp1 = c;
            }

            return comp1;
        }

        return curr;
    }
}
