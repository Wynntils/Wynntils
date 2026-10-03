/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.extension.ItemStackTemplateExtension;
import com.wynntils.models.items.FakeItemStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStackTemplate.class)
public abstract class ItemStackTemplateMixin implements ItemStackTemplateExtension {
    @Unique
    private FakeItemStack fakeItemStack;

    @Inject(method = "create()Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    private void modifyCreatedItemStack(CallbackInfoReturnable<ItemStack> cir) {
        if (fakeItemStack != null) {
            cir.setReturnValue(fakeItemStack);
        }
    }

    @Override
    @Unique
    public FakeItemStack getFakeItemStack() {
        return fakeItemStack;
    }

    @Override
    @Unique
    public void setFakeItemStack(FakeItemStack fakeItemStack) {
        this.fakeItemStack = fakeItemStack;
    }
}
