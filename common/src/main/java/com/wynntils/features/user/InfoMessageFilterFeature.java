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
    private static final Pattern NO_MANA_LEFT_TO_CAST_PATTERN =
            Pattern.compile("^§4You don't have enough mana to cast that spell!$");

    private static final Pattern HEALED_PATTERN = Pattern.compile("^.+ gave you §r§c\\[\\+(\\d+) ❤\\]$");

    private static final Pattern HEAL_PATTERN = Pattern.compile("^§r§c\\[\\+(\\d+) ❤\\]$");

    private static final Pattern SPEED_PATTERN = Pattern.compile("^\\+3 minutes speed boost.$");

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

    private static final Pattern BACKGROUND_HEALED_PATTERN = Pattern.compile("^.+ gave you §r§7§o\\[\\+(\\d+) ❤\\]$");

    private static final Pattern NO_ROOM_PATTERN = Pattern.compile("§4There is no room for a horse.");
    private static final Pattern HORSE_DESPAWNED_PATTERN =
            Pattern.compile("§dSince you interacted with your inventory, your horse has despawned.");

    private static final Pattern MAX_POTIONS_ALLOWED_PATTERN =
            Pattern.compile("§4You already are holding the maximum amount of potions allowed.");
    private static final Pattern LESS_POWERFUL_POTION_REMOVED_PATTERN =
            Pattern.compile("§7One less powerful potion was replaced to open space for the added one.");

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

    @Config
    private FilterType notEnoughMana = FilterType.REDIRECT;

    @Config
    private FilterType heal = FilterType.REDIRECT;

    @Config
    private FilterType speed = FilterType.REDIRECT;

    @Config
    private FilterType horse = FilterType.REDIRECT;

    @Config
    private FilterType potion = FilterType.REDIRECT;

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

            if (heal != FilterType.KEEP) {
                Matcher matcher = HEAL_PATTERN.matcher(msg);
                if (matcher.matches()) {
                    e.setCanceled(true);
                    if (heal == FilterType.HIDE) {
                        return;
                    }

                    String amount = matcher.group(1);

                    sendHealMessage(amount);
                    return;
                }

                matcher = HEALED_PATTERN.matcher(msg);
                if (matcher.matches()) {
                    e.setCanceled(true);
                    if (heal == FilterType.HIDE) {
                        return;
                    }

                    String amount = matcher.group(1);

                    sendHealMessage(amount);
                    return;
                }
            }

            if (speed != FilterType.KEEP) {
                Matcher matcher = SPEED_PATTERN.matcher(uncoloredMsg);
                if (matcher.matches()) {
                    e.setCanceled(true);
                    if (speed == FilterType.HIDE) {
                        return;
                    }

                    NotificationManager.queueMessage(new TextComponent("+3 minutes")
                            .withStyle(ChatFormatting.AQUA)
                            .append(new TextComponent(" speed boost").withStyle(ChatFormatting.GRAY)));
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
                        NotificationManager.queueMessage(new TextComponent("You have ")
                                .withStyle(ChatFormatting.DARK_RED)
                                .append(new TextComponent(String.valueOf(unusedSkillPoints))
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.DARK_RED))
                                .append(new TextComponent(" unused skill points").withStyle(ChatFormatting.DARK_RED)));
                    }

                    if (unusedAbilityPoints != 0) {
                        NotificationManager.queueMessage(new TextComponent("You have ")
                                .withStyle(ChatFormatting.DARK_AQUA)
                                .append(new TextComponent(String.valueOf(unusedAbilityPoints))
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.DARK_AQUA))
                                .append(new TextComponent(" unused ability points")
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

            if (horse != FilterType.KEEP) {
                Matcher roomMatcher = NO_ROOM_PATTERN.matcher(msg);
                if (roomMatcher.matches()) {
                    e.setCanceled(true);
                    if (horse == FilterType.HIDE) {
                        return;
                    }
                    NotificationManager.queueMessage(
                            new TextComponent("No room for a horse!").withStyle(ChatFormatting.DARK_RED));
                    return;
                }

                Matcher despawnMatcher = HORSE_DESPAWNED_PATTERN.matcher(msg);
                if (despawnMatcher.matches()) {
                    e.setCanceled(true);
                    if (horse == FilterType.HIDE) {
                        return;
                    }
                    NotificationManager.queueMessage(
                            new TextComponent("Your horse has despawned.").withStyle(ChatFormatting.DARK_PURPLE));
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
                            new TextComponent("Your tool has 0 durability!").withStyle(ChatFormatting.DARK_RED));

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
                            new TextComponent("Your items are damaged.").withStyle(ChatFormatting.DARK_RED));

                    return;
                }
            }

            if (notEnoughMana != FilterType.KEEP) {
                Matcher matcher = NO_MANA_LEFT_TO_CAST_PATTERN.matcher(msg);
                if (matcher.matches()) {
                    e.setCanceled(true);
                    if (notEnoughMana == FilterType.HIDE) {
                        return;
                    }

                    NotificationManager.queueMessage(
                            new TextComponent("Not enough mana to do that spell!").withStyle(ChatFormatting.DARK_RED));

                    return;
                }
            }

            if (potion != FilterType.KEEP) {
                Matcher maxPotionsMatcher = MAX_POTIONS_ALLOWED_PATTERN.matcher(msg);
                if (maxPotionsMatcher.matches()) {
                    e.setCanceled(true);
                    if (potion == FilterType.HIDE) {
                        return;
                    }

                    NotificationManager.queueMessage(
                            new TextComponent("At Potion Charge Limit!").withStyle(ChatFormatting.DARK_RED));

                    return;
                }

                Matcher lessPowerfulPotionMatcher = LESS_POWERFUL_POTION_REMOVED_PATTERN.matcher(msg);
                if (lessPowerfulPotionMatcher.matches()) {
                    e.setCanceled(true);
                    if (potion == FilterType.HIDE) {
                        return;
                    }

                    NotificationManager.queueMessage(
                            new TextComponent("Lesser potion replaced.").withStyle(ChatFormatting.GRAY));

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

            if (heal != FilterType.KEEP) {
                Matcher matcher = BACKGROUND_HEALED_PATTERN.matcher(msg);
                if (matcher.matches()) {
                    e.setCanceled(true);
                    if (heal == FilterType.HIDE) {
                        return;
                    }

                    String amount = matcher.group(1);

                    sendHealMessage(amount);
                    return;
                }
            }
        }
    }

    private void sendHealMessage(String amount) {
        NotificationManager.queueMessage(
                new TextComponent("[+%s ❤]".formatted(amount)).withStyle(ChatFormatting.DARK_RED));
    }

    private void sendFriendLeaveMessage(String playerName) {
        NotificationManager.queueMessage(new TextComponent("← ")
                .withStyle(ChatFormatting.RED)
                .append(new TextComponent(playerName).withStyle(ChatFormatting.DARK_GREEN)));
    }

    private static void sendFriendJoinMessage(String playerName, String server, String playerClass) {
        NotificationManager.queueMessage(new TextComponent("→ ")
                .withStyle(ChatFormatting.GREEN)
                .append(new TextComponent(playerName + " [")
                        .withStyle(ChatFormatting.DARK_GREEN)
                        .append(new TextComponent(server + "/" + playerClass)
                                .withStyle(ChatFormatting.GREEN)
                                .append(new TextComponent("]").withStyle(ChatFormatting.DARK_GREEN)))));
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
