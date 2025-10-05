/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.mod;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.mod.event.WynncraftConnectionEvent;
import com.wynntils.mc.event.ConnectionEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.screens.loading.LoadingScreen;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class ConnectionManager extends Manager {
    private static final String TRANSFER_REASON = "disconnect.transfer";
    private static final Pattern WYNNCRAFT_SERVER_PATTERN =
            Pattern.compile("^(?:(.*)\\.)?wynncraft\\.(?:com|net|org)$");

    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private String connectedHost = null;

    public ConnectionManager() {
        super(List.of());
    }

    public boolean onServer() {
        return connectionState == ConnectionState.CONNECTED;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onConnecting(ConnectionEvent.ConnectingEvent e) {
        if (connectionState != ConnectionState.DISCONNECTED) {
            WynntilsMod.error("Got connecting event while already connected to server: " + e);
            // fall through to see if the new server is Wynncraft, but send disconnect event first
            doDisconnect();
        }

        String host = e.getHost().toLowerCase(Locale.ROOT);
        Matcher matcher = WYNNCRAFT_SERVER_PATTERN.matcher(host);
        if (matcher.matches()) {
            String rawHostName = matcher.group(1);
            String hostName = (rawHostName == null) ? "play" : rawHostName.toLowerCase(Locale.ROOT);

            doConnecting(hostName);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onConnected(ConnectionEvent.ConnectedEvent e) {
        if (connectionState != ConnectionState.CONNECTING) return;
        if (connectedHost == null) return;

        doConnected();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDisconnected(ConnectionEvent.DisconnectedEvent e) {
        // If we are transferred to another server, it acts as a disconnect but
        // we should ignore it.
        if (e.getReason().equals(TRANSFER_REASON)) return;

        doDisconnect();
    }

    // ScreenOpenedEvent.Pre is used, because it is always posted
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenOpened(ScreenOpenedEvent.Pre e) {
        if (e.getOldScreen() instanceof ConnectScreen || e.getOldScreen() instanceof LoadingScreen) {
            if (e.getScreen() instanceof JoinMultiplayerScreen || e.getScreen() instanceof TitleScreen) {
                // This is the only way we get notified if the user aborts while connecting
                doDisconnect();
            }
        }

        // Sometimes the only notice we get about a disconnect is through
        // a screen. Typically this happens if a connection fails before
        // it is fully connected.
        if ((!(e.getScreen() instanceof DisconnectedScreen disconnectedScreen))) return;

        String reason = disconnectedScreen.details.reason().getContents() instanceof TranslatableContents tc
                ? tc.getKey()
                : "unknown";
        // If we are transferred to another server, it acts as a disconnect but
        // we should ignore it.
        if (reason.equals(TRANSFER_REASON)) return;

        doDisconnect();
    }

    private void doConnecting(String hostName) {
        connectionState = ConnectionState.CONNECTING;
        connectedHost = hostName;
        WynntilsMod.postEvent(new WynncraftConnectionEvent.Connecting(connectedHost));
    }

    private void doConnected() {
        connectionState = ConnectionState.CONNECTED;
        WynntilsMod.postEvent(new WynncraftConnectionEvent.Connected(connectedHost));
    }

    private void doDisconnect() {
        ConnectionState oldState = connectionState;
        String oldHostName = connectedHost;
        connectionState = ConnectionState.DISCONNECTED;
        connectedHost = null;
        if (oldState == ConnectionState.CONNECTED) {
            WynntilsMod.postEvent(new WynncraftConnectionEvent.Disconnected(oldHostName));
        } else if (oldState == ConnectionState.CONNECTING) {
            WynntilsMod.postEvent(new WynncraftConnectionEvent.ConnectingAborted(oldHostName));
        }
    }

    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED;
    }
}
