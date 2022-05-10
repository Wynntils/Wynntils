/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.lootrun.objects;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public record LootrunInstance(
        Long2ObjectMap<List<List<Point>>> points,
        Long2ObjectMap<Set<BlockPos>> chests,
        Long2ObjectMap<List<Pair<Vec3, Component>>> notes) {}
