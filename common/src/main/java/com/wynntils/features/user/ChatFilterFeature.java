/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wc.event.ChatMessageReceivedEvent;
import com.wynntils.wc.utils.WynnUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo
public class ChatFilterFeature extends UserFeature {
    private static final Pattern GLOBAL_CHAT =
            Pattern.compile("^§8\\[(Lv\\. )?\\d+\\*?/\\d+/..(/[^]]+)?\\]§r§7 \\[[A-Z0-9]+\\]§r.*");
    private static final Pattern LOCAL_CHAT = Pattern.compile("^§7\\[(Lv. )?\\d+\\*?/\\d+/..(/[^]]+)\\]§r.*");
    private static final Pattern PRIVATE_CHAT = Pattern.compile("^§7\\[§r§5.*§r§6 ➤ §r§5.*§r§7\\] §r§f.*");
    private static final Pattern SHOUT = Pattern.compile("^§3.* \\[[A-Z0-9]+\\] shouts: §r§b.*");

    private static final Pattern PRE_WELCOME_1 = Pattern.compile("^§7Loading Resource Pack...$");
    private static final Pattern PRE_WELCOME_2 =
            Pattern.compile("^§6Thank you for using the WynnPack. Enjoy the game!$");
    private static final Pattern PRE_WELCOME_3 = Pattern.compile(
            "^§cSelect a class! Each class is saved individually across all servers, you can come back at any time with /class and select another class!$");

    private static final Pattern WELCOME_1 = Pattern.compile("^ +§6§lWelcome to Wynncraft!$");
    private static final Pattern WELCOME_2 = Pattern.compile("^ +§fplay.wynncraft.com §7-/-§f wynncraft.com$");

    private static final Pattern SYSTEM_INFO = Pattern.compile("^(§r)?§.\\[Info\\] .*");
    private static final Pattern GUILD_INFO = Pattern.compile("^§3\\[INFO§3\\]§b (.*)$");

    private static final Pattern SOUL_POINT_1 = Pattern.compile("^§5As the sun rises, you feel a little bit safer...$");
    private static final Pattern SOUL_POINT_2 = Pattern.compile("^§d\\[(\\+\\d+ Soul Points?)\\]$");

    private static final Pattern LEVEL_UP_1 = Pattern.compile("^§6.* is now level .* in §.*");
    private static final Pattern LEVEL_UP_2 =
            Pattern.compile("^§8\\[§r§7!§r§8\\] §r§7Congratulations to §r.* for reaching (combat )?§r§flevel .*!$");

    private static final Pattern VIP_LOGIN =
            Pattern.compile("^§.\\[§r§.[A-Z+]+§r§.\\] §r§..*§r§. has just logged in!$");

    private static final Pattern BACKGROUND_GLOBAL_CHAT = Pattern.compile(
            "^(§r§8)?\\[(Lv\\. )?\\d+\\*?/\\d+/..(/[^]]+)?\\] \\[[A-Z0-9]+\\](§r§7)?( \\[(§k\\|)?§r§.[A-Z+]+§r§.(§k\\|§r§7)?\\])?(§r§7)? (§r§8)?.*");
    private static final Pattern BACKGROUND_LOCAL_CHAT = Pattern.compile("TODO_MISSING");
    private static final Pattern BACKGROUND_PRIVATE_CHAT = Pattern.compile("^§r§8\\[.* ➤ .*\\] §r§7.*");
    private static final Pattern BACKGROUND_SHOUT = Pattern.compile("^§r§8.* \\[[A-Z0-9]+\\] shouts: §r§7.*");

    private static final Pattern BACKGROUND_WELCOME_1 = Pattern.compile("^ +§6§lWelcome to Wynncraft!$");
    private static final Pattern BACKGROUND_WELCOME_2 =
            Pattern.compile("^ +§fplay.wynncraft.com §7-/-§f wynncraft.com$");

    private static final Pattern BACKGROUND_SYSTEM_INFO = Pattern.compile("^§r§8\\[Info\\] .*");
    private static final Pattern BACKGROUND_GUILD_INFO = Pattern.compile("TODO_MISSING");

    private static final Pattern BACKGROUND_SOUL_POINT_1 = Pattern.compile("TODO_MISSING");
    private static final Pattern BACKGROUND_SOUL_POINT_2 = Pattern.compile("TODO_MISSING");

    private static final Pattern BACKGROUND_LEVEL_UP_1 = Pattern.compile("TODO_MISSING");
    private static final Pattern BACKGROUND_LEVEL_UP_2 =
            Pattern.compile("^§r§8\\[!\\] Congratulations to §r.* for reaching (combat )?§r§7level .*!$");

    private static final Pattern BACKGROUND_VIP_LOGIN =
            Pattern.compile("^§r§8\\[§r§7[A-Z+]+§r§8\\] §r§7.*§r§8 has just logged in!$");

    @Config
    private boolean hideWelcome = true;

    @Config
    private boolean hideSystemInfo = true;

    @Config
    private boolean hideLevelUp = true;

    @Config
    private boolean hideVipLogin = true;

    @Config
    private boolean redirectSoulPoint = true;

