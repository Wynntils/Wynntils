/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.EventThread;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;

@EventThread(EventThread.Type.ANY)
public class BlockRenderShapeEvent extends Event {
    private final BlockState blockState;

    private RenderShape renderShape = null;

    public BlockRenderShapeEvent(BlockState blockState) {
        this.blockState = blockState;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public RenderShape getRenderShape() {
        return renderShape;
    }

    public void setRenderShape(RenderShape renderShape) {
        this.renderShape = renderShape;
    }
}
