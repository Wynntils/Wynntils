/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.mod.event.WynncraftConnectionEvent;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerDisplayNameChangeEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerLogOutEvent;
import com.wynntils.models.character.actionbar.segments.CharacterCreationSegment;
import com.wynntils.models.character.actionbar.segments.CharacterSelectionSegment;
import com.wynntils.models.worlds.actionbar.matchers.CharacterWardrobeSegmentMatcher;
import com.wynntils.models.worlds.actionbar.matchers.WynncraftVersionSegmentMatcher;
import com.wynntils.models.worlds.actionbar.segments.CharacterWardrobeSegment;
import com.wynntils.models.worlds.actionbar.segments.WynncraftVersionSegment;
import com.wynntils.models.worlds.bossbars.SkipCutsceneBar;
import com.wynntils.models.worlds.bossbars.StreamerModeBar;
import com.wynntils.models.worlds.event.CutsceneStartedEvent;
import com.wynntils.models.worlds.event.StreamModeEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.CutsceneState;
import com.wynntils.models.worlds.type.ServerRegion;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.models.worlds.type.WynncraftVersion;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;

public final class WorldStateModel extends Model {
    private static final UUID WORLD_NAME_UUID = UUID.fromString("16ff7452-714f-2752-b3cd-c3cb2068f6af");
    private static final Pattern WORLD_NAME = Pattern.compile("^§f {2}§lGlobal \\[(.*)\\]$");
    private static final Pattern HOUSING_NAME = Pattern.compile("^§f  §l([^§\"\\\\]{1,35})$");
    private static final Pattern HUB_NAME = Pattern.compile("^\n§6§l play.wynncraft.com \n$");
    private static final Pattern QUICK_CONNECT_PATTERN = Pattern.compile("§aQuick Connect");
    private static final String WYNNCRAFT_BETA_NAME = "beta";
    private static final String UNKNOWN_WORLD = "WC??";

    private static final SkipCutsceneBar skipCutsceneBar = new SkipCutsceneBar();
    private CutsceneState cutsceneState = CutsceneState.NOT_IN_CUTSCENE;

    private static final StreamerModeBar streamerModeBar = new StreamerModeBar();

    private String currentWorldName = "";
    private ServerRegion currentRegion = ServerRegion.WC;
    private long serverJoinTimestamp = 0;
    private boolean onBetaServer;
    private boolean hasJoinedAnyWorld = false;
    private boolean inStream = false;
    private boolean inCharacterWardrobe = false;
    private WynncraftVersion worldVersion = null;

