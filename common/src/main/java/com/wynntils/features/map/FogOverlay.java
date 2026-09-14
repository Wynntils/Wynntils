/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.wynntils.utils.colors.CustomColor;
import net.minecraft.resources.Identifier;

/** What the map renderers need to draw fog over one tile: the mask texture, its padding in blocks, and the fog colour. */
public record FogOverlay(Identifier mask, int paddingBlocks, CustomColor color) {}
