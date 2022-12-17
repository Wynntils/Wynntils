/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.map;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.TypeOverride;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.core.managers.Models;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.screens.maps.MainMapScreen;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.model.map.poi.CustomPoi;
import com.wynntils.wynn.model.map.poi.PoiLocation;
import com.wynntils.wynn.objects.HealthTexture;
import com.wynntils.wynn.screens.WynnScreenMatchers;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(category = FeatureCategory.MAP)
public class MapFeature extends UserFeature {
    public static MapFeature INSTANCE;

    @Config(visible = false)
    public List<CustomPoi> customPois = new ArrayList<>();

    @TypeOverride
    private final Type customPoisType = new TypeToken<List<CustomPoi>>() {}.getType();

    @Config
    public float poiFadeDistance = 0.6f;

    @Config
    public float combatPoiMinZoom = 0.1f;

    @Config
    public float servicePoiMinZoom = 1f;

    @Config
    public float customPoiMinZoom = 0.1f;

    @Config
    public float lootChestTier1PoiMinZoom = 1f;

    @Config
    public float lootChestTier2PoiMinZoom = 1f;

    @Config
    public float lootChestTier3PoiMinZoom = 0.1f;

    @Config
    public float lootChestTier4PoiMinZoom = 0.1f;

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
    public HealthTexture remotePlayerHealthTexture = HealthTexture.a;

    @Config(subcategory = "Remote Players")
    public FontRenderer.TextShadow remotePlayerNameShadow = FontRenderer.TextShadow.OUTLINE;

    private BlockPos lastChestPos;

    @RegisterKeyBind
    public final KeyBind openMapKeybind = new KeyBind("Open Main Map", GLFW.GLFW_KEY_M, false, () -> {
        // If the current screen is already the map, and we get this event, this means we are holding the keybind
        // and should signal that we should close when the key is not held anymore.
        if (McUtils.mc().screen instanceof MainMapScreen mainMapScreen) {
            mainMapScreen.setHoldingMapKey(true);
            return;
        }

        McUtils.mc().setScreen(MainMapScreen.create());
    });

    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.Map);
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
    public void onScreenOpened(ScreenOpenedEvent event) {
        if (!autoWaypointChests) return;
        if (lastChestPos == null) return;
        if (!(event.getScreen() instanceof ContainerScreen)) return;

        Matcher matcher = WynnScreenMatchers.lootChestMatcher(event.getScreen());
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

        if (MapFeature.INSTANCE.customPois.stream().noneMatch(customPoi -> customPoi.equals(newPoi))) {
            MapFeature.INSTANCE.customPois.add(newPoi);

            // TODO: Replace this notification with a popup
            NotificationManager.queueMessage(new TextComponent("Added new waypoint for " + tier.getWaypointName())
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

        public Texture getWaypointTexture() {
            return waypointTexture;
        }

        public String getWaypointName() {
            return waypointName;
        }

        public static ChestTier fromString(String s) {
            return values()[MathUtils.integerFromRoman(s) - 1];
        }
    }
}
