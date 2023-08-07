/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.LocalSoundEvent;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtension {
    @Unique
    private CustomColor wynntilsGlowColor = CustomColor.NONE;

    @Unique
    private boolean wynntilsRendered = true;

    @Override
    public CustomColor getGlowColor() {
        return wynntilsGlowColor;
    }

    @Override
    public void setGlowColor(CustomColor color) {
        wynntilsGlowColor = color;
    }

    @Override
    public boolean isRendered() {
        return wynntilsRendered;
    }

    @Override
    public void setRendered(boolean rendered) {
        wynntilsRendered = rendered;
    }

    @Inject(method = "playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", at = @At("HEAD"), cancellable = true)
    private void playSoundPre(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        LocalSoundEvent.LocalEntity event = new LocalSoundEvent.LocalEntity(sound, (Entity) (Object) this);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
