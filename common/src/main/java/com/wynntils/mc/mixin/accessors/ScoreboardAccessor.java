/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin.accessors;

import java.util.Map;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Scoreboard.class)
public interface ScoreboardAccessor {
    @Accessor("playerScores")
    Map<String, Map<Objective, Score>> getPlayerScores();
}
