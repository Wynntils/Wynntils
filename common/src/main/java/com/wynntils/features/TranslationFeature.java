/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.core.net.translation.TranslationManager;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.event.NpcDialogEvent;
import com.wynntils.handlers.chat.type.NpcDialogueType;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.UNCATEGORIZED)
public class TranslationFeature extends Feature {
    @RegisterConfig
    public final Config<String> languageName = new Config<>("");

    @RegisterConfig
    public final Config<Boolean> translateTrackedQuest = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> translateNpc = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> translateInfo = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> translatePlayerChat = new Config<>(false);

    @RegisterConfig
    public final Config<Boolean> keepOriginal = new Config<>(true);

    @RegisterConfig
    public final Config<TranslationManager.TranslationServices> translationService =
            new Config<>(TranslationManager.TranslationServices.GOOGLEAPI);

    @SubscribeEvent
    public void onChat(ChatMessageReceivedEvent e) {
        if (e.getRecipientType() != RecipientType.INFO && !translatePlayerChat.get()) return;
        if (e.getRecipientType() == RecipientType.INFO && !translateInfo.get()) return;

        StyledText origCoded = e.getCodedMessage();
        String wrapped = wrapCoding(origCoded);
        Managers.Translation.getTranslator(translationService.get())
                .translate(List.of(wrapped), languageName.get(), translatedMsgList -> {
                    StyledText messageToSend;
                    if (!translatedMsgList.isEmpty()) {
                        String result = translatedMsgList.get(0);
                        messageToSend = unwrapCoding(result);
                    } else {
                        if (keepOriginal.get()) return;

                        // We failed to get a translation; send the original message so it's not lost
                        messageToSend = origCoded;
                    }
                    McUtils.mc().doRunTask(() -> McUtils.sendMessageToClient(messageToSend.asComponent()));
                });
        if (!keepOriginal.get()) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onNpcDialgue(NpcDialogEvent e) {
        if (!translateNpc.get()) return;
        if (e instanceof TranslatedNpcDialogEvent) return;

        if (!e.getChatMessage().isEmpty()) {
            List<String> wrapped = e.getChatMessage().stream()
                    .map(component -> wrapCoding(ComponentUtils.getCoded(component)))
                    .toList();
            Managers.Translation.getTranslator(translationService.get())
                    .translate(wrapped, languageName.get(), translatedMsgList -> {
                        List<StyledText> unwrapped = translatedMsgList.stream()
                                .map(this::unwrapCoding)
                                .toList();
                        // FIXME: We need a ComponentUtils.componentFromCoded()...
                        // This will currently remove all formatting :(
                        List<Component> translatedComponents = unwrapped.stream()
                                .map(s -> (Component) Component.literal(ComponentUtils.stripFormatting(s)))
                                .toList();
                        Managers.TickScheduler.scheduleNextTick(() -> {
                            NpcDialogEvent translatedEvent =
                                    new TranslatedNpcDialogEvent(translatedComponents, e.getType(), e.isProtected());
                            WynntilsMod.postEvent(translatedEvent);
                        });
                    });
        } else {
            // We must also pass on the null event to clear the dialogue
            NpcDialogEvent translatedEvent = new TranslatedNpcDialogEvent(List.of(), e.getType(), e.isProtected());
            WynntilsMod.postEvent(translatedEvent);
        }
        if (!keepOriginal.get()) {
            e.setCanceled(true);
        }
    }

    private StyledText unwrapCoding(String origCoded) {
        return StyledText.of(origCoded.replaceAll("\\{ ?§ ?([0-9a-fklmnor]) ?\\}", "§$1"));
    }

    private String wrapCoding(StyledText origCoded) {
        return origCoded.str().replaceAll("(§[0-9a-fklmnor])", "{$1}");
    }

    private static class TranslatedNpcDialogEvent extends NpcDialogEvent {
        protected TranslatedNpcDialogEvent(List<Component> chatMsg, NpcDialogueType type, boolean isProtected) {
            super(chatMsg, type, isProtected);
        }
    }
}
