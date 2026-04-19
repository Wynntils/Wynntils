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
    public String getTranslatedText() {
        return translatedText;
    }
    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}
