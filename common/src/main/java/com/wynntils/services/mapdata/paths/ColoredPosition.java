/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import net.minecraft.world.phys.Vec3;

public record ColoredPosition(Vec3 position, int color, int sampleIndex) {}
