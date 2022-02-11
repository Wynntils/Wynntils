/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.LivingEntityRenderTranslucentCheckEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.player.RemotePlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerGhostTransparencyFeature extends Feature {
    LoadingCache<RemotePlayer, Boolean> isGhost =
            CacheBuilder.newBuilder()
                    .maximumSize(50)
                    .expireAfterWrite(1, TimeUnit.MINUTES)
                    .build(
                            new CacheLoader<>() {
                                @Override
                                public Boolean load(@NotNull RemotePlayer key) {
                                    throw new RuntimeException("TODO");
                                }
                            });

    @SubscribeEvent
    public void onTranslucentCheck(LivingEntityRenderTranslucentCheckEvent e)
            throws ExecutionException {
        // if (e.getEntity() instanceof RemotePlayer remotePlayer) {
        //     //if (isGhost.get(remotePlayer)) {}
        // }
    }
}
