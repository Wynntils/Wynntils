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
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.utils.colors.ColorChatFormatting;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.IterationDecision;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class ChatMentionFeature extends Feature {
    private static final Pattern END_OF_HEADER_PATTERN = Pattern.compile(".*[\\]:]\\s?");

    @RegisterConfig
    public final Config<Boolean> markMention = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> dingMention = new Config<>(true);

    @RegisterConfig
    public final Config<ColorChatFormatting> mentionColor = new Config<>(ColorChatFormatting.YELLOW);

    @RegisterConfig
    public final Config<String> aliases = new Config<>("");

    @RegisterConfig
    public final Config<Boolean> suppressMentionsInInfo = new Config<>(false);

    private Pattern mentionPattern;

    public ChatMentionFeature() {
        mentionPattern = buildPattern();
    }

    @Override
    protected void onConfigUpdate(ConfigHolder<?> configHolder) {
        // rebuild pattern in case it has changed
        mentionPattern = buildPattern();
    }

    private Pattern buildPattern() {
        return Pattern.compile(
                "(?<!\\[)\\b(" + McUtils.mc().getUser().getName()
                        + (!aliases.get().isEmpty() ? "|" + aliases.get().replace(",", "|") : "") + ")\\b(?!:|])",
                Pattern.CASE_INSENSITIVE);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChat(ChatMessageReceivedEvent e) {
        if (e.getRecipientType() == RecipientType.INFO && suppressMentionsInInfo.get()) return;

        StyledText styledText = e.getStyledText();
        StyledText modified = styledText.iterateBackwards((part, changes) -> {
            // We have reached the end of the message content,
            // we don't want to highlight our own name in our own message
            if (END_OF_HEADER_PATTERN
                    .matcher(part.getString(null, PartStyle.StyleType.NONE))
                    .matches()) {
                return IterationDecision.BREAK;
            }

            StyledTextPart partToReplace = part;
            Matcher matcher = mentionPattern.matcher(partToReplace.getString(null, PartStyle.StyleType.NONE));

            while (matcher.find()) {
                String match = partToReplace.getString(null, PartStyle.StyleType.NONE);

                String firstPart = match.substring(0, matcher.start());
                String mentionPart = match.substring(matcher.start(), matcher.end());
                String lastPart = match.substring(matcher.end());

                PartStyle partStyle = partToReplace.getPartStyle();

                StyledTextPart first = new StyledTextPart(firstPart, partStyle.getStyle(), null, Style.EMPTY);
                StyledTextPart mention = new StyledTextPart(
                        mentionPart,
                        partStyle.getStyle().withColor(mentionColor.get().getChatFormatting()),
                        null,
                        first.getPartStyle().getStyle());
                StyledTextPart last = new StyledTextPart(lastPart, partStyle.getStyle(), null, Style.EMPTY);

                changes.remove(partToReplace);
                changes.add(first);
                changes.add(mention);
                changes.add(last);

                partToReplace = last;
                matcher = mentionPattern.matcher(lastPart);
            }

            return IterationDecision.CONTINUE;
        });

        // No changes were made, there was no mention.
        if (styledText.equals(modified)) return;

        if (markMention.get()) {
            e.setMessage(modified.getComponent());
        }

        if (dingMention.get()) {
            McUtils.playSoundUI(SoundEvents.NOTE_BLOCK_PLING.value());
        }
    }
}
