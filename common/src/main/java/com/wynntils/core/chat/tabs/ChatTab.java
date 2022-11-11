/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.tabs;

import com.wynntils.core.chat.RecipientType;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import java.util.Set;
import java.util.regex.Pattern;

public class ChatTab {
    private String name;
    private boolean lowPriority;

    // Filters, inactive one is null
    private Set<RecipientType> filteredTypes;
    private String customRegexString;

    private transient Pattern customRegex;

    public ChatTab(String name, boolean lowPriority, Set<RecipientType> filteredTypes, String customRegexString) {
        this.name = name;
        this.lowPriority = lowPriority;
        this.filteredTypes = filteredTypes;
        this.customRegexString = customRegexString;

        if (customRegexString != null) {
            this.customRegex = Pattern.compile(customRegexString);
        }
    }

    public boolean matchMessageFromEvent(ChatMessageReceivedEvent event) {
        if (customRegexString != null) {
            return customRegex.matcher(event.getCodedMessage()).matches();
        }

        if (filteredTypes != null) {
            return filteredTypes.contains(event.getRecipientType());
        }

        return false;
    }

    public String getName() {
        return name;
    }

    public boolean isLowPriority() {
        return lowPriority;
    }
}
