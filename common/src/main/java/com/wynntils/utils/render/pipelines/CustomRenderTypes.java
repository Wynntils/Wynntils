/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.pipelines;

import com.wynntils.utils.render.Texture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class CustomRenderTypes {
    public static final RenderType LOOTRUN_QUAD = RenderTypes.text(Texture.LOOTRUN_LINE.identifier());

    public static final RenderType RANGE_QUAD = RenderTypes.beaconBeam(Texture.WHITE_PIXEL.identifier(), true);

    public static final RenderType PLAYER_PING_QUAD = RenderTypes.textSeeThrough(Texture.WHITE_PIXEL.identifier());
}
