/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat.type;

import com.wynntils.models.combat.label.MobDebuffsLabelInfo;
import net.minecraft.world.entity.Display;

public record DebuffLabelEntry(MobDebuffsLabelInfo info, Display.TextDisplay entity) {}
