/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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

        StyledText styledText = StyledText.fromComponent(message);

        StyledTextPart partFinding = styledText.getPartFinding(mentionPattern, PartStyle.StyleType.NONE);

        // If the part is null, it means that we did not get mentioned,
        // or the mention is in two different parts, we likely don't want to mark it.
        if (partFinding == null) {
            return;
        }

        if (markMention.get()) {
            Matcher matcher = styledText.getMatcher(mentionPattern, PartStyle.StyleType.NONE);

            if (!matcher.find()) {
                WynntilsMod.error(
                        "Found a part matching the mention pattern, but couldn't find a match in the string.");
                return;
            }

            StyledText newText = styledText.splitAt(matcher.start()).splitAt(matcher.end());

            StyledTextPart partMatching = newText.getPartMatching(mentionPattern, PartStyle.StyleType.NONE);

            if (partMatching == null) {
                WynntilsMod.error(
                        "Found a part finding the mention pattern, but couldn't find a match in the string after splitting.");
                return;
            }

            newText = newText.replacePart(
                    partMatching, partMatching.withStyle(style -> style.withColor(mentionColor.get())));

            MutableComponent newComponent = newText.getComponent();
            e.setMessage(newComponent);
        }

        if (dingMention.get()) {
            McUtils.playSoundUI(SoundEvents.NOTE_BLOCK_PLING.value());
        }
    }
}
