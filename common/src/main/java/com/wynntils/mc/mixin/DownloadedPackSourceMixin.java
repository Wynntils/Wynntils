/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.wynntils.mc.EventFactory;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.resources.DownloadedPackSource;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DownloadedPackSource.class)
public abstract class DownloadedPackSourceMixin {
    @Shadow
    private Pack serverPack;

    @Inject(method = "clearServerPack", at = @At("HEAD"), cancellable = true)
    private void onClearServerPackPre(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (serverPack == null) {
            return;
        }

        try (PackResources packResources = this.serverPack.open()) {
            // We can calculate this here as this is always going to be posted anyway
            if (packResources instanceof FilePackResources filePackResources) {
                String hash = Files.asByteSource(filePackResources.file)
                        .hash(Hashing.sha1())
                        .toString();

                if (EventFactory.onResourcePackClearEvent(hash).isCanceled()) {
                    cir.setReturnValue(CompletableFuture.completedFuture(null));
                    cir.cancel();
                }
            } else {
                EventFactory.onResourcePackClearEvent(null);
            }
        } catch (IOException e) {
            // ignored
        }
    }
}
