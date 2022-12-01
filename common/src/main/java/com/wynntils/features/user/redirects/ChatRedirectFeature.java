/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.redirects;

import com.wynntils.core.chat.MessageType;
import com.wynntils.core.chat.RecipientType;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import com.wynntils.wynn.utils.WynnPlayerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo
public class ChatRedirectFeature extends UserFeature {
    private static final Pattern UNUSED_POINTS_1 =
            Pattern.compile("You have (\\d+) unused Skill Points?! Right-Click while holding your compass to use them");
    private static final Pattern UNUSED_POINTS_2 = Pattern.compile(
            "You have (\\d+) unused Ability Points?! Right-Click while holding your compass to use them");
    private static final Pattern UNUSED_POINTS_3 = Pattern.compile(
            "You have (\\d+) unused Skill Points? and (\\d+) unused Ability Points?! Right-Click while holding your compass to use them");

    private static final Pattern MAX_POTIONS_ALLOWED_PATTERN =
            Pattern.compile("§4You already are holding the maximum amount of potions allowed.");
    private static final Pattern LESS_POWERFUL_POTION_REMOVED_PATTERN =
            Pattern.compile("§7One less powerful potion was replaced to open space for the added one.");

    @Config
    private RedirectAction loginAnnouncements = RedirectAction.REDIRECT;

    @Config
    private RedirectAction soulPoint = RedirectAction.REDIRECT;

    @Config
    private RedirectAction unusedPoints = RedirectAction.REDIRECT;

    @Config
    private RedirectAction friendJoin = RedirectAction.REDIRECT;

    @Config
    private RedirectAction toolDurability = RedirectAction.REDIRECT;

    @Config
    private RedirectAction craftedDurability = RedirectAction.REDIRECT;

    @Config
    private RedirectAction notEnoughMana = RedirectAction.REDIRECT;

    @Config
    private RedirectAction heal = RedirectAction.REDIRECT;

    @Config
    private RedirectAction speed = RedirectAction.REDIRECT;

    @Config
    private RedirectAction horse = RedirectAction.REDIRECT;

    @Config
    private RedirectAction potion = RedirectAction.REDIRECT;

    @Config
    private RedirectAction shaman = RedirectAction.REDIRECT;

    private final List<Redirector> redirectors = new ArrayList<>();

    public ChatRedirectFeature() {
        register(new CraftedDurabilityRedirector());
        register(new FriendJoinRedirector());
        register(new FriendLeaveRedirector());
        register(new HealRedirector());
        register(new HealedByOtherRedirector());
        register(new HorseDespawnedRedirector());
        register(new HorseSpawnFailRedirector());
        register(new LoginRedirector());
        register(new ManaDeficitRedirector());
        register(new NoTotemRedirector());
        register(new SoulPointDiscarder());
        register(new SoulPointRedirector());
        register(new SpeedBoostRedirector());
        register(new ToolDurabilityRedirector());
    }

    private void register(Redirector redirector) {
        redirectors.add(redirector);
    }

    @SubscribeEvent
    public void onInfoMessage(ChatMessageReceivedEvent e) {
        if (e.getRecipientType() != RecipientType.INFO) return;

        String msg = e.getOriginalCodedMessage();
        MessageType messageType = e.getMessageType();

        for (Redirector redirector : redirectors) {
            RedirectAction action = redirector.getAction();
            if (action == RedirectAction.KEEP) continue;

            Matcher matcher;
            Pattern pattern = redirector.getPattern(messageType);
            // Ideally we will get rid of those "uncolored" patterns
            Pattern uncoloredPattern = redirector.getUncoloredSystemPattern();
            if (messageType == MessageType.SYSTEM && uncoloredPattern != null) {
                matcher = uncoloredPattern.matcher(ComponentUtils.stripFormatting(msg));
            } else {
                if (pattern == null) continue;
                matcher = pattern.matcher(msg);
            }

            if (matcher.find()) {
                e.setCanceled(true);
                if (redirector.getAction() == RedirectAction.HIDE) continue;

                String notification = redirector.getNotification(matcher);
                if (notification == null) continue;

                NotificationManager.queueMessage(notification);
            }
        }

        if (potion != RedirectAction.KEEP) {
            Matcher maxPotionsMatcher = MAX_POTIONS_ALLOWED_PATTERN.matcher(msg);
            if (maxPotionsMatcher.matches()) {
                e.setCanceled(true);
                if (potion == RedirectAction.HIDE) {
                    return;
                }

                NotificationManager.queueMessage(
                        new TextComponent("At potion charge limit!").withStyle(ChatFormatting.DARK_RED));

                return;
            }

            Matcher lessPowerfulPotionMatcher = LESS_POWERFUL_POTION_REMOVED_PATTERN.matcher(msg);
            if (lessPowerfulPotionMatcher.matches()) {
                e.setCanceled(true);
                if (potion == RedirectAction.HIDE) {
                    return;
                }

                NotificationManager.queueMessage(
                        new TextComponent("Lesser potion replaced.").withStyle(ChatFormatting.GRAY));

                return;
            }
        }

        if (messageType == MessageType.SYSTEM && unusedPoints != RedirectAction.KEEP) {
            handleUnusedPoints(e, ComponentUtils.stripFormatting(msg));
        }
    }

