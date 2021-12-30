/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mixin;

import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
    @Inject(
            method = "removePlayerTeam(Lnet/minecraft/world/scores/PlayerTeam;)V",
            at = @At("HEAD"),
            cancellable = true)
    public void removePlayerTeamPre(PlayerTeam playerTeam, CallbackInfo ci) {
        // Work around bug in Wynncraft that causes a lot of NPEs in Vanilla
        if (playerTeam == null) {
            ci.cancel();
        }
    }
}
