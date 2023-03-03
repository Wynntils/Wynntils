/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.map;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.TypeOverride;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.features.user.overlays.CustomBarsOverlayFeature;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.models.map.PoiLocation;
import com.wynntils.models.map.pois.CustomPoi;
import com.wynntils.screens.maps.MainMapScreen;
import com.wynntils.screens.maps.PoiCreationScreen;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.PointerType;
import com.wynntils.utils.render.type.TextShadow;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.MAP)
public class MapFeature extends UserFeature {
    public static MapFeature INSTANCE;

    @Config(visible = false)
    public List<CustomPoi> customPois = new ArrayList<>();

    @TypeOverride
    private final Type customPoisType = new TypeToken<List<CustomPoi>>() {}.getType();

    @Config
    public float poiFadeAdjustment = 0.4f;

    @Config
    public float combatPoiMinZoom = 0.166f;

    @Config
    public float cavePoiMinZoom = 0.28f;

    @Config
    public float servicePoiMinZoom = 0.8f;

    @Config
    public float fastTravelPoiMinZoom = 0.166f;

    @Config
    public float customPoiMinZoom = 0.28f;

    @Config
    public float lootChestTier1PoiMinZoom = 0.8f;

    @Config
    public float lootChestTier2PoiMinZoom = 0.8f;

    @Config
    public float lootChestTier3PoiMinZoom = 0.28f;

    @Config
    public float lootChestTier4PoiMinZoom = 0.28f;

    @Config
    public PointerType pointerType = PointerType.Arrow;

    @Config
    public CustomColor pointerColor = new CustomColor(1f, 1f, 1f, 1f);

    @Config
    public boolean renderUsingLinear = true;

    @Config
    public float playerPointerScale = 1.5f;

    @Config
    public float poiScale = 1f;

    @Config
    public boolean autoWaypointChests = true;

    @Config
    public ChestTier minTierForAutoWaypoint = ChestTier.TIER_3;

    @Config(subcategory = "Remote Players")
    public boolean renderRemoteFriendPlayers = true;

    @Config(subcategory = "Remote Players")
    public boolean renderRemotePartyPlayers = true;

    //    @Config(subcategory = "Remote Players")
    //    public boolean renderRemoteGuildPlayers = true;

    @Config(subcategory = "Remote Players")
    public CustomBarsOverlayFeature.HealthTexture remotePlayerHealthTexture = CustomBarsOverlayFeature.HealthTexture.a;

    @Config(subcategory = "Remote Players")
    public TextShadow remotePlayerNameShadow = TextShadow.OUTLINE;

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
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!autoWaypointChests) return;

        BlockEntity blockEntity = McUtils.mc().level.getBlockEntity(event.getPos());
        if (blockEntity != null && blockEntity.getType() == BlockEntityType.CHEST) {
            lastChestPos = event.getPos();
        }
    }

    @SubscribeEvent
    public void onScreenOpened(ScreenOpenedEvent.Post event) {
        if (!autoWaypointChests) return;
        if (lastChestPos == null) return;
        if (!(event.getScreen() instanceof ContainerScreen)) return;

        Matcher matcher = Models.Container.lootChestMatcher(event.getScreen());
        if (!matcher.matches()) return;

        ChestTier tier = ChestTier.fromString(matcher.group(1));

        if (tier.ordinal() < minTierForAutoWaypoint.ordinal()) return;

        PoiLocation location = new PoiLocation(lastChestPos.getX(), lastChestPos.getY(), lastChestPos.getZ());
        CustomPoi newPoi = new CustomPoi(
                location,
                tier.getWaypointName(),
                CommonColors.WHITE,
                tier.getWaypointTexture(),
                CustomPoi.Visibility.DEFAULT);

        if (customPois.stream().noneMatch(customPoi -> customPoi.equals(newPoi))) {
            customPois.add(newPoi);

            // TODO: Replace this notification with a popup
            Managers.Notification.queueMessage(Component.literal("Added new waypoint for " + tier.getWaypointName())
                    .withStyle(ChatFormatting.AQUA));

            Managers.Config.saveConfig();
        }
    }

    public enum ChestTier {
        TIER_1(Texture.CHEST_T1, "Loot Chest 1"),
        TIER_2(Texture.CHEST_T2, "Loot Chest 2"),
        TIER_3(Texture.CHEST_T3, "Loot Chest 3"),
        TIER_4(Texture.CHEST_T4, "Loot Chest 4");

        private final Texture waypointTexture;
        private final String waypointName;

        ChestTier(Texture waypointTexture, String waypointName) {
            this.waypointTexture = waypointTexture;
            this.waypointName = waypointName;
        }

        private Texture getWaypointTexture() {
            return waypointTexture;
        }

        private String getWaypointName() {
            return waypointName;
        }

        private static ChestTier fromString(String s) {
            return values()[MathUtils.integerFromRoman(s) - 1];
        }
    }
}
