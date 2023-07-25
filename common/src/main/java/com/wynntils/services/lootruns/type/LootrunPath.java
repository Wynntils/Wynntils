/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.lootruns.type;

import java.util.List;
import net.minecraft.world.phys.Vec3;

public record LootrunPath(List<Vec3> points) {}
