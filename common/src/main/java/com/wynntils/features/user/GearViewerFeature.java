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
import com.wynntils.wynn.utils.WynnPlayerUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
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
        LocalPlayer player = McUtils.player();

        Vec3 start = player.getEyePosition(1f);
        Vec3 look = player.getLookAngle();
        Vec3 direction = start.add(look.x * RAYCAST_RANGE, look.y * RAYCAST_RANGE, look.z * RAYCAST_RANGE);
        AABB bb = player.getBoundingBox()
                .expandTowards(look.x * RAYCAST_RANGE, look.y * RAYCAST_RANGE, look.z * RAYCAST_RANGE)
                .expandTowards(1, 1, 1);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                McUtils.mc().level, player, start, direction, bb, (e) -> e instanceof Player);

        if (hitResult == null) return;

        Player hitPlayer = (Player) hitResult.getEntity();

        if (hitPlayer.getScoreboardName().contains("§")) return; // npc
        if (WynnPlayerUtils.isPlayerGhost(hitPlayer)) return;

        McUtils.mc().setScreen(new GearViewerScreen((Player) hitResult.getEntity()));
    }
}
