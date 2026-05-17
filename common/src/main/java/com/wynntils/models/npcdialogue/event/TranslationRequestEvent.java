/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue.event;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.events.EventThread;
import java.util.function.Consumer;
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
    private final Consumer<String> consumer;

    private boolean translated = false;

    public TranslationRequestEvent(String input, Consumer<String> consumer) {
        this.input = input;
        this.consumer = consumer;
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
     * (synchronous after starting the translation Thread, not in the new Thread!).
     * To prevent issues with multithreading. <br />
     * {@code event.setTranslated(true);}
     * */
    public void setTranslated(boolean translated) {
        this.translated = translated;
    }

    /**
     * Call this Asynchronous when your translation is done
     * */
    public void outputTranslated(String translatedText) {
        try {
            consumer.accept(translatedText);
        } catch (RuntimeException exception) {
            WynntilsMod.error(exception.getMessage(), exception);
        }
    }
}
