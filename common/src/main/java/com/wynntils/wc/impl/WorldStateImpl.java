/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.impl;

import com.wynntils.mc.event.ConnectionEvent.ConnectedEvent;
import com.wynntils.mc.event.ConnectionEvent.DisconnectedEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerDisplayNameChangeEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerLogOutEvent;
import com.wynntils.mc.event.PlayerInfoFooterChangedEvent;
import com.wynntils.mc.event.PlayerTeleportEvent;
import com.wynntils.mc.event.ResourcePackEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.Utils;
import com.wynntils.wc.event.WorldStateEvent;
import com.wynntils.wc.model.WorldState;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldStateImpl implements WorldState {
    private static final Pattern WORLD_NAME = Pattern.compile("^§f  §lGlobal \\[(.*)\\]$");
    private static final Pattern HUB_NAME = Pattern.compile("^\n§6§l play.wynncraft.com \n$");
    private static final Position CHARACTER_SELECTION_POSITION = new Vec3(-1337.5, 16.2, -1120.5);
    private static final UUID WORLD_UUID = UUID.fromString("16ff7452-714f-3752-b3cd-c3cb2068f6af");
    private static final String WYNNCRAFT_SERVER_SUFFIX = ".wynncraft.com";
    private static final String WYNNCRAFT_BETA_PREFIX = "beta.";

    private String currentTabListFooter = "";
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
        Utils.getEventBus().post(new WorldStateEvent(newState, oldState, newWorldName));
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
            Component displayName = e.getDisplayName();
            String name = StringUtils.getUnformatted(displayName);
            Matcher m = WORLD_NAME.matcher(name);
            if (m.find()) {
                String worldName = m.group(1);
                setState(State.WORLD, worldName);
            } else {
                Utils.logUnknown("World name not matching pattern", name);
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
            Utils.logUnknown("Got connected event while already connected to server", e);
            currentState = State.NOT_CONNECTED;
            currentWorldName = "";
        }

        String host = e.getHost().toLowerCase(Locale.ROOT);
        if (host.endsWith(WYNNCRAFT_SERVER_SUFFIX)) {
            onBetaServer = host.startsWith(WYNNCRAFT_BETA_PREFIX);
            setState(State.CONNECTING, "");
            currentTabListFooter = "";
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
        if (footer.equals(currentTabListFooter)) return;

        currentTabListFooter = footer;

        if (footer.length() > 0) {
            if (HUB_NAME.matcher(footer).find()) {
                setState(State.HUB, "");
            }
        }
    }

    @SubscribeEvent
    public void onTeleport(PlayerTeleportEvent e) {
        if (!onServer()) return;

        if (e.getNewPosition().equals(CHARACTER_SELECTION_POSITION)) {
            setState(State.CHARACTER_SELECTION, "");
        }
    }
}
