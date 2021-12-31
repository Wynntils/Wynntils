/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin {
    @Shadow
    public abstract PlayerTeam getPlayersTeam(String username);

    @Inject(
            method =
                    "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V",
            at = @At("HEAD"),
            cancellable = true)
    public void removePlayerFromTeamPre(String username, PlayerTeam playerTeam, CallbackInfo ci) {
        // Work around bug in Wynncraft that causes NPEs in Vanilla
        if (this.getPlayersTeam(username) != playerTeam) {
            ci.cancel();
        }
    }
}
