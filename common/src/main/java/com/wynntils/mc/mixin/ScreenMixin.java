/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.PauseMenuInitEvent;
import com.wynntils.mc.event.ScreenFocusEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.ScreenRenderEvent;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.mc.extension.ScreenExtension;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
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

    @WrapOperation(
            method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/screens/Screen;renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;Ljava/util/Optional;II)V"))
    private void renderTooltipPre(
            Screen instance,
            PoseStack poseStack,
            List<Component> tooltips,
            Optional<TooltipComponent> visualTooltipComponent,
            int mouseX,
            int mouseY,
            Operation<Void> original,
            @Local(argsOnly = true) ItemStack itemStack) {
        ItemTooltipRenderEvent.Pre event =
                new ItemTooltipRenderEvent.Pre(poseStack, itemStack, tooltips, mouseX, mouseY);
        MixinHelper.post(event);
        if (event.isCanceled()) return;

        original.call(
                instance,
                event.getPoseStack(),
                event.getTooltips(),
                event.getItemStack().getTooltipImage(),
                event.getMouseX(),
                event.getMouseY());
    }

    @Inject(
            method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("RETURN"))
    private void renderTooltipPost(PoseStack poseStack, ItemStack itemStack, int mouseX, int mouseY, CallbackInfo ci) {
        MixinHelper.post(new ItemTooltipRenderEvent.Post(poseStack, itemStack, mouseX, mouseY));
    }

    @Inject(
            method = "rebuildWidgets()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init()V"))
    private void onScreenInit(CallbackInfo ci) {
        // This is called whenever a screen is re-inited (e.g. when the window is resized)
        MixinHelper.postAlways(new ScreenInitEvent((Screen) (Object) this, false));
    }

    @Inject(
            method = "init(Lnet/minecraft/client/Minecraft;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init()V"))
    private void onFirstScreenInit(CallbackInfo ci) {
        // This is called only once, when the screen is first initialized
        MixinHelper.post(new ScreenInitEvent((Screen) (Object) this, true));
    }

    @Inject(method = "changeFocus(Lnet/minecraft/client/gui/ComponentPath;)V", at = @At("HEAD"), cancellable = true)
    private void onChangeFocus(ComponentPath componentPath, CallbackInfo ci) {
        GuiEventListener guiEventListener = componentPath instanceof ComponentPath.Path path
                ? path.childPath().component()
                : componentPath.component();

        ScreenFocusEvent event = new ScreenFocusEvent((Screen) (Object) this, guiEventListener);
        MixinHelper.post(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", at = @At("RETURN"))
    private void onScreenRenderPost(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        MixinHelper.post(new ScreenRenderEvent((Screen) (Object) this, poseStack, mouseX, mouseY, partialTick));
    }

    @Inject(
            method =
                    "Lnet/minecraft/client/gui/screens/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V",
            at = @At("HEAD"),
            cancellable = true)
    private static void wrapScreenErrorPre(Runnable action, String errorDesc, String screenName, CallbackInfo ci) {
        if (!(Minecraft.getInstance().screen instanceof WynntilsScreen wynntilsScreen)) return;

        // This is too involved in error handling to worth risk sending events
        wynntilsScreen.wrapCurrentScreenError(action, errorDesc, screenName);
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
