/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.GroundItemEntityTransformEvent;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MythicBoxScalerFeature extends UserFeature {

    @Config
    private float scale = 2f;

    @SubscribeEvent
    public void onItemRendering(GroundItemEntityTransformEvent e) {
        if (!WynnItemMatchers.isMythicBox(e.getItemStack())) return;

        PoseStack stack = e.getPoseStack();

        // Value of constant from ItemEntityRenderer#render
        // cancel out old scale y transform and later replace it after scaling
        stack.translate(0f, -0.25f, 0f);

        stack.scale(scale, scale, scale);

        stack.translate(0f, 0.25f, 0f);
    }
}
