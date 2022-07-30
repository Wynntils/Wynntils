/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.chat.MessageType;
import com.wynntils.core.chat.RecipientType;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.event.ChatMessageReceivedEvent;
import com.wynntils.wc.event.NpcDialogEvent;
import com.wynntils.wc.utils.WynnUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo
public class InfoMessageFilterFeature extends UserFeature {
    private static final Pattern PRE_WELCOME_1 = Pattern.compile("^§7Loading Resource Pack...$");
    private static final Pattern PRE_WELCOME_2 =
            Pattern.compile("^§6Thank you for using the WynnPack. Enjoy the game!$");
    private static final Pattern PRE_WELCOME_3 = Pattern.compile(
            "^§cSelect a class! Each class is saved individually across all servers, you can come back at any time with /class and select another class!$");

    private static final Pattern WELCOME_1 = Pattern.compile("^ +§6§lWelcome to Wynncraft!$");
    private static final Pattern WELCOME_2 = Pattern.compile("^ +§fplay.wynncraft.com §7-/-§f wynncraft.com$");

    private static final Pattern SYSTEM_INFO = Pattern.compile("^(§r)?§.\\[Info\\] .*$");

    private static final Pattern SOUL_POINT_1 = Pattern.compile("^§5As the sun rises, you feel a little bit safer...$");
    private static final Pattern SOUL_POINT_2 = Pattern.compile("^§d\\[(\\+\\d+ Soul Points?)\\]$");

    private static final Pattern LEVEL_UP_1 = Pattern.compile("^§6.* is now level .* in §.*$");
    private static final Pattern LEVEL_UP_2 =
            Pattern.compile("^§8\\[§r§7!§r§8\\] §r§7Congratulations to §r.* for reaching (combat )?§r§flevel .*!$");

    private static final Pattern VIP_LOGIN =
            Pattern.compile("^§.\\[§r§.[A-Z+]+§r§.\\] §r§..*§r§. has just logged in!$");

    private static final Pattern BACKGROUND_WELCOME_1 = Pattern.compile("^ +§6§lWelcome to Wynncraft!$");
    private static final Pattern BACKGROUND_WELCOME_2 =
            Pattern.compile("^ +§fplay.wynncraft.com §7-/-§f wynncraft.com$");

    private static final Pattern BACKGROUND_SYSTEM_INFO = Pattern.compile("^(§r§8)?\\[Info\\] .*$");

    private static final Pattern BACKGROUND_SOUL_POINT_1 =
            Pattern.compile("^(§r§8)?As the sun rises, you feel a little bit safer...$");
    private static final Pattern BACKGROUND_SOUL_POINT_2 = Pattern.compile("^§r§7\\[(\\+\\d+ Soul Points?)\\]$");

    private static final Pattern BACKGROUND_LEVEL_UP_1 = Pattern.compile("TODO_MISSING");
    private static final Pattern BACKGROUND_LEVEL_UP_2 =
            Pattern.compile("^(§r§8)?\\[!\\] Congratulations to §r.* for reaching (combat )?§r§7level .*!$");

    private static final Pattern BACKGROUND_VIP_LOGIN =
            Pattern.compile("^(§r§8)?\\[§r§7[A-Z+]+§r§8\\] §r§7.*§r§8 has just logged in!$");

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

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent e) {
        if (!WynnUtils.onServer()) return;
        if (e.getRecipientType() == RecipientType.INFO) return;

        RecipientType recipientType = e.getRecipientType();
        MessageType messageType = e.getMessageType();

        // TODO: This is a stand-in for per recipientType chat tabs
        e.setMessage(new TextComponent(messageType.name() + "-" + recipientType.name() + ": ").append(e.getMessage()));
    }

    @SubscribeEvent
    public void onNpcDialog(NpcDialogEvent e) {
        if (!WynnUtils.onServer()) return;

        List<String> codedDialogLines = e.getCodedDialogLines();
        if (codedDialogLines.isEmpty()) {
            McUtils.sendMessageToClient(new TextComponent("[NPC dialog removed]"));
            return;
        }

        for (String dialogLine : codedDialogLines) {
            McUtils.sendMessageToClient(new TextComponent("NPC: " + dialogLine));
        }
    }

    @SubscribeEvent
    public void onInfoMessage(ChatMessageReceivedEvent e) {
        if (!WynnUtils.onServer()) return;
        if (e.getRecipientType() != RecipientType.INFO) return;

        filterInfoMessage(e);

        if (!e.isCanceled()) {
            System.out.println("UNHANDLED-" + e.getMessageType().name() + ": " + e.getCodedMessage());
            e.setMessage(new TextComponent("UNHANDLED-" + e.getMessageType().name() + ": ").append(e.getMessage()));
        }
    }

    private void filterInfoMessage(ChatMessageReceivedEvent e) {
        String msg = e.getCodedMessage();
        MessageType messageType = e.getMessageType();

        if (messageType == MessageType.NORMAL) {
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
        } else if (messageType == MessageType.SYSTEM) {
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
        } else if (messageType == MessageType.BACKGROUND) {
            if (hideSystemInfo) {
                if (BACKGROUND_SYSTEM_INFO.matcher(msg).find()) {
                    e.setCanceled(true);
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
        }
    }
}
