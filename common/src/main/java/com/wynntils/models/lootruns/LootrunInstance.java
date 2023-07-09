/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootruns;

import com.wynntils.models.lootruns.type.ColoredPath;
import com.wynntils.models.lootruns.type.LootrunNote;
import com.wynntils.models.lootruns.type.LootrunPath;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import org.joml.Vector2d;

public record LootrunInstance(
        String name,
        LootrunPath path,
        List<Vector2d> simplifiedPath,
        Long2ObjectMap<List<ColoredPath>> points,
        Long2ObjectMap<Set<BlockPos>> chests,
        Long2ObjectMap<List<LootrunNote>> notes) {}
