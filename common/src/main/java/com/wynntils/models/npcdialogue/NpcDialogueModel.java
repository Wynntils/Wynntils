/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.PacketEvent;
import com.wynntils.models.npcdialogue.event.DialogueProcessedEvent;
import com.wynntils.models.npcdialogue.event.OverlayDisplayEvent;
import com.wynntils.models.npcdialogue.event.TranslationRequestEvent;
import com.wynntils.utils.colors.ColorChatFormatting;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.DialogueUtils;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.neoforged.bus.api.SubscribeEvent;

public final class NpcDialogueModel extends Model {
    // Read this 2 public variables if you want to make a custom wynntils overlay
    // Keep in Mind, they only get updated when NpcDialogueFeature is enabled
    // (TranslationFeature enabled or not doesn't matter)
    public volatile DialogueUtils.Content lastDialogueContent;
    public volatile String lastTranslatedText;

    // very fast small and synchronous cache
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public boolean keepColors;
    public ColorChatFormatting chatColor;

    public NpcDialogueModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onPacketReceived(PacketEvent.PacketReceivedEvent<?> event) {
        if (event.getPacket() instanceof ClientboundSystemChatPacket chatPacket) {
            if (!chatPacket.overlay()) return;

            DialogueUtils.Content content = DialogueUtils.getDialogueContent(chatPacket.content(), keepColors);
            OverlayDisplayEvent overlayEvent = new OverlayDisplayEvent(chatPacket.content(), content);
            WynntilsMod.postEvent(overlayEvent);

            if (overlayEvent.isCanceled()) {
                event.setCanceled(true);
            }
        }
    }

    public void requestDialogueTranslation(DialogueUtils.Content content) {
        TranslationRequestEvent event = new TranslationRequestEvent(content.getText());
        WynntilsMod.postEvent(event);

        if (!event.isTranslated() && !event.isCanceled()) {
            // Translator ist not enabled (or something similar),
            // so we don't translate and provide the unmodified english text.
            addCache(event.getInput(), event.getInput());
        }
    }

    private void sendChat(DialogueUtils.Content content, StyledText tanslatedText) {
        Style textColor = Style.EMPTY.withColor(chatColor.getChatFormatting());
        Style darkGreen = Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);

        // §2Npcname: §aText
        Component message = Component.empty()
                .append(Component.literal(content.getName() != null ? content.getName() + ": " : "")
                        .withStyle(darkGreen))
                .append(tanslatedText
                        .getComponent()
                        .withStyle(textColor.withHoverEvent(
                                new HoverEvent.ShowText(StyledText.fromString(content.getText())
                                        .getComponent()
                                        .withStyle(textColor)))));

        // TODO: send only in chattabs with RecipientType.NPC

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
                .info("[{}] Original Text: \"{}\"", this.getClass().getSimpleName(), content.getText());
        WynntilsMod.getLogger()
                .info("[{}] Translated Text: \"{}\"", this.getClass().getSimpleName(), tanslatedText);

        StyledText styledTranslatedText = StyledText.fromString(tanslatedText);
        if (sendToChat) {
            sendChat(content, styledTranslatedText);
        }

        // for other Features or as an API Hook for Addons
        DialogueProcessedEvent dialogueProcessedEvent = new DialogueProcessedEvent(content, styledTranslatedText);
        WynntilsMod.postEvent(dialogueProcessedEvent);
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
