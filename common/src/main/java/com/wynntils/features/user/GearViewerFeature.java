/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.gui.screens.GearViewerScreen;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.utils.RaycastUtils;
import com.wynntils.wynn.utils.WynnPlayerUtils;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class GearViewerFeature extends UserFeature {
    private static final float RAYCAST_RANGE = 5f;

    @RegisterKeyBind
    private final KeyBind gearViewerKeybind = new KeyBind(
            "View player's gear",
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            InputConstants.Type.MOUSE,
            true,
            this::tryOpenGearViewer);

    private void tryOpenGearViewer() {
        Optional<Player> hitPlayer = RaycastUtils.getHoveredPlayer();
        if (hitPlayer.isEmpty()) return;

        if (!WynnPlayerUtils.isLocalPlayer(hitPlayer.get())) return;

        McUtils.mc().setScreen(GearViewerScreen.create(hitPlayer.get()));
    }
}
