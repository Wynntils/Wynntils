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
    private Pattern customRegex;

    public ChatTab(String name, Set<RecipientType> filteredTypes, Pattern customRegex) {
        this.name = name;
        this.filteredTypes = filteredTypes;
        this.customRegex = customRegex;
    }

    public boolean matchMessageFromEvent(ChatMessageReceivedEvent event) {
        if (filteredTypes != null) {
            return filteredTypes.contains(event.getRecipientType());
        }

        if (customRegex != null) {
            return customRegex.matcher(event.getCodedMessage()).matches();
        }

        return false;
    }
}
