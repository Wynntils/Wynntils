/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.services.TranslationManager;
import com.wynntils.mc.event.ChatReceivedEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.WynnUtils;
import java.text.MessageFormat;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TranslationFeature extends UserFeature {
    public static TranslationFeature INSTANCE;

    @Config
    public String languageName = "en";

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
    public boolean removeAccents = false;

    @Config
    public TranslationManager.TranslationServices translationService = TranslationManager.TranslationServices.PIGLATIN;

    private void sendTranslation(String message, String prefix, String suffix, String formatted) {
        // We only want to translate the actual message, not formatting, sender, etc.
        TranslationManager.getTranslator().translate(message, languageName, translatedMsg -> {
            try {
                // Don't want translation to appear before original
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore
            }
            if (removeAccents) {
                translatedMsg = org.apache.commons.lang3.StringUtils.stripAccents(translatedMsg);
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
    public void onChat(ChatReceivedEvent e) {
        if (!WynnUtils.onServer()) return;
        if (e.getType() == ChatType.GAME_INFO) return;

        // FIXME: this is very simplistic until we get the ChatManager
        String msg = ComponentUtils.getUnformatted(e.getMessage());
        sendTranslation(msg, "", "", "");
    }
}
