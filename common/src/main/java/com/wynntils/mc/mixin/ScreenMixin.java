/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.PauseMenuInitEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.mc.extension.ScreenExtension;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin implements ScreenExtension {
    @Unique
    private TextInputBoxWidget wynntilsFocusedTextInput;

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("HEAD"))
    private void initPre(Minecraft client, int width, int height, CallbackInfo info) {
        Screen screen = (Screen) (Object) this;
        if (!(screen instanceof TitleScreen titleScreen)) return;

        MixinHelper.postAlways(new TitleScreenInitEvent.Pre(titleScreen));
    }

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("RETURN"))
    private void initPost(Minecraft client, int width, int height, CallbackInfo info) {
        Screen screen = (Screen) (Object) this;

        if (screen instanceof TitleScreen titleScreen) {
            MixinHelper.postAlways(new TitleScreenInitEvent.Post(titleScreen));
        } else if (screen instanceof PauseScreen pauseMenuScreen) {
            MixinHelper.post(new PauseMenuInitEvent(pauseMenuScreen));
        }
    }

    @Inject(
            method = "rebuildWidgets()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init()V"))
    private void onScreenInitPre(CallbackInfo ci) {
        // This is called whenever a screen is re-inited (e.g. when the window is resized)
        MixinHelper.postAlways(new ScreenInitEvent.Pre((Screen) (Object) this, false));
    }

    @Inject(method = "rebuildWidgets()V", at = @At("RETURN"))
    private void onScreenInitPost(CallbackInfo ci) {
        MixinHelper.postAlways(new ScreenInitEvent.Post((Screen) (Object) this, false));
    }

    @Inject(
            method = "init(Lnet/minecraft/client/Minecraft;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init()V"))
    private void onFirstScreenInitPre(CallbackInfo ci) {
        // This is called only once, when the screen is first initialized
        MixinHelper.post(new ScreenInitEvent.Pre((Screen) (Object) this, true));
    }

    @Inject(
            method = "init(Lnet/minecraft/client/Minecraft;II)V",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/gui/screens/Screen;init()V",
                            shift = At.Shift.AFTER))
    private void onFirstScreenInitPost(CallbackInfo ci) {
        // This is called only once, when the screen is first initialized
        MixinHelper.post(new ScreenInitEvent.Post((Screen) (Object) this, true));
    }

    @Inject(
            method = "Lnet/minecraft/client/gui/screens/Screen;fillCrashDetails(Lnet/minecraft/CrashReport;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void wrapScreenErrorPre(CrashReport crashReport, CallbackInfo ci) {
        if (!(Minecraft.getInstance().screen instanceof WynntilsScreen wynntilsScreen)) return;

        // This is too involved in error handling to worth risk sending events
        wynntilsScreen.wrapCurrentScreenError(crashReport);
        ci.cancel();
    }

    @Override
    @Unique
    public TextInputBoxWidget getFocusedTextInput() {
        return wynntilsFocusedTextInput;
    }

    @Override
    @Unique
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {
        this.wynntilsFocusedTextInput = focusedTextInput;
    }
}
