/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue.event;

import com.wynntils.core.components.Models;
import com.wynntils.core.events.EventThread;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * With this Event you could also use your own Translator for
 * Npc Dialogues from your Mod as an Addon. <br />
 * Call {@link #outputTranslated(String)} after translation with your Output.
 * */
@EventThread(EventThread.Type.ANY)
public class TranslationRequestEvent extends Event implements ICancellableEvent {
    private String input;
    private boolean translated = false;

    public TranslationRequestEvent(String inputText) {
        this.input = inputText;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public boolean isTranslated() {
        return translated;
    }

    /**
     * Call this after starting your Translation
     * (synchronous after starting the translation Thread, not in the new Thread!) <br />
     * to prevent issues with multithreading.
     * */
    public void setTranslated(boolean translated) {
        this.translated = translated;
    }

    public void outputTranslated(String translatedText) {
        // Called directly, because of Multithreading
        Models.NpcDialogue.addCache(input, translatedText);
    }
}
