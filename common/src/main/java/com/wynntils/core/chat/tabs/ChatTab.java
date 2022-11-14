/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.tabs;

import com.wynntils.core.chat.RecipientType;
import com.wynntils.mc.event.ClientsideMessageEvent;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import java.util.Set;
import java.util.regex.Pattern;

public class ChatTab {
    private String name;
    private boolean lowPriority;

    // Filters
    private Set<RecipientType> filteredTypes;
    private String customRegexString;

    private transient Pattern customRegex;

    public ChatTab(String name, boolean lowPriority, Set<RecipientType> filteredTypes, String customRegexString) {
        this.name = name;
        this.lowPriority = lowPriority;
        this.filteredTypes = filteredTypes;
        this.customRegexString = customRegexString;
    }

    public boolean matchMessageFromEvent(ChatMessageReceivedEvent event) {
        if (filteredTypes != null && !filteredTypes.isEmpty() && !filteredTypes.contains(event.getRecipientType())) {
            return false;
        }

        if (customRegexString != null
                && !getCustomRegex().matcher(event.getCodedMessage()).matches()) {
            return false;
        }

        return true;
    }

    public boolean matchMessageFromEvent(ClientsideMessageEvent event) {
        if (customRegexString == null) {
            return false;
        }

        return getCustomRegex().matcher(event.getCodedMessage()).matches();
    }

    public String getName() {
        return name;
    }

    public boolean isLowPriority() {
        return lowPriority;
    }

    public Pattern getCustomRegex() {
        return customRegex == null && customRegexString != null
                ? customRegex = Pattern.compile(customRegexString, Pattern.DOTALL)
                : customRegex;
    }
}
