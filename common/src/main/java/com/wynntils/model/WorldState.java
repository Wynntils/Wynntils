/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model;

import com.wynntils.WynntilsMod;
import com.wynntils.mc.McIf;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerDisplayNameChangeEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerLogOutEvent;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldState extends Model {
    private static final Pattern WORLD_NAME = Pattern.compile("^§f  §lGlobal \\[(.*)\\]$");
    private static final UUID WORLD_UUID = UUID.fromString("16ff7452-714f-3752-b3cd-c3cb2068f6af");

    @SubscribeEvent
    public static void remove(PlayerLogOutEvent e) {
        if (e.getId().equals(WORLD_UUID)) {
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
                String world = m.group(1);
                System.out.println("Joining world " + world);
            } else {
                WynntilsMod.logUnknown("World name not matching pattern", name);
            }
        }
    }
}
