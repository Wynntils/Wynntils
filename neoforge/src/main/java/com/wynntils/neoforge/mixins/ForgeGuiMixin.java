/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.neoforge.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RenderEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class ForgeGuiMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    // See FabricGuiMixin#onRenderFood for the Fabric mixin.
    @WrapOperation(
            method = "Lnet/minecraft/client/gui/Gui;renderFoodLevel(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int onRenderFood(
            Gui instance,
            LivingEntity entity,
            Operation<Integer> original,
            @Local(argsOnly = true) GuiGraphics guiGraphics) {
        if (!MixinHelper.onWynncraft()) return original.call(instance, entity);

        RenderEvent.Pre event = new RenderEvent.Pre(
                guiGraphics, DeltaTracker.ZERO, this.minecraft.getWindow(), RenderEvent.ElementType.FOOD_BAR);
        MixinHelper.post(event);

        // Return a non-zero value to cancel rendering
        if (event.isCanceled()) return 1;

        return original.call(instance, entity);
    }

    @Inject(
            method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onRenderSelectedItemNamePre(GuiGraphics guiGraphics, int yShift, CallbackInfo ci) {
        if (!MixinHelper.onWynncraft()) return;

        RenderEvent.Pre event = new RenderEvent.Pre(
                guiGraphics, DeltaTracker.ZERO, this.minecraft.getWindow(), RenderEvent.ElementType.SELECTED_ITEM);
        MixinHelper.post(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
