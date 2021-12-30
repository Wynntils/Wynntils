/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mixin;

import com.wynntils.mc.event.EventFactory;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.PlayerUpdate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundPlayerInfoPacket.class)
public class ClientboundPlayerInfoPacketMixin {
    @Shadow @Final public List<PlayerUpdate> entries;
    @Shadow private Action action;

    @Inject(method = "read(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"))
    public void readPost(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
        EventFactory.onPlayerInfoPacket(action, entries);
    }
}
