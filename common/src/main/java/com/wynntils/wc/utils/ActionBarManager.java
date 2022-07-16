/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.PacketEvent.PacketReceivedEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ActionBarManager {

    private static final Pattern ACTIONBAR_PATTERN =
            Pattern.compile("§c❤ (\\d+)/(\\d+)§0\\s+(.+)\\s+§b✺ (\\d+)/(\\d+)");

    private static String previousActionBar = null;

    private static int currentHealth = 100;
    private static int maxHealth = 100;
    private static int currentMana = 20;
    private static int maxMana = 20;

    // The server doesn't send SetActionBarPackets, instead it sends ChatPackets with the ChatType GAME_INFO
    @SubscribeEvent
    public static void onActionBarUpdate(PacketReceivedEvent<ClientboundChatPacket> e) {
        if (!WynnUtils.onWorld()) return;

        ClientboundChatPacket packet = e.getPacket();
        if (packet.getType() != ChatType.GAME_INFO) return;

        String actionBar = packet.getMessage().getString();
        // don't parse unchanged actionbar
        if (actionBar.equals(previousActionBar)) return;
        previousActionBar = actionBar;

        Matcher matcher = ACTIONBAR_PATTERN.matcher(actionBar);
        if (!matcher.matches()) return;

        currentHealth = Integer.parseInt(matcher.group(1));
        maxHealth = Integer.parseInt(matcher.group(2));
        currentMana = Integer.parseInt(matcher.group(4));
        maxMana = Integer.parseInt(matcher.group(5));

        String centerText = matcher.group(3);
        // TODO: parse partial spell progress from centerText
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
