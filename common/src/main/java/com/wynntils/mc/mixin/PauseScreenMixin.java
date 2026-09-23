/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.PauseMenuButtonReplaceEvent;
import com.wynntils.utils.type.PauseMenuButtonType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {
    @ModifyExpressionValue(
            method = "createPauseMenu",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/screens/PauseScreen;openScreenButton(Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/components/Button;",
                            ordinal = 0))
    private Button replaceAdvancements(Button button) {
        PauseMenuButtonReplaceEvent.Text event =
                new PauseMenuButtonReplaceEvent.Text(PauseMenuButtonType.ADVANCEMENTS, button);

        MixinHelper.post(event);
        return event.getButton();
    }

    @ModifyExpressionValue(
            method = "createPauseMenu",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/screens/PauseScreen;openScreenButton(Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/components/Button;",
                            ordinal = 1))
    private Button replaceStats(Button button) {
        PauseMenuButtonReplaceEvent.Text event =
                new PauseMenuButtonReplaceEvent.Text(PauseMenuButtonType.STATS, button);

        MixinHelper.post(event);
        return event.getButton();
    }

    @ModifyExpressionValue(
            method = "createPauseMenu",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/SpriteIconButton$Builder;build()Lnet/minecraft/client/gui/components/SpriteIconButton;",
                            ordinal = 0))
    private SpriteIconButton replaceReportBugs(SpriteIconButton button) {
        PauseMenuButtonReplaceEvent.SpriteIcon event =
                new PauseMenuButtonReplaceEvent.SpriteIcon(PauseMenuButtonType.REPORT_BUGS, button);

        MixinHelper.post(event);
        return event.getButton();
    }

    @ModifyExpressionValue(
            method = "createPauseMenu",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/components/SpriteIconButton$Builder;build()Lnet/minecraft/client/gui/components/SpriteIconButton;",
                            ordinal = 1))
    private SpriteIconButton replaceSendFeedback(SpriteIconButton button) {
        PauseMenuButtonReplaceEvent.SpriteIcon event =
                new PauseMenuButtonReplaceEvent.SpriteIcon(PauseMenuButtonType.GIVE_FEEDBACK, button);

        MixinHelper.post(event);
        return event.getButton();
    }
}
