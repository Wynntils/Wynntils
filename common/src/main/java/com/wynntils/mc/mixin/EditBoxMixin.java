/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.EditBoxInsertEvent;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
public abstract class EditBoxMixin {
    @Inject(method = "insertText(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void onInsertText(String textToInsert, CallbackInfo ci) {
        EditBoxInsertEvent event = new EditBoxInsertEvent(textToInsert);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
