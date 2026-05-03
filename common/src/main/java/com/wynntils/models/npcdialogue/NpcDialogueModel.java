/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.characterstats.actionbar.matchers.DialogueSegmentMatcher;
import com.wynntils.models.npcdialogue.event.DialogueProcessedEvent;
import com.wynntils.models.npcdialogue.event.TranslationRequestEvent;
import com.wynntils.utils.colors.ColorChatFormatting;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.DialogueUtils;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public final class NpcDialogueModel extends Model {
    // very fast small and synchronous cache
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public ColorChatFormatting chatColor;
    public boolean renderOverChat;

    public NpcDialogueModel() {
        super(List.of());
        Handlers.ActionBar.registerSegment(new DialogueSegmentMatcher());
    }

    public void requestDialogueTranslation(String text, Consumer<String> consumer) {
        TranslationRequestEvent event = new TranslationRequestEvent(text, consumer);
        WynntilsMod.postEvent(event);

        if (!event.isTranslated() && !event.isCanceled()) {
            // Translator is not enabled (or something similar),
            // so we don't translate and provide the unmodified english text.
            addCache(event.getInput(), event.getInput());
        }
    }

    private void sendChat(DialogueUtils.Content content, StyledText translatedText) {
        Style textColor = Style.EMPTY.withColor(chatColor.getChatFormatting());
        Style darkGreen = Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);

        // §2Npcname: §aText
        Component message = Component.empty()
                .append(Component.literal(content.getName() != null ? content.getName() + ": " : "")
                        .withStyle(darkGreen))
                .append(translatedText
                        .getComponent()
                        .withStyle(textColor.withHoverEvent(
                                new HoverEvent.ShowText(StyledText.fromString(content.getText())
                                        .getComponent()
                                        .withStyle(textColor)))));

        // TODO: send only in chattabs with RecipientType.NPC and "ALL"

        McUtils.mc().execute(() -> {
            McUtils.mc().gui.getChat().addMessage(Component.empty());
            McUtils.mc().gui.getChat().addMessage(message);
        });
    }

    /**
     * this method will get called once per Dialogue
     * */
    public void dispatchContent(DialogueUtils.Content content, String tanslatedText, boolean sendToChat) {
        WynntilsMod.getLogger()
                .debug("[{}] Original Text: \"{}\"", this.getClass().getSimpleName(), content.getText());
        WynntilsMod.getLogger()
                .debug("[{}] Translated Text: \"{}\"", this.getClass().getSimpleName(), tanslatedText);

        StyledText styledTranslatedText = StyledText.fromString(tanslatedText);
        if (sendToChat) {
            sendChat(content, styledTranslatedText);
        }

        // for other Features or as an API Hook for Addons
        try {
            DialogueProcessedEvent dialogueProcessedEvent = new DialogueProcessedEvent(content, styledTranslatedText);
            WynntilsMod.postEvent(dialogueProcessedEvent);
        } catch (RuntimeException exception) {
            WynntilsMod.warn(exception.getMessage(), exception);
        }
    }

    public String getFromCache(String inputText) {
        return cache.get(inputText);
    }

    public void addCache(String inputText, String translatedText) {
        cache.put(inputText, translatedText);
    }

    /**
     * clear cache, to save memory or change language
     * */
    public void clearCache() {
        cache.clear();
    }
}
