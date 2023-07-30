/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.lootrunpaths;

import com.wynntils.services.lootrunpaths.type.LootrunNote;
import com.wynntils.services.lootrunpaths.type.LootrunPath;
import java.io.File;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;

public record UncompiledLootrunPath(LootrunPath path, Set<BlockPos> chests, List<LootrunNote> notes, File file) {}
