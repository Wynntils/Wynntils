/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.data;

import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Optional;

public record DamageData(int dps, Optional<GearAttackSpeed> attackSpeed, List<Pair<DamageType, RangedValue>> damages)
        implements ItemData {}
