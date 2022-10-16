/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.wynntils.mc.EventFactory;
import java.io.IOException;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPackSource.class)
public abstract class ClientPackSourceMixin {
    @Shadow
    private Pack serverPack;

    @Inject(method = "clearServerPack", at = @At("HEAD"), cancellable = true)
    private void onClearServerPackPre(CallbackInfo ci) {
        if (serverPack == null) {
            return;
        }

        PackResources packResources = this.serverPack.supplier.get();

        if (packResources instanceof AbstractPackResources abstractPackResources) {
            try {
                String hash = Files.asByteSource(abstractPackResources.file)
                        .hash(Hashing.sha1())
                        .toString();
                if (EventFactory.onResourcePackClearEvent(hash).isCanceled()) {
                    ci.cancel();
                }
            } catch (IOException e) {
                // ignored
            }
        } else {
            EventFactory.onResourcePackClearEvent(null);
        }
    }
}
