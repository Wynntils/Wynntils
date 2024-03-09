/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.models.npcdialogue.event.NpcDialogueProcessingEvent;
import com.wynntils.services.translation.TranslationService;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.UTILITIES)
public class TranslationFeature extends Feature {
    @Persisted
    public final Config<String> languageName = new Config<>("");

    @Persisted
    public final Config<Boolean> translateTrackedQuest = new Config<>(true);

    @Persisted
    public final Config<Boolean> translateNpc = new Config<>(true);

    @Persisted
    public final Config<Boolean> translateInfo = new Config<>(true);

    @Persisted
    public final Config<Boolean> translatePlayerChat = new Config<>(false);

    @Persisted
    public final Config<Boolean> keepOriginal = new Config<>(true);

    @Persisted
    public final Config<TranslationService.TranslationServices> translationService =
            new Config<>(TranslationService.TranslationServices.GOOGLEAPI);

    @SubscribeEvent
    public void onChat(ChatMessageReceivedEvent e) {
        if (languageName.get().isEmpty()) return;

        if (e.getRecipientType() != RecipientType.INFO && !translatePlayerChat.get()) return;
        if (e.getRecipientType() == RecipientType.INFO && !translateInfo.get()) return;

        StyledText origCoded = e.getStyledText();
        String wrapped = wrapCoding(origCoded);
        Services.Translation.getTranslator(translationService.get())
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
                    McUtils.mc().doRunTask(() -> McUtils.sendMessageToClient(messageToSend.getComponent()));
                });
        if (!keepOriginal.get()) {
            e.setCanceled(true);
        }
    }

    // Translation should be the last post-processing step
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onNpcDialogue(NpcDialogueProcessingEvent.Pre event) {
        if (!translateNpc.get()) return;
        if (languageName.get().isEmpty()) return;

        event.addProcessingStep(future -> future.thenCompose(styledTexts -> {
            if (styledTexts.isEmpty()) return CompletableFuture.completedFuture(styledTexts);

            CompletableFuture<List<StyledText>> translationFuture = new CompletableFuture<>();

            CompletableFuture.runAsync(() -> {
                try {
                    Services.Translation.getTranslator(translationService.get())
                            .translate(
                                    styledTexts.stream().map(this::wrapCoding).toList(),
                                    languageName.get(),
                                    translatedMsgList -> {
                                        List<StyledText> translatedComponents = new ArrayList<>();

                                        // Add the original message if requested
                                        if (keepOriginal.get()) {
                                            translatedComponents.addAll(styledTexts);
                                        }

                                        // Add the translated message
                                        translatedComponents.addAll(translatedMsgList.stream()
                                                .map(this::unwrapCoding)
                                                .toList());

                                        translationFuture.complete(translatedComponents);
                                    });
                } catch (Exception e) {
                    WynntilsMod.error("Failed to translate NPC dialogue.", e);
                    translationFuture.complete(styledTexts);
                }
            });

            return translationFuture;
        }));
    }

    private StyledText unwrapCoding(String origCoded) {
        // Some translated text (e.g. from pt_br) contains Á. This will be stripped later on,
        // so convert it to A (not ideal but better than nothing).
        // FIXME: Check if Á should be À
        // FIXME: We can easily inject back clickable/hoverable events here, with StyledText
        return StyledText.fromString(
                origCoded.replaceAll("\\{ ?§ ?([0-9a-fklmnor]) ?\\}", "§$1").replace('Á', 'A'));
    }

    private String wrapCoding(StyledText origCoded) {
        return origCoded.getString().replaceAll("(§[0-9a-fklmnor])", "{$1}");
    }
}