    private void handleUnusedPoints(ChatMessageReceivedEvent e, String uncoloredMsg) {
        // This is a bit special and does not readily fit the Redirector model
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

        if (unusedPoints == RedirectAction.REDIRECT) {
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

        if (unusedPoints != RedirectAction.KEEP && (unusedAbilityPoints != 0 || unusedSkillPoints != 0)) {
            e.setCanceled(true);
        }
    }

    public enum RedirectAction {
        KEEP,
        HIDE,
        REDIRECT
    }

    private abstract static class Redirector {
        Pattern getPattern(MessageType messageType) {
            return switch (messageType) {
                case NORMAL -> getNormalPattern();
                case BACKGROUND -> getBackgroundPattern();
                case SYSTEM -> getSystemPattern();
            };
        }

        Pattern getSystemPattern() {
            return null;
        }

        Pattern getUncoloredSystemPattern() {
            return null;
        }
        Pattern getNormalPattern() {
            return null;
        }

        Pattern getBackgroundPattern() {
            return null;
        }

        abstract RedirectAction getAction();

        abstract String getNotification(Matcher matcher);
    }

    private class CraftedDurabilityRedirector extends Redirector {
        private static final Pattern UNCOLORED_SYSTEM_PATTERN = Pattern.compile(
                "^Your items are damaged and have become less effective. Bring them to a Blacksmith to repair them.$");

        @Override
        Pattern getUncoloredSystemPattern() {
            return UNCOLORED_SYSTEM_PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return craftedDurability;
        }

        @Override
        String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_RED + "Your items are damaged.";
        }
    }

    private class FriendJoinRedirector extends Redirector {
        private static final Pattern NORMAL_PATTERN = Pattern.compile(
                "§a(§o)?(?<name>.+)§r§2 has logged into server §r§a(?<server>.+)§r§2 as §r§aan? (?<class>.+)");
        private static final Pattern BACKGROUND_PATTERN = Pattern.compile(
                "§r§7(§o)?(?<name>.+)§r§8(§o)? has logged into server §r§7(§o)?(?<server>.+)§r§8(§o)? as §r§7(§o)?an? (?<class>.+)");

        @Override
        Pattern getNormalPattern() {
            return NORMAL_PATTERN;
        }

        @Override
        Pattern getBackgroundPattern() {
            return BACKGROUND_PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return friendJoin;
        }

        @Override
        String getNotification(Matcher matcher) {
            String playerName = matcher.group("name");
            String server = matcher.group("server");
            String playerClass = matcher.group("class");

            return ChatFormatting.GREEN + "→ " + ChatFormatting.DARK_GREEN
                    + playerName + " [" + ChatFormatting.GREEN
                    + server + "/" + playerClass + ChatFormatting.DARK_GREEN
                    + "]";
        }
    }

    private class FriendLeaveRedirector extends Redirector {
        private static final Pattern SYSTEM_PATTERN = Pattern.compile("§a(?<name>.+) left the game.");
        private static final Pattern BACKGROUND_PATTERN =
                Pattern.compile("§r§7(?<name>.+) left the game.");

        @Override
        Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        Pattern getBackgroundPattern() {
            return BACKGROUND_PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return friendJoin;
        }

        @Override
        String getNotification(Matcher matcher) {
            String playerName = matcher.group("name");

            return ChatFormatting.RED + "← " + ChatFormatting.DARK_GREEN + playerName;
        }
    }

    private class HealRedirector extends Redirector {
        private static final Pattern NORMAL_PATTERN = Pattern.compile("^§r§c\\[\\+(\\d+) ❤\\]$");

        @Override
        Pattern getNormalPattern() {
            return NORMAL_PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return heal;
        }

        @Override
        String getNotification(Matcher matcher) {
            String amount = matcher.group(1);

            return ChatFormatting.DARK_RED + "[+" + amount + " ❤]";
        }
    }

    private class HealedByOtherRedirector extends Redirector {
        private static final Pattern NORMAL_PATTERN = Pattern.compile("^.+ gave you §r§c\\[\\+(\\d+) ❤\\]$");
        private static final Pattern BACKGROUND_PATTERN = Pattern.compile("^.+ gave you §r§7§o\\[\\+(\\d+) ❤\\]$");

        @Override
        Pattern getNormalPattern() {
            return NORMAL_PATTERN;
        }

        @Override
        Pattern getBackgroundPattern() {
            return BACKGROUND_PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return heal;
        }

