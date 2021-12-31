/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model;

import com.wynntils.WynntilsMod;
import com.wynntils.mc.McIf;
import com.wynntils.mc.event.ConnectionEvent.ConnectedEvent;
import com.wynntils.mc.event.ConnectionEvent.DisconnectedEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerDisplayNameChangeEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerLogOutEvent;
import com.wynntils.mc.event.PlayerInfoFooterChangedEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldState extends Model {
    private static final Pattern WORLD_NAME = Pattern.compile("^§f  §lGlobal \\[(.*)\\]$");
    private static final Pattern HUB_NAME = Pattern.compile("^\n§6§l play.wynncraft.com\n$");
    private static final UUID WORLD_UUID = UUID.fromString("16ff7452-714f-3752-b3cd-c3cb2068f6af");

    private static String currentWorldName = "";

    @SubscribeEvent
    public static void remove(PlayerLogOutEvent e) {
        if (e.getId().equals(WORLD_UUID) && currentWorldName.length() > 0) {
            System.out.println("Leaving current world");
        }
    }

    @SubscribeEvent
    public static void update(PlayerDisplayNameChangeEvent e) {
        if (e.getId().equals(WORLD_UUID)) {
            Component nameComponent = e.getDisplayName();
            String name = McIf.getUnformattedText(nameComponent);
            Matcher m = WORLD_NAME.matcher(name);
            if (m.find()) {
                String worldName = m.group(1);
                if (worldName.equals(currentWorldName)) return;

                System.out.println("Joining world " + worldName);
            } else {
                WynntilsMod.logUnknown("World name not matching pattern", name);
            }
        }
    }

    @SubscribeEvent
    public static void screenOpened(ScreenOpenedEvent e) {
        if (e.getScreen() instanceof DisconnectedScreen) {
            System.out.println("Disconnected from server (GUI)");
        }
    }

    @SubscribeEvent
    public static void disconnected(DisconnectedEvent e) {
        System.out.println("Disconnected from server (Connection)");
    }

    @SubscribeEvent
    public static void connected(ConnectedEvent e) {
        System.out.println("Connecting to " + e.getHost());
    }

    @SubscribeEvent
    public static void onTabListFooter(PlayerInfoFooterChangedEvent e) {
        String footer = e.getFooter();
        if (footer.length() > 0) {
            if (HUB_NAME.matcher(footer).find()) {
                System.out.println("Joined the Hub");
            }
        }
    }
}
