/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.players;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.models.players.event.FriendsEvent;
import com.wynntils.models.players.event.PartyEvent;
import com.wynntils.screens.gearviewer.GearViewerScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.RaycastUtils;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.PLAYERS)
public class GearViewerFeature extends Feature {
    @RegisterKeyBind
    private final KeyBind gearViewerKeybind = new KeyBind(
            "View player's gear",
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            InputConstants.Type.MOUSE,
            true,
            this::tryOpenGearViewer);

    private GearViewerScreen gearViewerScreen = null;

    private void tryOpenGearViewer() {
        Optional<Player> hitPlayer = RaycastUtils.getHoveredPlayer();
        if (hitPlayer.isEmpty()) return;

        if (!Models.Player.isLocalPlayer(hitPlayer.get())) return;

        gearViewerScreen = (GearViewerScreen) GearViewerScreen.create(hitPlayer.get());
        McUtils.mc().setScreen(gearViewerScreen);
    }

    @SubscribeEvent
    public void onFriendAdded(FriendsEvent.Added e) {
        if (gearViewerScreen == null) return;
        gearViewerScreen.updateButtonIcons();
    }

    @SubscribeEvent
    public void onFriendRemoved(FriendsEvent.Removed e) {
        if (gearViewerScreen == null) return;
        gearViewerScreen.updateButtonIcons();
    }

    @SubscribeEvent
    public void onPartyOtherJoined(PartyEvent.OtherJoined e) {
        if (gearViewerScreen == null) return;
        gearViewerScreen.updateButtonIcons();
    }

    @SubscribeEvent
    public void onPartyOtherLeft(PartyEvent.OtherLeft e) {
        if (gearViewerScreen == null) return;
        gearViewerScreen.updateButtonIcons();
    }
}