    @Config
    private boolean redirectGuildInfo = true;

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent e) {
        if (!WynnUtils.onServer()) return;

        String msg = ComponentUtils.getFormatted(e.getMessage());

        System.out.println("We got chat1: " + msg + "system:" + e.getType());

        if (e.getType() == ChatMessageReceivedEvent.MessageType.NORMAL) {
            if (GLOBAL_CHAT.matcher(msg).find()) {
                handleGlobalChat(e);
                return;
            }
            if (LOCAL_CHAT.matcher(msg).find()) {
                handleLocalChat(e);
                return;
            }
            if (PRIVATE_CHAT.matcher(msg).find()) {
                handlePrivateChat(e);
                return;
            }
            if (SHOUT.matcher(msg).find()) {
                handleShout(e);
                return;
            }

            if (hideSystemInfo) {
                if (SYSTEM_INFO.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (hideWelcome) {
                if (WELCOME_1.matcher(msg).find() || WELCOME_2.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (redirectGuildInfo) {
                Matcher m = GUILD_INFO.matcher(msg);
                if (m.find()) {
                    e.setCanceled(true);
                    NotificationManager.queueMessage(ComponentUtils.stripFormattingCodes(m.group(1)));
                    return;
                }
            }

            System.out.println("UNHANDLED_NORMAL: " + msg);
            e.setMessage(new TextComponent("UNHANDLED_NORMAL: ").append(e.getMessage()));
        } else if (e.getType() == ChatMessageReceivedEvent.MessageType.SYSTEM) {
            if (hideLevelUp) {
                if (LEVEL_UP_1.matcher(msg).find() || LEVEL_UP_2.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (hideWelcome) {
                if (PRE_WELCOME_1.matcher(msg).find()
                        || PRE_WELCOME_2.matcher(msg).find()
                        || PRE_WELCOME_3.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (hideVipLogin) {
                if (VIP_LOGIN.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (redirectSoulPoint) {
                if (SOUL_POINT_1.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }

                Matcher m = SOUL_POINT_2.matcher(msg);
                if (m.find()) {
                    e.setCanceled(true);
                    // Send the matching part, which could be +1 Soul Point or +2 Soul Points, etc.
                    NotificationManager.queueMessage(m.group(1));
                    return;
                }
            }

            System.out.println("UNHANDLED_SYSTEM: " + msg);
            e.setMessage(new TextComponent("UNHANDLED_SYSTEM: ").append(e.getMessage()));
        } else if (e.getType() == ChatMessageReceivedEvent.MessageType.BACKGROUND) {
            System.out.println("BACKGROUND: " + msg);
            e.setMessage(new TextComponent("BACKGROUND: ").append(e.getMessage()));

            if (BACKGROUND_GLOBAL_CHAT.matcher(msg).find()) {
                handleGlobalChat(e);
                return;
            }
            if (BACKGROUND_LOCAL_CHAT.matcher(msg).find()) {
                handleLocalChat(e);
                return;
            }
            if (BACKGROUND_PRIVATE_CHAT.matcher(msg).find()) {
                handlePrivateChat(e);
                return;
            }
            if (BACKGROUND_SHOUT.matcher(msg).find()) {
                handleShout(e);
                return;
            }

            if (hideSystemInfo) {
                if (BACKGROUND_SYSTEM_INFO.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (redirectGuildInfo) {
                Matcher m = BACKGROUND_GUILD_INFO.matcher(msg);
                if (m.find()) {
                    e.setCanceled(true);
                    NotificationManager.queueMessage(ComponentUtils.stripFormattingCodes(m.group(1)));
                    return;
                }
            }
            if (hideLevelUp) {
                if (BACKGROUND_LEVEL_UP_1.matcher(msg).find()
                        || BACKGROUND_LEVEL_UP_2.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (hideWelcome) {
                if (BACKGROUND_WELCOME_1.matcher(msg).find()
                        || BACKGROUND_WELCOME_2.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (hideVipLogin) {
                if (BACKGROUND_VIP_LOGIN.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (redirectSoulPoint) {
                if (BACKGROUND_SOUL_POINT_1.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }

                Matcher m = BACKGROUND_SOUL_POINT_2.matcher(msg);
                if (m.find()) {
                    e.setCanceled(true);
                    // Send the matching part, which could be +1 Soul Point or +2 Soul Points, etc.
                    NotificationManager.queueMessage(m.group(1));
                    return;
                }
            }

            System.out.println("UNHANDLED_BACKGROUND: " + msg);
            e.setMessage(new TextComponent("UNHANDLED_BACKGROUND: ").append(e.getMessage()));
        }
    }

    private void handleShout(ChatMessageReceivedEvent e) {
        e.setMessage(new TextComponent("SHOUT: ").append(e.getMessage()));
    }

    private void handlePrivateChat(ChatMessageReceivedEvent e) {
        e.setMessage(new TextComponent("PRIVATE: ").append(e.getMessage()));
    }

    private void handleLocalChat(ChatMessageReceivedEvent e) {
        e.setMessage(new TextComponent("LOCAL: ").append(e.getMessage()));
    }

    private void handleGlobalChat(ChatMessageReceivedEvent e) {
        e.setMessage(new TextComponent("GLOBAL: ").append(e.getMessage()));
    }
}
