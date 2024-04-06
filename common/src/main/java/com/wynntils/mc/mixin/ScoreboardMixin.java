/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.google.common.collect.Maps;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.PlayerTeamEvent;
import java.util.Map;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin {
    @Shadow
    public Map<String, Map<Objective, Score>> playerScores;

    @Inject(method = "<init>()V", at = @At("RETURN"))
    private void onCtor(CallbackInfo ci) {
        // We need to replace Vanilla's HashMap with a concurrent hash map
        // This should be a safe maneuver to do in the constructor
        this.playerScores = Maps.newConcurrentMap();
    }

    @Inject(
            method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void removePlayerFromTeamPre(String username, PlayerTeam playerTeam, CallbackInfo ci) {
        PlayerTeamEvent.Removed event = new PlayerTeamEvent.Removed(username, playerTeam);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "addPlayerToTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)Z", at = @At("RETURN"))
    private void addPlayerToTeamPost(String username, PlayerTeam playerTeam, CallbackInfoReturnable<Boolean> cir) {
        MixinHelper.post(new PlayerTeamEvent.Added(username, playerTeam));
    }
}
