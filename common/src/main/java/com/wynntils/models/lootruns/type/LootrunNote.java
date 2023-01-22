/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootruns.type;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public record LootrunNote(Vec3 position, Component component) {}
