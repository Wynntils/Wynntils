/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.lootrunpaths.type;

import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;

public record LootrunNote(Position position, Component component) {}
