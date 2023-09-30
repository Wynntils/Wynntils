/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RecipeBookOpenEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @ModifyArg(
            method = "init()V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/ImageButton;<init>(IIIILnet/minecraft/client/gui/components/WidgetSprites;Lnet/minecraft/client/gui/components/Button$OnPress;)V"))
    private Button.OnPress onRecipeBookButtonCreate(Button.OnPress onPress) {
        return button -> {
            RecipeBookOpenEvent event = new RecipeBookOpenEvent();
            MixinHelper.post(event);
            if (event.isCanceled()) return;

            // Call original lambda
            onPress.onPress(button);
        };
    }
}
