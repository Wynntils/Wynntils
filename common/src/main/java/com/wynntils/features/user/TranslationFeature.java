/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.services.TranslationManager;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.event.ChatMessageReceivedEvent;
import com.wynntils.wc.event.NpcDialogEvent;
import com.wynntils.wc.utils.WynnUtils;
import java.text.MessageFormat;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TranslationFeature extends UserFeature {
    public static TranslationFeature INSTANCE;

    @Config
    public String languageName = "sv";

    @Config
    public boolean translateTrackedQuest = true;

    @Config
    public boolean translateNpc = true;

    @Config
    public boolean translateInfo = false;

    @Config
    public boolean translatePlayerChat = false;

    @Config
    public boolean keepOriginal = true;

    @Config
    public TranslationManager.TranslationServices translationService = TranslationManager.TranslationServices.GOOGLEAPI;

    private void sendTranslation(String message, String prefix, String suffix, String formatted) {
        // We only want to translate the actual message, not formatting, sender, etc.
        TranslationManager.getTranslator().translate(message, languageName, translatedMsg -> {
            try {
                // Don't want translation to appear before original
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore
            }
            String strToSend;
            if (translatedMsg == null) {
                strToSend = MessageFormat.format("{0}{1}", TranslationManager.UNTRANSLATED_PREFIX, formatted);
            } else {
                strToSend = MessageFormat.format(
                        "{0}{1}{2}{3}", TranslationManager.TRANSLATED_PREFIX, prefix, translatedMsg, suffix);
            }
            // FIXME: In legacy, this was done as mc().addScheduledTask. We must probably do something similar
            McUtils.sendMessageToClient(new TextComponent(strToSend));
        });
    }

    @SubscribeEvent
    public void onChat(ChatMessageReceivedEvent e) {
        if (!WynnUtils.onServer()) return;

        String origCoded = e.getCodedMessage();
        String wrapped = wrapCoding(origCoded);
        // FIXME: preserve leading spaces
        // FIXME: preserve Wynntils coding
        TranslationManager.getTranslator().translate(wrapped, languageName, translatedMsg -> {
            String unwrapped = unwrapCoding(translatedMsg);
            System.out.println("ORIG:" + origCoded);
            System.out.println("WRAP:" + wrapped);
            System.out.println("TRAN:" + translatedMsg);
            System.out.println("UNWR:" + unwrapped);
            McUtils.mc().doRunTask(() -> {
                McUtils.sendMessageToClient(new TextComponent(unwrapped));
            });
        });
        if (!keepOriginal) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onNpcDialgue(NpcDialogEvent e) {
        if (!WynnUtils.onServer()) return;
        if (e instanceof TranslatedNpcDialogEvent) return;

        String origCoded = e.getCodedDialog();
        if (origCoded != null) {
            String wrapped = wrapCoding(origCoded);
            TranslationManager.getTranslator().translate(wrapped, languageName, translatedMsg -> {
                String unwrapped = unwrapCoding(translatedMsg);
                System.out.println("ORIG:" + origCoded);
                System.out.println("WRAP:" + wrapped);
                System.out.println("TRAN:" + translatedMsg);
                System.out.println("UNWR:" + unwrapped);
                McUtils.mc().doRunTask(() -> {
                    NpcDialogEvent translatedEvent = new TranslatedNpcDialogEvent(unwrapped);
                    WynntilsMod.getEventBus().post(translatedEvent);
                });
            });
        } else {
            // We must also pass on the null event to clear the dialogue
            NpcDialogEvent translatedEvent = new TranslatedNpcDialogEvent(null);
            WynntilsMod.getEventBus().post(translatedEvent);
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
        public TranslatedNpcDialogEvent(String codedDialog) {
            super(codedDialog);
        }
    }
}
