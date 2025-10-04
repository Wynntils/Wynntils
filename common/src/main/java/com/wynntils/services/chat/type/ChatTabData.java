/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.chat.type;

import com.wynntils.core.WynntilsMod;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.minecraft.client.gui.components.ChatComponent;

public final class ChatTabData {
    private final ChatComponent chatComponent;
    private boolean hasUnreadMessages;
    private final Optional<Pattern> customRegex;

    public ChatTabData(ChatComponent chatComponent, boolean hasUnreadMessages, Optional<Pattern> customRegex) {
        this.chatComponent = chatComponent;
        this.hasUnreadMessages = hasUnreadMessages;
        this.customRegex = customRegex;
    }

    public ChatTabData(ChatComponent chatComponent, boolean hasUnreadMessages, String customRegexString) {
        this(chatComponent, hasUnreadMessages, Optional.ofNullable(compileRegex(customRegexString)));
    }

    private static Pattern compileRegex(String customRegexString) {
        if (customRegexString != null && !customRegexString.isBlank()) {
            try {
                return Pattern.compile(customRegexString, Pattern.DOTALL);
            } catch (PatternSyntaxException e) {
                WynntilsMod.warn("Got a saved invalid chat tab regex: " + customRegexString);
                return null;
            }
        }
        return null;
    }

    public ChatComponent getChatComponent() {
        return chatComponent;
    }

    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

    public void setUnreadMessages(boolean hasUnreadMessages) {
        this.hasUnreadMessages = hasUnreadMessages;
    }

    public Optional<Pattern> getCustomRegex() {
        return customRegex;
    }

    @Override
    public String toString() {
        return "ChatTabData[" + "chatComponent="
                + chatComponent + ", " + "hasUnreadMessages="
                + hasUnreadMessages + ", " + "customRegex="
                + customRegex + ']';
    }
}
