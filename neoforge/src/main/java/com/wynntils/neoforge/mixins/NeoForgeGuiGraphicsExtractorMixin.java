/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.neoforge.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.TooltipRenderEvent;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

@Mixin(GuiGraphicsExtractor.class)
public abstract class NeoForgeGuiGraphicsExtractorMixin {
    // Note: Call site 3 of 3 of ItemTooltipRenderEvent. Check the event class for more info.
    @WrapMethod(
            method =
                    "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/resources/Identifier;I)V",
            remap = false)
    private void setItemTooltipForNextFrame(
            Font font,
            List<Component> tooltipLines,
            Optional<TooltipComponent> visualTooltipComponent,
            ItemStack itemStack,
            int mouseX,
            int mouseY,
            Identifier backgroundTexture,
            int linesBeforeExtraSpace,
            Operation<Void> operation) {
        ItemTooltipRenderEvent event =
                new ItemTooltipRenderEvent((GuiGraphicsExtractor) (Object) this, itemStack, tooltipLines);
        MixinHelper.post(event);

        if (event.isCanceled()) return;

        operation.call(
                font,
                event.getTooltips(),
                visualTooltipComponent,
                itemStack,
                mouseX,
                mouseY,
                backgroundTexture,
                linesBeforeExtraSpace);
    }

    @Inject(
            method =
                    "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;ILnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"))
    private void renderTooltipPre(
            Font font,
            List<ClientTooltipComponent> components,
            int x,
            int y,
            ClientTooltipPositioner positioner,
            Identifier background,
            int linesBeforeExtraSpace,
            ItemStack tooltipStack,
            CallbackInfo ci) {
        MixinHelper.post(
                new TooltipRenderEvent.Pre((GuiGraphicsExtractor) (Object) this, components, linesBeforeExtraSpace));
    }

    @WrapOperation(
            method =
                    "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;ILnet/minecraft/world/item/ItemStack;)V",
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
        TooltipRenderEvent.Position event = new TooltipRenderEvent.Position((GuiGraphicsExtractor) (Object) this);
        MixinHelper.post(event);

        if (event.getPositioner() != null) {
            return event.getPositioner()
                    .positionTooltip(screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight);
        }

        return operation.call(instance, screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight);
    }

    @Inject(
            method =
                    "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;ILnet/minecraft/world/item/ItemStack;)V",
            at = @At("RETURN"))
    private void renderTooltipPost(
            Font font,
            List<ClientTooltipComponent> components,
            int x,
            int y,
            ClientTooltipPositioner positioner,
            Identifier background,
            int linesBeforeExtraSpace,
            ItemStack tooltipStack,
            CallbackInfo ci) {
        MixinHelper.post(new TooltipRenderEvent.Post((GuiGraphicsExtractor) (Object) this));
    }
}
