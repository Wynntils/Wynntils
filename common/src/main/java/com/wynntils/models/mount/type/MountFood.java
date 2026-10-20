/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

import com.wynntils.utils.type.Pair;
import java.util.List;

public record MountFood(List<Pair<MountStat, Integer>> mountStats) {}