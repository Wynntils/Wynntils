/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.handlers.chat.type.RecipientType;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class InfoMessageFilterFeature extends Feature {
    private static final Pattern PRE_WELCOME_1 = Pattern.compile("^§7Loading Resource Pack...$");
    private static final Pattern PRE_WELCOME_2 =
            Pattern.compile("^§6Thank you for using the WynnPack. Enjoy the game!$");
    private static final Pattern PRE_WELCOME_3 = Pattern.compile(
            "^§cSelect a class! Each class is saved individually across all servers, you can come back at any time with /class and select another class!$");

    private static final Pattern WELCOME_1 = Pattern.compile("^ +§6§lWelcome to Wynncraft!$");
    private static final Pattern WELCOME_2 = Pattern.compile("^ +§fplay.wynncraft.com §7-/-§f wynncraft.com$");

    private static final Pattern SYSTEM_INFO = Pattern.compile("^(§r)?§.\\[Info\\] .*$");

    private static final Pattern LEVEL_UP_1 = Pattern.compile("^§6.* is now (?:combat )?level .*(?: in §.*)?$");
    private static final Pattern LEVEL_UP_2 =
            Pattern.compile("^§8\\[§r§7!§r§8\\] §r§7Congratulations to §r.* for reaching (combat )?§r§flevel .*!$");

    private static final Pattern BACKGROUND_WELCOME_1 = Pattern.compile("^ +§6§lWelcome to Wynncraft!$");
    private static final Pattern BACKGROUND_WELCOME_2 =
            Pattern.compile("^ +§fplay.wynncraft.com §7-/-§f wynncraft.com$");

    private static final Pattern BACKGROUND_SYSTEM_INFO = Pattern.compile("^(§r§8)?\\[Info\\] .*$");

    private static final Pattern BACKGROUND_LEVEL_UP_1 =
            Pattern.compile("^(?:§r§8)?.* is now (?:combat )?level .*(?: in §.*)?$");
    private static final Pattern BACKGROUND_LEVEL_UP_2 =
            Pattern.compile("^(§r§8)?\\[!\\] Congratulations to §r.* for reaching (combat )?§r§7level .*!$");

    @RegisterConfig
    public final Config<Boolean> hideWelcome = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> hideSystemInfo = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> hideLevelUp = new Config<>(false);

    @SubscribeEvent
    public void onInfoMessage(ChatMessageReceivedEvent e) {
        if (e.getRecipientType() != RecipientType.INFO) return;

        StyledText msg = e.getOriginalCodedMessage();
        MessageType messageType = e.getMessageType();

        if (messageType == MessageType.FOREGROUND) {
            if (hideSystemInfo.get()) {
                if (msg.match(SYSTEM_INFO).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (hideWelcome.get()) {
                if (msg.match(WELCOME_1).find() || msg.match(WELCOME_2).find()) {
                    e.setCanceled(true);
                    return;
                }
            }
            if (hideLevelUp.get()) {
                if (msg.match(LEVEL_UP_1).find() || msg.match(LEVEL_UP_2).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (hideWelcome.get()) {
                if (msg.match(PRE_WELCOME_1).find()
                        || msg.match(PRE_WELCOME_2).find()
                        || msg.match(PRE_WELCOME_3).find()) {
                    e.setCanceled(true);
                    return;
                }
            }
        } else if (messageType == MessageType.BACKGROUND) {
            if (hideSystemInfo.get()) {
                if (msg.match(BACKGROUND_SYSTEM_INFO).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (hideLevelUp.get()) {
                if (msg.match(BACKGROUND_LEVEL_UP_1).find()
                        || msg.match(BACKGROUND_LEVEL_UP_2).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (hideWelcome.get()) {
                if (msg.match(BACKGROUND_WELCOME_1).find()
                        || msg.match(BACKGROUND_WELCOME_2).find()) {
                    e.setCanceled(true);
                    return;
                }
            }
        }
    }
}
