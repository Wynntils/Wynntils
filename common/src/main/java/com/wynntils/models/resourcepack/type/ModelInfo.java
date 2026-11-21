/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.resourcepack.type;

import java.util.List;
import java.util.Set;
import net.minecraft.resources.Identifier;

public record ModelInfo(String fingerprint, Set<String> referencedSlots, List<Identifier> textureIds) {}