    public WorldStateModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(new WynncraftVersionSegmentMatcher());
        Handlers.ActionBar.registerSegment(new CharacterWardrobeSegmentMatcher());
        Handlers.BossBar.registerBar(skipCutsceneBar);
        Handlers.BossBar.registerBar(streamerModeBar);
    }

    private WorldState currentState = WorldState.NOT_CONNECTED;

    public boolean onWorld() {
        return currentState == WorldState.WORLD;
    }

    public boolean inCharacterWardrobe() {
        return inCharacterWardrobe;
    }

    public boolean isInStream() {
        return inStream;
    }

    public boolean isOnBetaServer() {
        return onBetaServer;
    }

    public WynncraftVersion getWorldVersion() {
        return worldVersion;
    }

    public WorldState getCurrentState() {
        return currentState;
    }

    private void setState(WorldState newState, String newWorldName, boolean isFirstJoinWorld) {
        if (newState == currentState && newWorldName.equals(currentWorldName)) return;

        WynntilsMod.info("Changing world state to " + newState);
        cutsceneEnded();
        WorldState oldState = currentState;
        // Switch state before sending event
        currentState = newState;
        currentWorldName = newWorldName;
        if (newState == WorldState.WORLD) {
            serverJoinTimestamp = System.currentTimeMillis();
        }

        if (currentWorldName.length() >= 2) {
            String region = currentWorldName.substring(0, 2);
            currentRegion = ServerRegion.fromString(region);
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
    public void connectionAborted(WynncraftConnectionEvent.ConnectingAborted e) {
        setState(WorldState.NOT_CONNECTED);
    }

    @SubscribeEvent
    public void connecting(WynncraftConnectionEvent.Connecting e) {
        if (currentState != WorldState.NOT_CONNECTED) {
            WynntilsMod.error("Got connected event while already connected to server: " + e.getHost());
            currentState = WorldState.NOT_CONNECTED;
            currentWorldName = "";
        }

        String host = e.getHost();
        onBetaServer = host.equals(WYNNCRAFT_BETA_NAME);
        setState(WorldState.CONNECTING);
    }

    @SubscribeEvent
    public void connected(WynncraftConnectionEvent.Connected e) {
        if (currentState != WorldState.CONNECTING) {
            WynntilsMod.error("Got connected event without getting connecting event to server: " + e.getHost());
            currentState = WorldState.CONNECTING;
            currentWorldName = "";
        }

        setState(WorldState.INTERIM);
    }

    @SubscribeEvent
    public void remove(PlayerLogOutEvent e) {
        if (e.getId().equals(WORLD_NAME_UUID) && !currentWorldName.isEmpty()) {
            setState(WorldState.INTERIM);
        }
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresent(CharacterCreationSegment.class, this::onCharacterCreation);
        event.runIfPresent(CharacterSelectionSegment.class, this::onCharacterSelection);
        event.runIfPresent(WynncraftVersionSegment.class, this::setWorldVersion);
        inCharacterWardrobe = false;
        event.runIfPresent(CharacterWardrobeSegment.class, this::onCharacterWardrobe);
    }

    @SubscribeEvent
    public void onContainerSetEvent(ContainerSetContentEvent.Post e) {
        if (e.getContainerId() != McUtils.inventoryMenu().containerId) return;
        ItemStack firstHotbarSlot = e.getItems().get(36);

        if (firstHotbarSlot.getItem().equals(Items.COMPASS)) {
            StyledText name = StyledText.fromComponent(firstHotbarSlot.getHoverName());
            if (name.matches(QUICK_CONNECT_PATTERN)) {
                setState(WorldState.HUB);
                return;
            }
        }

        if (currentState == WorldState.HUB) {
            setState(WorldState.INTERIM);
        }
    }

    private void onCharacterCreation(CharacterCreationSegment segment) {
        setState(WorldState.CHARACTER_SELECTION);
    }

    private void onCharacterSelection(CharacterSelectionSegment segment) {
        setState(WorldState.CHARACTER_SELECTION);
    }

    private void setWorldVersion(WynncraftVersionSegment segment) {
        worldVersion = segment.getWynncraftVersion();
    }

    private void onCharacterWardrobe(CharacterWardrobeSegment segment) {
        inCharacterWardrobe = true;
    }

    @SubscribeEvent
    public void update(PlayerDisplayNameChangeEvent e) {
        if (!e.getId().equals(WORLD_NAME_UUID)) return;
        if (inStream) return;

        Component displayName = e.getDisplayName();
        StyledText name = StyledText.fromComponent(displayName);
        Matcher m = name.getMatcher(WORLD_NAME);
        if (setWorldIfMatched(m, false)) return;
        // must check in this order as housing name regex matches anything that WORLD_NAME would match, housing names
        // need to exclude world names.
        Matcher housingNameMatcher = name.getMatcher(HOUSING_NAME);
        setWorldIfMatched(housingNameMatcher, true);
    }

    private boolean setWorldIfMatched(Matcher m, boolean housing) {
        if (m.find()) {
            String worldName = housing ? currentWorldName : m.group(1);
            if (worldName.isEmpty() && housing) {
                worldName = UNKNOWN_WORLD;
                WynntilsMod.warn("Changed world via housing join, current world name is unknown");
            }
            setState(WorldState.WORLD, worldName, !hasJoinedAnyWorld);
            hasJoinedAnyWorld = true;
            Models.Housing.updateHousingState(housing, housing ? m.group(1) : "");
            return true;
        }
        return false;
    }

    public void setStreamerMode(boolean inStream) {
        this.inStream = inStream;
        WynntilsMod.postEvent(new StreamModeEvent(inStream));
    }

    public void cutsceneStarted(boolean groupCutscene) {
        if (cutsceneState == CutsceneState.NOT_IN_CUTSCENE) {
            cutsceneState = CutsceneState.IN_CUTSCENE;

            CutsceneStartedEvent event = new CutsceneStartedEvent(groupCutscene);
            WynntilsMod.postEvent(event);

            if (event.isCanceled()) {
                cutsceneState = CutsceneState.SKIPPED_CUTSCENE;
            }
        }
    }

    public void cutsceneEnded() {
        cutsceneState = CutsceneState.NOT_IN_CUTSCENE;
    }

    /**
     * @return Full name of the current world, such as "NA32"
     */
    public String getCurrentWorldName() {
        return currentWorldName;
    }

    public ServerRegion getCurrentServerRegion() {
        return currentRegion;
    }

    public long getServerJoinTimestamp() {
        return serverJoinTimestamp;
    }
}
