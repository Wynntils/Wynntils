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
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

    private static final Pattern LEVEL_UP_1 = Pattern.compile("^§6.* is now (?:combat )?level .*(?: in §.*)?$");
    private static final Pattern LEVEL_UP_2 =
            Pattern.compile("^§8\\[§r§7!§r§8\\] §r§7Congratulations to §r.* for reaching (combat )?§r§flevel .*!$");

    private static final Pattern LOGIN_ANNOUNCEMENT =
            Pattern.compile("^§.\\[§r§.([A-Z+]+)§r§.\\] §r§.(.*)§r§. has just logged in!$");

    private static final Pattern UNUSED_POINTS_1 =
            Pattern.compile("You have (\\d+) unused Skill Points?! Right-Click while holding your compass to use them");
    private static final Pattern UNUSED_POINTS_2 = Pattern.compile(
            "You have (\\d+) unused Ability Points?! Right-Click while holding your compass to use them");
    private static final Pattern UNUSED_POINTS_3 = Pattern.compile(
            "You have (\\d+) unused Skill Points? and (\\d+) unused Ability Points?! Right-Click while holding your compass to use them");

    private static final Pattern FRIEND_JOIN_PATTERN = Pattern.compile(
            "§a(§o)?(?<name>.+)§r§2 has logged into server §r§a(?<server>.+)§r§2 as §r§aan? (?<class>.+)");
    private static final Pattern FRIEND_LEAVE_PATTERN = Pattern.compile("§a(?<name>.+) left the game.");

    private static final Pattern NO_TOOL_DURABILITY_PATTERN = Pattern.compile(
            "^Your tool has 0 durability left! You will not receive any new resources until you repair it at a Blacksmith.$");
    private static final Pattern NO_CRAFTED_DURABILITY_PATTERN = Pattern.compile(
            "^Your items are damaged and have become less effective. Bring them to a Blacksmith to repair them.$");

    private static final Pattern BACKGROUND_WELCOME_1 = Pattern.compile("^ +§6§lWelcome to Wynncraft!$");
    private static final Pattern BACKGROUND_WELCOME_2 =
            Pattern.compile("^ +§fplay.wynncraft.com §7-/-§f wynncraft.com$");

    private static final Pattern BACKGROUND_SYSTEM_INFO = Pattern.compile("^(§r§8)?\\[Info\\] .*$");

    private static final Pattern BACKGROUND_SOUL_POINT_1 =
            Pattern.compile("^(§r§8)?As the sun rises, you feel a little bit safer...$");
    private static final Pattern BACKGROUND_SOUL_POINT_2 = Pattern.compile("^§r§7\\[(\\+\\d+ Soul Points?)\\]$");

    private static final Pattern BACKGROUND_LEVEL_UP_1 =
            Pattern.compile("^(?:§r§8)?.* is now (?:combat )?level .*(?: in §.*)?$");
    private static final Pattern BACKGROUND_LEVEL_UP_2 =
            Pattern.compile("^(§r§8)?\\[!\\] Congratulations to §r.* for reaching (combat )?§r§7level .*!$");

    private static final Pattern BACKGROUND_LOGIN_ANNOUNCEMENT =
            Pattern.compile("^(§r§8)?\\[§r§7([A-Z+]+)§r§8\\] §r§7(.*)§r§8 has just logged in!$");

    private static final Pattern BACKGROUND_FRIEND_JOIN_PATTERN = Pattern.compile(
            "§r§7(§o)?(?<name>.+)§r§8(§o)? has logged into server §r§7(§o)?(?<server>.+)§r§8(§o)? as §r§7(§o)?an? (?<class>.+)");
    private static final Pattern BACKGROUND_FRIEND_LEAVE_PATTERN = Pattern.compile("§r§7(?<name>.+) left the game.");

    @Config
    private boolean hideWelcome = true;

    @Config
    private boolean hideSystemInfo = true;

    @Config
    private boolean hideLevelUp = true;

    @Config
    private FilterType loginAnnouncements = FilterType.REDIRECT;

    @Config
    private FilterType soulPoint = FilterType.REDIRECT;

    @Config
    private FilterType unusedPoints = FilterType.REDIRECT;

    @Config
    private FilterType friendJoin = FilterType.REDIRECT;

    @Config
    private FilterType toolDurability = FilterType.REDIRECT;

    @Config
    private FilterType craftedDurability = FilterType.REDIRECT;

    @SubscribeEvent
    public void onInfoMessage(ChatMessageReceivedEvent e) {
        if (e.getRecipientType() != RecipientType.INFO) return;

        String msg = e.getOriginalCodedMessage();
        String uncoloredMsg = ComponentUtils.stripFormatting(msg);
        MessageType messageType = e.getMessageType();

        if (messageType == MessageType.NORMAL) {
            if (hideSystemInfo) {
                if (SYSTEM_INFO.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (loginAnnouncements != FilterType.KEEP) {
                Matcher matcher = LOGIN_ANNOUNCEMENT.matcher(msg);
                if (matcher.find()) {
                    e.setCanceled(true);
                    if (loginAnnouncements == FilterType.HIDE) {
                        return;
                    }

                    String playerName = matcher.group(2);
                    String rank = matcher.group(1);

                    sendLoginMessage(playerName, rank);
                    return;
                }
            }

            if (hideWelcome) {
                if (WELCOME_1.matcher(msg).find() || WELCOME_2.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }
            }

            if (friendJoin != FilterType.KEEP) {
                Matcher matcher = FRIEND_JOIN_PATTERN.matcher(msg);
                if (matcher.find()) {
                    e.setCanceled(true);
                    if (friendJoin == FilterType.HIDE) {
                        return;
                    }

                    String playerName = matcher.group("name");
                    String server = matcher.group("server");
                    String playerClass = matcher.group("class");

                    sendFriendJoinMessage(playerName, server, playerClass);
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

            if (soulPoint != FilterType.KEEP) {
                if (SOUL_POINT_1.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }

                Matcher m = SOUL_POINT_2.matcher(msg);
                if (m.find()) {
                    e.setCanceled(true);
                    if (soulPoint == FilterType.HIDE) {
                        return;
                    }

                    // Send the matching part, which could be +1 Soul Point or +2 Soul Points, etc.
                    NotificationManager.queueMessage(ChatFormatting.LIGHT_PURPLE + m.group(1));
                    return;
                }
            }

            if (unusedPoints != FilterType.KEEP) {

                Matcher matcher = UNUSED_POINTS_1.matcher(uncoloredMsg);

                int unusedSkillPoints = 0;
                int unusedAbilityPoints = 0;
                if (matcher.matches()) {
                    unusedSkillPoints = Integer.parseInt(matcher.group(1));
                }

                matcher = UNUSED_POINTS_2.matcher(uncoloredMsg);
                if (matcher.matches()) {
                    unusedAbilityPoints = Integer.parseInt(matcher.group(2));
                }

                matcher = UNUSED_POINTS_3.matcher(uncoloredMsg);
                if (matcher.matches()) {
                    unusedSkillPoints = Integer.parseInt(matcher.group(1));
                    unusedAbilityPoints = Integer.parseInt(matcher.group(2));
                }

                if (unusedPoints == FilterType.REDIRECT) {
                    if (unusedSkillPoints != 0) {
                        NotificationManager.queueMessage(Component.literal("You have ")
                                .withStyle(ChatFormatting.DARK_RED)
                                .append(Component.literal(String.valueOf(unusedSkillPoints))
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.DARK_RED))
                                .append(Component.literal(" unused skill points")
                                        .withStyle(ChatFormatting.DARK_RED)));
                    }

                    if (unusedAbilityPoints != 0) {
                        NotificationManager.queueMessage(Component.literal("You have ")
                                .withStyle(ChatFormatting.DARK_AQUA)
                                .append(Component.literal(String.valueOf(unusedAbilityPoints))
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.DARK_AQUA))
                                .append(Component.literal(" unused ability points")
                                        .withStyle(ChatFormatting.DARK_AQUA)));
                    }
                }

                if (unusedPoints != FilterType.KEEP && (unusedAbilityPoints != 0 || unusedSkillPoints != 0)) {
                    e.setCanceled(true);
                }
            }

            if (friendJoin != FilterType.KEEP) {
                Matcher matcher = FRIEND_LEAVE_PATTERN.matcher(msg);
                if (matcher.find()) {
                    e.setCanceled(true);
                    if (friendJoin == FilterType.HIDE) {
                        return;
                    }

                    String playerName = matcher.group("name");

                    sendFriendLeaveMessage(playerName);
                    return;
                }
            }

            if (toolDurability != FilterType.KEEP) {
                Matcher matcher = NO_TOOL_DURABILITY_PATTERN.matcher(uncoloredMsg);
                if (matcher.find()) {
                    e.setCanceled(true);
                    if (toolDurability == FilterType.HIDE) {
                        return;
                    }

                    NotificationManager.queueMessage(
                            Component.literal("Your tool has 0 durability!").withStyle(ChatFormatting.DARK_RED));

                    return;
                }
            }

            if (craftedDurability != FilterType.KEEP) {
                Matcher matcher = NO_CRAFTED_DURABILITY_PATTERN.matcher(uncoloredMsg);
                if (matcher.find()) {
                    e.setCanceled(true);
                    if (craftedDurability == FilterType.HIDE) {
                        return;
                    }

                    NotificationManager.queueMessage(
                            Component.literal("Your items are damaged.").withStyle(ChatFormatting.DARK_RED));

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

            if (loginAnnouncements != FilterType.KEEP) {
                Matcher matcher = BACKGROUND_LOGIN_ANNOUNCEMENT.matcher(msg);
                if (matcher.find()) {
                    e.setCanceled(true);
                    if (loginAnnouncements == FilterType.HIDE) {
                        return;
                    }

                    String playerName = matcher.group(3);
                    String rank = matcher.group(2);

                    sendLoginMessage(playerName, rank);
                    return;
                }
            }

            if (soulPoint != FilterType.KEEP) {
                if (BACKGROUND_SOUL_POINT_1.matcher(msg).find()) {
                    e.setCanceled(true);
                    return;
                }

                Matcher m = BACKGROUND_SOUL_POINT_2.matcher(msg);
                if (m.find()) {
                    e.setCanceled(true);
                    if (soulPoint == FilterType.HIDE) {
                        return;
                    }

                    // Send the matching part, which could be +1 Soul Point or +2 Soul Points, etc.
                    NotificationManager.queueMessage(ChatFormatting.LIGHT_PURPLE + m.group(1));
                    return;
                }
            }

            if (friendJoin != FilterType.KEEP) {
                Matcher matcher = BACKGROUND_FRIEND_JOIN_PATTERN.matcher(msg);
                if (matcher.find()) {
                    e.setCanceled(true);
                    if (friendJoin == FilterType.HIDE) {
                        return;
                    }

                    String playerName = matcher.group("name");
                    String server = matcher.group("server");
                    String playerClass = matcher.group("class");

                    sendFriendJoinMessage(playerName, server, playerClass);
                    return;
                }

                matcher = BACKGROUND_FRIEND_LEAVE_PATTERN.matcher(msg);
                if (matcher.find()) {
                    e.setCanceled(true);
                    if (friendJoin == FilterType.HIDE) {
                        return;
                    }

                    String playerName = matcher.group("name");

                    sendFriendLeaveMessage(playerName);
                    return;
                }
            }
        }
    }

    private void sendFriendLeaveMessage(String playerName) {
        NotificationManager.queueMessage(Component.literal("← ")
                .withStyle(ChatFormatting.RED)
                .append(Component.literal(playerName).withStyle(ChatFormatting.DARK_GREEN)));
    }

    private static void sendFriendJoinMessage(String playerName, String server, String playerClass) {
        NotificationManager.queueMessage(Component.literal("→ ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(playerName + " [")
                        .withStyle(ChatFormatting.DARK_GREEN)
                        .append(Component.literal(server + "/" + playerClass)
                                .withStyle(ChatFormatting.GREEN)
                                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GREEN)))));
    }

    private static void sendLoginMessage(String playerName, String rank) {
        ChatFormatting primary;
        ChatFormatting secondary;
        switch (rank) {
            case "VIP" -> {
                primary = ChatFormatting.DARK_GREEN;
                secondary = ChatFormatting.GREEN;
            }
            case "VIP+" -> {
                primary = ChatFormatting.DARK_AQUA;
                secondary = ChatFormatting.AQUA;
            }
            case "HERO" -> {
                primary = ChatFormatting.DARK_PURPLE;
                secondary = ChatFormatting.LIGHT_PURPLE;
            }
            case "CHAMPION" -> {
                primary = ChatFormatting.YELLOW;
                secondary = ChatFormatting.GOLD;
            }
            default -> {
                return;
            }
        }

        NotificationManager.queueMessage(ChatFormatting.GREEN + "→ " + primary + "[" + secondary + rank + primary + "] "
                + secondary + playerName);
    }

    public enum FilterType {
        KEEP,
        HIDE,
        REDIRECT
    }
}
