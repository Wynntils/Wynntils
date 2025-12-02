/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.DataComponentGetEvent;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DataComponentHolder.class)
public interface DataComponentHolderMixin {
    @Inject(
            method = "get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;",
            at = @At("RETURN"),
            cancellable = true)
    private void onGetComponent(DataComponentType<?> type, CallbackInfoReturnable<Object> cir) {
        // We are only interested in ItemStacks
        if (!(((Object) this) instanceof ItemStack stack)) return;

        Object original = cir.getReturnValue();

        DataComponentGetEvent<?> event = null;

        if (type == DataComponents.CUSTOM_MODEL_DATA && original instanceof CustomModelData cmd) {
            event = new DataComponentGetEvent.CustomModelData(stack, cmd);
        } else if (type == DataComponents.DYED_COLOR && original instanceof DyedItemColor dye) {
            event = new DataComponentGetEvent.DyedItemColor(stack, dye);
        } else if (type == DataComponents.ENCHANTMENT_GLINT_OVERRIDE) {
            // Original will always be null for items that do not have an override
            event = new DataComponentGetEvent.EnchantmentGlintOverride(stack, original != null);
        } else if (type == DataComponents.POTION_CONTENTS && original instanceof PotionContents pc) {
            event = new DataComponentGetEvent.PotionContents(stack, pc);
        }

        if (event == null) return;

        MixinHelper.post(event);
        cir.setReturnValue(event.getValue());
    }
}
