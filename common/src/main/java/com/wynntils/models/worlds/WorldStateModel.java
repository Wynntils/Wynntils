/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.mod.event.WynncraftConnectionEvent;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerDisplayNameChangeEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerLogOutEvent;
import com.wynntils.mc.event.PlayerInfoFooterChangedEvent;
import com.wynntils.mc.event.PlayerTeleportEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.PosUtils;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class WorldStateModel extends Model {
    private static final UUID WORLD_NAME_UUID = UUID.fromString("16ff7452-714f-3752-b3cd-c3cb2068f6af");
    private static final Pattern WORLD_NAME = Pattern.compile("^§f {2}§lGlobal \\[(.*)\\]$");
    private static final Pattern HUB_NAME = Pattern.compile("^\n§6§l play.wynncraft.com \n$");
    private static final Position CHARACTER_SELECTION_POSITION = new PositionImpl(-1337.5, 16.2, -1120.5);
    private static final Pattern WYNNCRAFT_SERVER_PATTERN = Pattern.compile("^(.*)\\.wynncraft\\.(?:com|net|org)$");
    private static final String WYNNCRAFT_BETA_NAME = "beta";
    private static final StyledText CHARACTER_SELECTION_TITLE = StyledText.fromString("§8§lSelect a Character");

    private StyledText currentTabListFooter = StyledText.EMPTY;
    private String currentWorldName = "";
    private long serverJoinTimestamp = 0;
    private boolean onBetaServer;
    private boolean hasJoinedAnyWorld = false;

    public WorldStateModel() {
        super(List.of());
    }

    private WorldState currentState = WorldState.NOT_CONNECTED;

    public boolean onWorld() {
        return currentState == WorldState.WORLD;
    }

    public boolean isInStream() {
        return currentWorldName.equals("-");
    }

    public boolean isOnBetaServer() {
        return onBetaServer;
    }

    public WorldState getCurrentState() {
        return currentState;
    }

    private void setState(WorldState newState, String newWorldName, boolean isFirstJoinWorld) {
        if (newState == currentState && newWorldName.equals(currentWorldName)) return;

        WorldState oldState = currentState;
        // Switch state before sending event
        currentState = newState;
        currentWorldName = newWorldName;
        if (newState == WorldState.WORLD) {
            serverJoinTimestamp = System.currentTimeMillis();
        }
        WynntilsMod.postEvent(new WorldStateEvent(newState, oldState, newWorldName, isFirstJoinWorld));
    }

    private void setState(WorldState newState) {
        setState(newState, "", false);
    }

    @SubscribeEvent
    public void disconnected(WynncraftConnectionEvent.Disconnected e) {
        setState(WorldState.NOT_CONNECTED);
    }

    @SubscribeEvent
    public void connecting(WynncraftConnectionEvent.Connected e) {
        if (currentState != WorldState.NOT_CONNECTED) {
            WynntilsMod.error("Got connected event while already connected to server: " + e.getHost());
            currentState = WorldState.NOT_CONNECTED;
            currentWorldName = "";
        }

        String host = e.getHost();
        onBetaServer = host.equals(WYNNCRAFT_BETA_NAME);
        setState(WorldState.CONNECTING);
        currentTabListFooter = StyledText.EMPTY;
    }

    @SubscribeEvent
    public void remove(PlayerLogOutEvent e) {
        if (e.getId().equals(WORLD_NAME_UUID) && !currentWorldName.isEmpty()) {
            setState(WorldState.INTERIM);
        }
    }

    @SubscribeEvent
    public void onTeleport(PlayerTeleportEvent e) {
        if (PosUtils.isSame(e.getNewPosition(), CHARACTER_SELECTION_POSITION)) {
            // We get here even if the character selection menu will not show up because of autojoin
            if (getCurrentState() != WorldState.CHARACTER_SELECTION) {
                // Sometimes the TP comes after the character selection menu, instead of before
                // Don't lose the CHARACTER_SELECTION state if that is the case
                setState(WorldState.INTERIM);
            }
        }
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent e) {
        if (e.getMenuType() == MenuType.GENERIC_9x3
                && StyledText.fromComponent(e.getTitle()).equals(CHARACTER_SELECTION_TITLE)) {
            setState(WorldState.CHARACTER_SELECTION);
        }
    }

    @SubscribeEvent
    public void onTabListFooter(PlayerInfoFooterChangedEvent e) {
        StyledText footer = e.getFooter();
        if (footer.equals(currentTabListFooter)) return;

        currentTabListFooter = footer;

        if (!footer.isEmpty()) {
            if (footer.getMatcher(HUB_NAME).find()) {
                setState(WorldState.HUB);
            }
        }
    }

    @SubscribeEvent
    public void update(PlayerDisplayNameChangeEvent e) {
        if (!e.getId().equals(WORLD_NAME_UUID)) return;

        Component displayName = e.getDisplayName();
        StyledText name = StyledText.fromComponent(displayName);
        Matcher m = name.getMatcher(WORLD_NAME);
        if (m.find()) {
            String worldName = m.group(1);
            setState(WorldState.WORLD, worldName, !hasJoinedAnyWorld);
            hasJoinedAnyWorld = true;
        }
    }

    public String getCurrentWorldName() {
        return currentWorldName;
    }

    public long getServerJoinTimestamp() {
        return serverJoinTimestamp;
    }
}
