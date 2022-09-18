package com.wynntils.mc.mixin;

import com.wynntils.mc.EventFactory;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(
            method = "onScroll",
            at = @At("HEAD")
    )
    private void onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
        EventFactory.onMouseScroll(yOffset);
    }

}