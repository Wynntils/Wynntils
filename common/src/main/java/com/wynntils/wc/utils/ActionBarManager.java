/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.utils.StringUtils;
import com.wynntils.wc.event.ActionBarMessageUpdateEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ActionBarManager {
    private static final Pattern ACTIONBAR_PATTERN =
            StringUtils.compileCCRegex("§❤ ([0-9]+)/([0-9]+)§ +(.+?) +§✺ ([0-9]+)/([0-9]+)");

    private static Component previousMessage = null;

    private static int currentHealth = -1;
    private static int maxHealth = -1;
    private static int currentMana = -1;
    private static int maxMana = -1;

    @SubscribeEvent
    public static void onActionBarUpdate(ChatPacketReceivedEvent e) {
        if (!WynnUtils.onWorld() || e.getType() != ChatType.GAME_INFO) return;

        String actionBar = e.getMessage().getString();

        Matcher matcher = ACTIONBAR_PATTERN.matcher(actionBar);
        if (!matcher.matches()) return;

        currentHealth = Integer.parseInt(matcher.group(1));
        maxHealth = Integer.parseInt(matcher.group(2));
        currentMana = Integer.parseInt(matcher.group(4));
        maxMana = Integer.parseInt(matcher.group(5));

        if (previousMessage != null && actionBar.equals(previousMessage.getString())) return;

        ActionBarMessageUpdateEvent.ActionText actionText =
                new ActionBarMessageUpdateEvent.ActionText(matcher.group(3));
        ActionBarMessageUpdateEvent.HealthText healthText =
                new ActionBarMessageUpdateEvent.HealthText("§c❤ " + currentHealth + "/" + maxHealth);
        ActionBarMessageUpdateEvent.ManaText manaText =
                new ActionBarMessageUpdateEvent.ManaText("§b✺ " + currentMana + "/" + maxMana);

        WynntilsMod.getEventBus().post(actionText);
        WynntilsMod.getEventBus().post(healthText);
        WynntilsMod.getEventBus().post(manaText);

        MutableComponent modified = new TextComponent(healthText.getMessage())
                .append("    ")
                .append(actionText.getMessage())
                .append("    ")
                .append(manaText.getMessage());

        previousMessage = modified;

        e.setMessage(modified);
    }

    public static void init() {
        WynntilsMod.getEventBus().register(ActionBarManager.class);
    }

    public static int getCurrentHealth() {
        return currentHealth;
    }

    public static int getMaxHealth() {
        return maxHealth;
    }

    public static int getCurrentMana() {
        return currentMana;
    }

    public static int getMaxMana() {
        return maxMana;
    }
}