        @Override
        String getNotification(Matcher matcher) {
            String amount = matcher.group(1);

            return ChatFormatting.DARK_RED + "[+" + amount + " ❤]";
        }
    }

    private class HorseDespawnedRedirector extends Redirector {
        private static final Pattern SYSTEM_PATTERN =
                Pattern.compile("§dSince you interacted with your inventory, your horse has despawned.");

        @Override
        Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return horse;
        }

        @Override
        String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_PURPLE + "Your horse has despawned.";
        }
    }

    private class HorseSpawnFailRedirector extends Redirector {
        private static final Pattern SYSTEM_PATTERN = Pattern.compile("§4There is no room for a horse.");

        @Override
        Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return horse;
        }

        @Override
        String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_RED + "No room for a horse!";
        }
    }

    private class LoginRedirector extends Redirector {
        private static final Pattern NORMAL_PATTERN =
                Pattern.compile("^§.\\[§r§.([A-Z+]+)§r§.\\] §r§.(.*)§r§. has just logged in!$");
        private static final Pattern BACKGROUND_PATTERN =
                Pattern.compile("^(?:§r§8)?\\[§r§7([A-Z+]+)§r§8\\] §r§7(.*)§r§8 has just logged in!$");

        @Override
        Pattern getNormalPattern() {
            return NORMAL_PATTERN;
        }

        @Override
        Pattern getBackgroundPattern() {
            return BACKGROUND_PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return loginAnnouncements;
        }

        @Override
        String getNotification(Matcher matcher) {
            String rank = matcher.group(1);
            String playerName = matcher.group(2);

            return ChatFormatting.GREEN + "→ " + WynnPlayerUtils.getFormattedRank(rank) + playerName;
        }
    }

    private class ManaDeficitRedirector extends Redirector {
        private static final Pattern SYSTEM_PATTERN =
                Pattern.compile("^§4You don't have enough mana to cast that spell!$");

        @Override
        Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return notEnoughMana;
        }

        @Override
        String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_RED + "Not enough mana to do that spell!";
        }
    }

    private class NoTotemRedirector extends Redirector {
        private static final Pattern SYSTEM_PATTERN = Pattern.compile("§4You have no active totems near you$");

        @Override
        Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return shaman;
        }

        @Override
        String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_RED + "No totems nearby!";
        }
    }

    private class SoulPointDiscarder extends Redirector {
        private static final Pattern SYSTEM_PATTERN = Pattern.compile("^§5As the sun rises, you feel a little bit safer...$");
        private static final Pattern BACKGROUND_PATTERN =
                Pattern.compile("^(§r§8)?As the sun rises, you feel a little bit safer...$");

        @Override
        Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        Pattern getBackgroundPattern() {
            return BACKGROUND_PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return soulPoint;
        }

        @Override
        String getNotification(Matcher matcher) {
            // Soul point messages comes in two lines. We just throw away the chatty one
            // if we have hide or redirect as action.
            return null;
        }
    }

    private class SoulPointRedirector extends Redirector {
        private static final Pattern BACKGROUND_PATTERN = Pattern.compile("^§r§7\\[(\\+\\d+ Soul Points?)\\]$");
        private static final Pattern SYSTEM_PATTERN = Pattern.compile("^§d\\[(\\+\\d+ Soul Points?)\\]$");

        @Override
        Pattern getSystemPattern() {
            return SYSTEM_PATTERN;
        }

        @Override
        Pattern getBackgroundPattern() {
            return BACKGROUND_PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return soulPoint;
        }

        @Override
        String getNotification(Matcher matcher) {
            // Send the matching part, which could be +1 Soul Point or +2 Soul Points, etc.
            return ChatFormatting.LIGHT_PURPLE + matcher.group(1);
        }
    }

    private class SpeedBoostRedirector extends Redirector {
        private static final Pattern NORMAL_PATTERN = Pattern.compile("^\\+3 minutes speed boost.$");

        @Override
        Pattern getNormalPattern() {
            return NORMAL_PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return speed;
        }

        @Override
        String getNotification(Matcher matcher) {
            return ChatFormatting.AQUA + "+3 minutes" + ChatFormatting.GRAY + " speed boost";
        }
    }

    private class ToolDurabilityRedirector extends Redirector {
        private static final Pattern UNCOLORED_SYSTEM__PATTERN = Pattern.compile(
                "^Your tool has 0 durability left! You will not receive any new resources until you repair it at a Blacksmith.$");

        @Override
        Pattern getUncoloredSystemPattern() {
            return UNCOLORED_SYSTEM__PATTERN;
        }

        @Override
        RedirectAction getAction() {
            return toolDurability;
        }

        @Override
        String getNotification(Matcher matcher) {
            return ChatFormatting.DARK_RED + "Your tool has 0 durability!";
        }
    }
}
