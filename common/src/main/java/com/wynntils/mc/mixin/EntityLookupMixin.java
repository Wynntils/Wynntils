/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.AddEntityLookupEvent;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLookup.class)
public abstract class EntityLookupMixin {
    @Shadow
    @Final
    private Map<UUID, EntityAccess> byUuid;

    @Inject(method = "add(Lnet/minecraft/world/level/entity/EntityAccess;)V", at = @At("HEAD"), cancellable = true)
    private void addPre(EntityAccess entityAccess, CallbackInfo ci) {
        AddEntityLookupEvent event = new AddEntityLookupEvent(entityAccess.getUUID(), byUuid);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
