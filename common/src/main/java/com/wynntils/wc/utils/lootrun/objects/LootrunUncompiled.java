/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.lootrun.objects;

import it.unimi.dsi.fastutil.Pair;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public record LootrunUncompiled(List<Vec3> points, Set<BlockPos> chests, List<Pair<Vec3, Component>> notes, File file) {

    public LootrunUncompiled(LootrunUncompiled old, File file) {
        this(old.points, old.chests, old.notes, file);
    }
}
