/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.mc.event.BlockRenderShapeEvent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.neoforged.bus.api.SubscribeEvent;

public class HideTripwiresFeature extends Feature {
    public HideTripwiresFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent
    public void onGetBlockRenderShape(BlockRenderShapeEvent event) {
        // Set tripwires as invisible as the resource pack makes them invisible but when using Sodium they become black
        // lines
        if (event.getBlockState().is(Blocks.TRIPWIRE)) {
            event.setRenderShape(RenderShape.INVISIBLE);
        }
    }
}
