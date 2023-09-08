/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ResourcePackClearEvent;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.resources.DownloadedPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DownloadedPackSource.class)
public abstract class DownloadedPackSourceMixin {
    @Shadow
    private Pack serverPack;

    @Inject(method = "clearServerPack()Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"), cancellable = true)
    private void onClearServerPackPre(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (serverPack == null) return;

        Event event = new ResourcePackClearEvent(serverPack);
        MixinHelper.postAlways(event);
        if (event.isCanceled()) {
            cir.setReturnValue(CompletableFuture.completedFuture(null));
            cir.cancel();
        }
    }
}
