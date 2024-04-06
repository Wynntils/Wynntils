/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.lootrunpaths;

import com.wynntils.services.lootrunpaths.type.ColoredPath;
import com.wynntils.services.lootrunpaths.type.LootrunNote;
import com.wynntils.services.lootrunpaths.type.LootrunPath;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import org.joml.Vector2d;

public record LootrunPathInstance(
        String name,
        LootrunPath path,
        List<Vector2d> simplifiedPath,
        Long2ObjectMap<List<ColoredPath>> points,
        Long2ObjectMap<Set<BlockPos>> chests,
        Long2ObjectMap<List<LootrunNote>> notes) {}
