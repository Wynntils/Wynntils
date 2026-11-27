/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.wynntils.services.mapdata.features.type.MapPath;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public record CompiledMapPath(
        MapPath feature,
        Long2ObjectMap<List<ColoredPath>> points,
        Long2ObjectMap<Set<BlockPos>> highlights,
        Long2ObjectMap<List<Note>> notes,
        LongSet chunks) {
    public record Note(Vec3 position, Component component) {}
}
