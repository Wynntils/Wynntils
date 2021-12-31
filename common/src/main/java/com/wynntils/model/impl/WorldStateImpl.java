/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.impl;

import com.wynntils.WynntilsMod;
import com.wynntils.mc.McIf;
import com.wynntils.mc.event.ConnectionEvent.ConnectedEvent;
import com.wynntils.mc.event.ConnectionEvent.DisconnectedEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerDisplayNameChangeEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerLogOutEvent;
import com.wynntils.mc.event.PlayerInfoFooterChangedEvent;
import com.wynntils.mc.event.ResourcePackEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.model.Models;
import com.wynntils.model.WorldState;
import com.wynntils.model.event.WorldStateEvent;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldStateImpl extends Models implements WorldState {
    private static final Pattern WORLD_NAME = Pattern.compile("^§f  §lGlobal \\[(.*)\\]$");
    private static final Pattern HUB_NAME = Pattern.compile("^\n§6§l play.wynncraft.com\n$");
    private static final UUID WORLD_UUID = UUID.fromString("16ff7452-714f-3752-b3cd-c3cb2068f6af");
    private static final String WYNNCRAFT_SERVER_SUFFIX = ".wynncraft.com";
    private static final String WYNNCRAFT_BETA_PREFIX = "beta.";

    private String currentWorldName = "";
    private boolean onBetaServer;

    private State currentState = State.NOT_CONNECTED;

    @Override
    public boolean onServer() {
        return currentState != State.NOT_CONNECTED;
    }

    @Override
    public boolean onWorld() {
        return currentState == State.WORLD;
    }

    @Override
    public boolean isInStream() {
        return currentWorldName.equals("-");
    }

    @Override
    public boolean isOnBetaServer() {
        return onBetaServer;
    }

    @Override
    public State getCurrentState() {
        return currentState;
    }

    private void setState(State newState, String newWorldName) {
        if (newState == currentState && newWorldName.equals(currentWorldName)) return;

        State oldState = currentState;
        // Switch state before sending event
        currentState = newState;
        currentWorldName = newWorldName;
        WynntilsMod.EVENT_BUS.post(new WorldStateEvent(newState, oldState, newWorldName));
    }

    @SubscribeEvent
    public void remove(PlayerLogOutEvent e) {
        if (!onServer()) return;

        if (e.getId().equals(WORLD_UUID) && currentWorldName.length() > 0) {
            setState(State.INTERIM, "");
        }
    }

    @SubscribeEvent
    public void update(PlayerDisplayNameChangeEvent e) {
        if (!onServer()) return;

        if (e.getId().equals(WORLD_UUID)) {
            Component nameComponent = e.getDisplayName();
            String name = McIf.getUnformattedText(nameComponent);
            Matcher m = WORLD_NAME.matcher(name);
            if (m.find()) {
                String worldName = m.group(1);
                setState(State.WORLD, worldName);
            } else {
                WynntilsMod.logUnknown("World name not matching pattern", name);
            }
        }
    }

    @SubscribeEvent
    public void screenOpened(ScreenOpenedEvent e) {
        if (!onServer()) return;

        if (e.getScreen() instanceof DisconnectedScreen) {
            setState(State.NOT_CONNECTED, "");
        }
    }

    @SubscribeEvent
    public void disconnected(DisconnectedEvent e) {
        if (!onServer()) return;

        setState(State.NOT_CONNECTED, "");
    }

    @SubscribeEvent
    public void connecting(ConnectedEvent e) {
        if (onServer()) {
            WynntilsMod.logUnknown("Got connection while connected", e);
            currentState = State.NOT_CONNECTED;
            currentWorldName = "";
        }

        String host = e.getHost().toLowerCase(Locale.ROOT);
        if (host.endsWith(WYNNCRAFT_SERVER_SUFFIX)) {
            onBetaServer = host.startsWith(WYNNCRAFT_BETA_PREFIX);
            setState(State.CONNECTING, "");
        }
    }

    @SubscribeEvent
    public void onResourcePack(ResourcePackEvent e) {
        if (!onServer()) return;

        setState(State.INTERIM, "");
    }

    @SubscribeEvent
    public void onTabListFooter(PlayerInfoFooterChangedEvent e) {
        if (!onServer()) return;

        String footer = e.getFooter();
        if (footer.length() > 0) {
            if (HUB_NAME.matcher(footer).find()) {
                setState(State.HUB, "");
            }
        }
    }
}
