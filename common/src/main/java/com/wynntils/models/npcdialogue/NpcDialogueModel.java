/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.models.characterstats.actionbar.matchers.DialogueSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.segments.DialogueSegment;
import com.wynntils.models.npcdialogue.event.DialogueProcessedEvent;
import com.wynntils.models.npcdialogue.event.NpcDialogueUpdatedEvent;
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
import net.neoforged.bus.api.SubscribeEvent;

public final class NpcDialogueModel extends Model {
    // very fast small and synchronous cache
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public ColorChatFormatting chatColor;

    // maybe some other features need this. It's only true when the renderOverChat option AND the feature is enabled
    public boolean renderOverChat;
    public Component renderDialogue;

    public NpcDialogueModel() {
        super(List.of());
        Handlers.ActionBar.registerSegment(new DialogueSegmentMatcher());
    }

    @SubscribeEvent
    public void onActionBarRender(ActionBarRenderEvent event) {
        event.setSegmentEnabled(DialogueSegment.class, renderOverChat);
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresentOrElse(
                DialogueSegment.class,
                dialogueSegment -> {
                    renderDialogue = dialogueSegment.getDialogue().getComponent();
                    WynntilsMod.postEvent(new NpcDialogueUpdatedEvent(
                            dialogueSegment.getDialogueText(),
                            dialogueSegment.requiresShift(),
                            dialogueSegment.hasChoices()));
                },
                () -> {
                    renderDialogue = null;
                    WynntilsMod.postEvent(NpcDialogueUpdatedEvent.dialogueGone());
                });
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

    private void sendChat(DialogueUtils.Content content, String translatedText) {
        Style textColor = Style.EMPTY.withColor(chatColor.getChatFormatting());
        Style darkGreen = Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);
        StyledText styledTranslatedText = StyledText.fromString(replaceCommonSymbols(translatedText));

        // §2Npcname: §aText
        Component message = Component.empty()
                .append(Component.literal(content.getName() != null ? content.getName() + ": " : "")
                        .withStyle(darkGreen))
                .append(styledTranslatedText
                        .getComponent()
                        .withStyle(textColor.withHoverEvent(
                                new HoverEvent.ShowText(StyledText.fromString(replaceCommonSymbols(content.getText()))
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
    public void dispatchContent(DialogueUtils.Content content, String translatedText, boolean sendToChat) {
        WynntilsMod.info("[" + this.getClass().getSimpleName() + "] Original Text: \"" + content.getText() + "\"");
        WynntilsMod.info("[" + this.getClass().getSimpleName() + "] Translated Text: \"" + translatedText + "\"");

        if (sendToChat) {
            sendChat(content, translatedText);
        }

        // for other Features or as an API Hook for Addons
        try {
            StyledText styledTranslatedText = StyledText.fromString(translatedText);
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

    /**
     * Replaces common Wynncraft Element Symbols like Earth, Fire, Thunder, ... for Chat display.
     * */
    public String replaceCommonSymbols(String input) {
        return input.replace("\uE000", "❋") // air
                .replace("\uE001", "✤") // earth
                .replace("\uE002", "✹") // fire🛡
                .replace("\uE003", "✦") // thunder
                .replace("\uE004", "❉") // water
                .replace("\uE005", "Ⰾ") // neutral Ⰾ or ⚛
                .replace("\uE006", "♥") // heart
                .replace("\uE008", "\uD83D\uDDE1") // sword
                .replace("\uE009", "\uD83D\uDEE1"); // shield
    }
}
