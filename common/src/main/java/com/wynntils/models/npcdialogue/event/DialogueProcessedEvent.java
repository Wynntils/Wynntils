/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue.event;

import com.wynntils.core.events.EventThread;
import com.wynntils.utils.wynn.DialogueUtils;
import net.neoforged.bus.api.Event;

/**
 * Fired once for each dialogue after it has been processed and is ready for output.
 * <p>
 * This event is intended as an API hook for addons and integrations such as chat display,
 * TextToSpeach AIs, logging, or other custom dialogue handlers. <br />
 * {@link DialogueUtils.Content} contains all, that you need.
 * <p>
 * */
@EventThread(EventThread.Type.ANY)
public class DialogueProcessedEvent extends Event {
    private final DialogueUtils.Content content;
    private String translatedText;

    public DialogueProcessedEvent(DialogueUtils.Content content, String translatedText) {
        this.content = content;
        this.translatedText = translatedText;
    }

    public DialogueUtils.Content getContent() {
        return content;
    }

    /**
     * If enabled, can contain chat formating codes like §0-9, §a-f or §#HEX-Color <br />
     * use {@code StyledText.fromString(text).getComponent()} to format <br />
     * use {@code StyledText.fromString(text).getText()} to remove formatings
     * */
    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}
