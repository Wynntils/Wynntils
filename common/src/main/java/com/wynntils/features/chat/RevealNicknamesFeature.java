/*
 * Copyright © Wynntils 2024-2025.
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
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.type.IterationDecision;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class RevealNicknamesFeature extends Feature {
    private static final String NICKNAME_HOVER_TEXT = "§f%s§7's nickname is §f%s";

    public RevealNicknamesFeature() {
        super(ProfileDefault.DISABLED);
    }

    @Persisted
    private final Config<NicknameReplaceOption> nicknameReplaceOption =
            new Config<>(NicknameReplaceOption.PREPEND_USERNAME);

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerChat(ChatMessageEvent.Edit event) {
        StyledText styledText = event.getMessage().iterate((currentPart, changes) -> {
            HoverEvent hoverEvent = currentPart.getPartStyle().getStyle().getHoverEvent();

            // If the hover event doesn't exist or it is not SHOW_TEXT event, it's not a nickname text part
            if (hoverEvent == null || hoverEvent.getAction() != HoverEvent.Action.SHOW_TEXT) {
                return IterationDecision.CONTINUE;
            }

            StyledText[] partTexts = StyledText.fromComponent(hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT))
                    .split("\n");

            List<StyledText> newHoverTexts = new ArrayList<>();
            String nickname = null;
            String username = null;
            for (StyledText partText : partTexts) {
                Matcher nicknameMatcher = partText.getMatcher(StyledTextUtils.NICKNAME_PATTERN);

                if (nicknameMatcher.matches()) {
                    nickname = nicknameMatcher.group("nick");
                    username = nicknameMatcher.group("username");
                } else {
                    newHoverTexts.add(partText);
                }
            }

            // If the nickname or username is null, it's not a nickname text part
            if (nickname == null || username == null) {
                return IterationDecision.CONTINUE;
            }

            // If the text part is not the nickname as the text, it's not a nickname text part
            if (!currentPart.getString(null, StyleType.NONE).startsWith(nickname)) {
                return IterationDecision.CONTINUE;
            }

            newHoverTexts.add(
                    StyledText.fromComponent(Component.literal(NICKNAME_HOVER_TEXT.formatted(username, nickname))));

            // If the nickname matches, replace the text with the nickname
            switch (nicknameReplaceOption.get()) {
                case REPLACE -> {
                    changes.remove(currentPart);

                    String currentText = currentPart.getString(null, StyleType.NONE);
                    boolean hasColon = currentText.trim().endsWith(":");

                    String newText = username + (hasColon ? ": " : "");

                    // Add the real username as if it was the nickname
                    Style newStyle = currentPart
                            .getPartStyle()
                            .withItalic(false)
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    StyledText.join("\n", newHoverTexts).getComponent()))
                            .getStyle();

                    StyledTextPart newPart = new StyledTextPart(newText, newStyle, null, Style.EMPTY);
                    changes.add(newPart);
                }
                case PREPEND_USERNAME -> {
                    // Add the real username as a prefix to the nickname
                    Style newStyle = currentPart
                            .getPartStyle()
                            .withItalic(false)
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    StyledText.join("\n", newHoverTexts).getComponent()))
                            .getStyle();

                    StyledTextPart newPart = new StyledTextPart(username + "/", newStyle, null, Style.EMPTY);
                    StyledTextPart oldPart = new StyledTextPart(
                            currentPart.getString(null, StyleType.NONE), newStyle.withItalic(true), null, Style.EMPTY);

                    changes.remove(currentPart);
                    changes.add(newPart);
                    changes.add(oldPart);
                }
            }

            return IterationDecision.CONTINUE;
        });

        event.setMessage(styledText);
    }

    public enum NicknameReplaceOption {
        REPLACE,
        PREPEND_USERNAME
    }
}
