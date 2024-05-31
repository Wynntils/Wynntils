/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.models.containers.containers.reward.LootChestContainer;
import com.wynntils.models.containers.type.LootChestTier;
import com.wynntils.screens.maps.MainMapScreen;
import com.wynntils.screens.maps.PoiCreationScreen;
import com.wynntils.services.map.pois.CustomPoi;
import com.wynntils.services.mapdata.providers.builtin.LootChestsProvider;
import com.wynntils.services.mapdata.providers.builtin.WaypointsProvider;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.type.HealthTexture;
import com.wynntils.utils.render.type.PointerType;
import com.wynntils.utils.render.type.TextShadow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.MAP)
public class MainMapFeature extends Feature {
    // Use userWaypoints or foundChestLocations instead
    // This config is to be kept as an "upfixer" to migrate old data
    @Deprecated
    @Persisted
    public final HiddenConfig<List<CustomPoi>> customPois = new HiddenConfig<>(new ArrayList<>());

    @Persisted
    private final Storage<List<WaypointsProvider.WaypointLocation>> userWaypoints = new Storage<>(new ArrayList<>());

    @Persisted
    private final Storage<List<LootChestsProvider.FoundChestLocation>> foundChestLocations =
            new Storage<>(new ArrayList<>());

    @Persisted
    public final Config<Float> poiFadeAdjustment = new Config<>(0.4f);

    @Persisted
    public final Config<Float> combatPoiMinZoom = new Config<>(0.166f);

    @Persisted
    public final Config<Float> cavePoiMinZoom = new Config<>(0.28f);

    @Persisted
    public final Config<Float> servicePoiMinZoom = new Config<>(0.8f);

    @Persisted
    public final Config<Float> fastTravelPoiMinZoom = new Config<>(0.166f);

    @Persisted
    public final Config<Float> customPoiMinZoom = new Config<>(0.28f);

    @Persisted
    public final Config<PointerType> pointerType = new Config<>(PointerType.ARROW);

    @Persisted
    public final Config<CustomColor> pointerColor = new Config<>(new CustomColor(1f, 1f, 1f, 1f));

    @Persisted
    public final Config<Float> playerPointerScale = new Config<>(1.5f);

    @Persisted
    public final Config<Float> poiScale = new Config<>(1f);

    @Persisted
    public final Config<Boolean> centerWhenUnmapped = new Config<>(true);

    @Persisted
    public final Config<Boolean> autoWaypointChests = new Config<>(true);

    @Persisted
    public final Config<Boolean> renderRemoteFriendPlayers = new Config<>(true);

    @Persisted
    public final Config<Boolean> renderRemotePartyPlayers = new Config<>(true);

    @Persisted
    public final Config<HealthTexture> remotePlayerHealthTexture = new Config<>(HealthTexture.A);

    @Persisted
    public final Config<TextShadow> remotePlayerNameShadow = new Config<>(TextShadow.OUTLINE);

    @Persisted
    public final Config<Boolean> holdGuildMapOpen = new Config<>(true);

    private BlockPos lastChestPos;

    @RegisterKeyBind
    public final KeyBind openMapKeybind = new KeyBind("Open Main Map", GLFW.GLFW_KEY_M, false, this::openMainMap);

    @RegisterKeyBind
    public final KeyBind newWaypointKeybind =
            new KeyBind("New Waypoint", GLFW.GLFW_KEY_B, true, this::openWaypointSetup);

    private void openMainMap() {
        // If the current screen is already the map, and we get this event, this means we are holding the keybind
        // and should signal that we should close when the key is not held anymore.
        if (McUtils.mc().screen instanceof MainMapScreen mainMapScreen) {
            mainMapScreen.setHoldingMapKey(true);
            return;
        }

        McUtils.mc().setScreen(MainMapScreen.create());
    }

    private void openWaypointSetup() {
        PoiLocation location = new PoiLocation(
                McUtils.player().getBlockX(),
                McUtils.player().getBlockY(),
                McUtils.player().getBlockZ());

        McUtils.mc().setScreen(PoiCreationScreen.create(null, location));
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.InteractAt event) {
        if (!autoWaypointChests.get()) return;

        Entity entity = event.getEntityHitResult().getEntity();
        if (entity != null && entity.getType() == EntityType.SLIME) {
            // We don't actually know if this is a chest, but it's a good enough guess.
            lastChestPos = entity.blockPosition();
        }
    }

    @SubscribeEvent
    public void onScreenOpened(ScreenOpenedEvent.Post event) {
        if (!autoWaypointChests.get()) return;
        if (lastChestPos == null) return;
        if (!(event.getScreen() instanceof ContainerScreen)) return;

        if (!(Models.Container.getCurrentContainer() instanceof LootChestContainer)) {
            lastChestPos = null;
            return;
        }

        LootChestTier chestType = Models.LootChest.getChestType(event.getScreen());
        if (chestType == null) return;

        Location location = new Location(lastChestPos);

        if (foundChestLocations.get().stream()
                .noneMatch(foundLocation -> foundLocation.getLocation().equals(location))) {
            foundChestLocations.get().add(new LootChestsProvider.FoundChestLocation(location, chestType));
            foundChestLocations.touched();

            // TODO: Replace this notification with a popup
            Managers.Notification.queueMessage(
                    Component.literal("Added new waypoint for " + chestType.getWaypointName())
                            .withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        if (config == customPois) {
            updateWaypoints();
        }
    }

    @Deprecated
    // FIXME: This can be removed when all the callers of customPois are removed
    public void updateWaypoints() {
        // FIXME: WaypointsProvider.updateWaypoints();
        customPois.get().forEach(WaypointsProvider::registerFeature);
    }

    public List<WaypointsProvider.WaypointLocation> getUserWaypoints() {
        return Collections.unmodifiableList(userWaypoints.get());
    }

    public void addUserWaypoint(WaypointsProvider.WaypointLocation waypoint) {
        userWaypoints.get().add(waypoint);
        userWaypoints.touched();
        // FIXME: Add userWaypoints.updateWaypoints();
    }

    public void removeUserWaypoint(WaypointsProvider.WaypointLocation waypoint) {
        userWaypoints.get().remove(waypoint);
        userWaypoints.touched();
        // FIXME: Add userWaypoints.updateWaypoints();
    }

    public List<LootChestsProvider.FoundChestLocation> getFoundChestLocations() {
        return Collections.unmodifiableList(foundChestLocations.get());
    }

    public void addFoundChestLocation(LootChestsProvider.FoundChestLocation location) {
        foundChestLocations.get().add(location);
        foundChestLocations.touched();
        // FIXME: Add foundChestLocations.updateWaypoints();
    }

    public void removeFoundChestLocation(LootChestsProvider.FoundChestLocation location) {
        foundChestLocations.get().remove(location);
        foundChestLocations.touched();
        // FIXME: Add foundChestLocations.updateWaypoints();
    }
}
