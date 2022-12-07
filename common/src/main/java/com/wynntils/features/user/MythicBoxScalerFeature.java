/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.GroundItemEntityRenderEvent;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MythicBoxScalerFeature extends UserFeature {

    @Config
    private float scale = 1.5f;

    @SubscribeEvent
    public void onItemRendering(GroundItemEntityRenderEvent e) {
        if (!WynnItemMatchers.isMythic(e.getItemStack())) return;

        PoseStack stack = e.getPoseStack();

        stack.scale(scale, scale, 1f);

        // Direction is always null when passed into SkullBlockRenderer#renderSkull which causes a 0.5 translation in x
        // which we need to cancel out due to scaling
        stack.translate(-0.5 + 0.5 / scale, 0, 0);
    }
}
