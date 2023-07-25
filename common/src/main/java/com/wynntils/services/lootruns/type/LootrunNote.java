/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.lootruns.type;

import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;

public record LootrunNote(Position position, Component component) {}
