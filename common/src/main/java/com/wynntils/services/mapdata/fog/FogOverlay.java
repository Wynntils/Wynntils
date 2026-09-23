/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.fog;

import com.wynntils.utils.colors.CustomColor;
import net.minecraft.resources.Identifier;

/**
 * What the map renderers need to draw fog over one tile: the style, the mask texture, its padding in blocks, and the
 * fog colour (only its alpha is used by the parchment style).
 */
public record FogOverlay(FogStyle style, Identifier mask, int paddingBlocks, CustomColor color) {}
