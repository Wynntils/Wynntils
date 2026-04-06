/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.neoforge.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.TooltipRenderEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class NeoForgeGuiGraphicsMixin {
    // Note: Call site 3 of 3 of ItemTooltipRenderEvent. Check the event class for more info.
    //       See FabricAbstractContainerScreenMixin#renderTooltipPre for the Fabric mixin.
    @WrapOperation(
            method =
                    "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/resources/Identifier;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V"),
            remap = false)
    private void setTooltipForNextFramePre(
            GuiGraphics instance,
            Font font,
            List<Component> tooltipLines,
            Optional<TooltipComponent> visualTooltipComponent,
            int mouseX,
            int mouseY,
            Identifier backgroundTexture,
            Operation<Void> operation,
            @Local(argsOnly = true) ItemStack itemStack) {
        ItemTooltipRenderEvent.Pre event = new ItemTooltipRenderEvent.Pre(
                (GuiGraphics) (Object) this,
                itemStack,
                Screen.getTooltipFromItem(McUtils.mc(), itemStack),
                mouseX,
                mouseY);
        MixinHelper.post(event);

        if (event.isCanceled()) return;

        operation.call(
                instance,
                font,
                event.getTooltips(),
                event.getItemStack().getTooltipImage(),
                event.getMouseX(),
                event.getMouseY(),
                backgroundTexture);
    }

    @Inject(
            method =
                    "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"))
    private void renderTooltipPre(
            Font font,
            List<ClientTooltipComponent> components,
            int x,
            int y,
            ClientTooltipPositioner positioner,
            Identifier background,
            ItemStack tooltipStack,
            CallbackInfo ci) {
        MixinHelper.post(new TooltipRenderEvent.Pre((GuiGraphics) (Object) this));
    }

    @WrapOperation(
            method =
                    "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Lnet/minecraft/world/item/ItemStack;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;"))
    private Vector2ic renderTooltipPositioning(
            ClientTooltipPositioner instance,
            int screenWidth,
            int screenHeight,
            int mouseX,
            int mouseY,
            int tooltipWidth,
            int tooltipHeight,
            Operation<Vector2ic> operation) {
        TooltipRenderEvent.Position event = new TooltipRenderEvent.Position((GuiGraphics) (Object) this);
        MixinHelper.post(event);

        if (event.getPositioner() != null) {
            return event.getPositioner()
                    .positionTooltip(screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight);
        }

        return operation.call(instance, screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight);
    }

    @Inject(
            method =
                    "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("RETURN"))
    private void renderTooltipPost(
            Font font,
            List<ClientTooltipComponent> components,
            int x,
            int y,
            ClientTooltipPositioner positioner,
            Identifier background,
            ItemStack tooltipStack,
            CallbackInfo ci) {
        MixinHelper.post(new TooltipRenderEvent.Post((GuiGraphics) (Object) this));
    }
}
