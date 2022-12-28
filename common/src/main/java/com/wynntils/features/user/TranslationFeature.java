/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.core.services.TranslationModel;
import com.wynntils.handlers.chat.NpcDialogueType;
import com.wynntils.handlers.chat.RecipientType;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.event.NpcDialogEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@StartDisabled
public class TranslationFeature extends UserFeature {
    public static TranslationFeature INSTANCE;

    @Config
    public String languageName = "";

    @Config
    public boolean translateTrackedQuest = true;

    @Config
    public boolean translateNpc = true;

    @Config
    public boolean translateInfo = true;

    @Config
    public boolean translatePlayerChat = false;

    @Config
    public boolean keepOriginal = true;

    @Config
    public TranslationModel.TranslationServices translationService = TranslationModel.TranslationServices.GOOGLEAPI;

    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.Translation);
    }

    @SubscribeEvent
    public void onChat(ChatMessageReceivedEvent e) {
        if (e.getRecipientType() != RecipientType.INFO && !translatePlayerChat) return;
        if (e.getRecipientType() == RecipientType.INFO && !translateInfo) return;

        String origCoded = e.getCodedMessage();
        String wrapped = wrapCoding(origCoded);
        Models.Translation.getTranslator().translate(List.of(wrapped), languageName, translatedMsgList -> {
            String messageToSend;
            if (!translatedMsgList.isEmpty()) {
                String result = translatedMsgList.get(0);
                messageToSend = unwrapCoding(result);
            } else {
                if (keepOriginal) return;

                // We failed to get a translation; send the original message so it's not lost
                messageToSend = origCoded;
            }
            McUtils.mc().doRunTask(() -> McUtils.sendMessageToClient(Component.literal(messageToSend)));
        });
        if (!keepOriginal) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onNpcDialgue(NpcDialogEvent e) {
        if (!translateNpc) return;
        if (e instanceof TranslatedNpcDialogEvent) return;

        if (!e.getChatMessage().isEmpty()) {
            List<String> wrapped = e.getChatMessage().stream()
                    .map(component -> wrapCoding(ComponentUtils.getCoded(component)))
                    .toList();
            Models.Translation.getTranslator().translate(wrapped, languageName, translatedMsgList -> {
                List<String> unwrapped =
                        translatedMsgList.stream().map(this::unwrapCoding).toList();
                // FIXME: We need a ComponentUtils.componentFromCoded()...
                // This will currently remove all formatting :(
                List<Component> translatedComponents = unwrapped.stream()
                        .map(s -> (Component) Component.literal(ComponentUtils.stripFormatting(s)))
                        .toList();
                McUtils.mc().doRunTask(() -> {
                    NpcDialogEvent translatedEvent = new TranslatedNpcDialogEvent(translatedComponents, e.getType());
                    WynntilsMod.postEvent(translatedEvent);
                });
            });
        } else {
            // We must also pass on the null event to clear the dialogue
            NpcDialogEvent translatedEvent = new TranslatedNpcDialogEvent(List.of(), e.getType());
            WynntilsMod.postEvent(translatedEvent);
        }
        if (!keepOriginal) {
            e.setCanceled(true);
        }
    }

    private String unwrapCoding(String origCoded) {
        return origCoded.replaceAll("\\{ ?§ ?([0-9a-fklmnor]) ?\\}", "§$1");
    }

    private String wrapCoding(String origCoded) {
        return origCoded.replaceAll("(§[0-9a-fklmnor])", "{$1}");
    }

    private static class TranslatedNpcDialogEvent extends NpcDialogEvent {
        protected TranslatedNpcDialogEvent(List<Component> chatMsg, NpcDialogueType type) {
            super(chatMsg, type);
        }
    }
}
