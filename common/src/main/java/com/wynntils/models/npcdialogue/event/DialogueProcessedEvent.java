/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue.event;

import com.wynntils.core.events.EventThread;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.wynn.DialogueUtils;
import net.neoforged.bus.api.Event;

/**
 * Fired once for each dialogue after it has been processed and is ready for output.
 * <p>
 * This event is intended as an API hook for addons and integrations such as chat display,
 * TextToSpeech AIs, logging, or other custom dialogue handlers. <br />
 * {@link DialogueUtils.Content} contains all, that you need.
 * <p>
 * */
@EventThread(EventThread.Type.ANY)
public class DialogueProcessedEvent extends Event {
    private final DialogueUtils.Content content;
    private final String formattedTranslatedText;
    private final String translatedText;

    public DialogueProcessedEvent(DialogueUtils.Content content, StyledText translatedText) {
        this.content = content;
        this.formattedTranslatedText = translatedText.getString();
        this.translatedText = translatedText.getComponent().getString();
    }

    public DialogueUtils.Content getContent() {
        return content;
    }

    /**
     * If enabled, can contain chat formating codes like §0-9, §a-f or §#HEX-Color <br />
     * use {@code StyledText.fromString(text).getComponent()} to get formated Component
     * */
    public String getFormattedTranslatedText() {
        return formattedTranslatedText;
    }

    /**
     * @return the clean translated Text without Chatformatings
     * */
    public String getTranslatedText() {
        return translatedText;
    }
}
