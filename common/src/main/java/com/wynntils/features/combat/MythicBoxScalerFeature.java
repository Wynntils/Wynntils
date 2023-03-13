/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigInfo;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.GroundItemEntityTransformEvent;
import com.wynntils.utils.wynn.WynnItemMatchers;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class MythicBoxScalerFeature extends UserFeature {
    @ConfigInfo
    private float scale = 1.5f;

    @SubscribeEvent
    public void onItemRendering(GroundItemEntityTransformEvent e) {
        if (!WynnItemMatchers.isMythicBox(e.getItemStack())) return;

        PoseStack stack = e.getPoseStack();

        // Essentially vanilla has a line that translate by 0.25 times the model scale
        // However, we want to factor in our own custom scale
        // Since this code runs after the model scale is applied, we first do
        // -0.25 to remove the original translation and then multiply our scale before
        // reapplying the transformation
        stack.translate(0f, -0.25f, 0f);

        stack.scale(scale, scale, scale);

        stack.translate(0f, 0.25f, 0f);
    }
}
