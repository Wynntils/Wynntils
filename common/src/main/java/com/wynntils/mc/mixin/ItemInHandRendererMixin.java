package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.RenderArmWithItemEvent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @WrapMethod(method = "renderArmWithItem")
    private void renderArmWithItem(
            AbstractClientPlayer player,
            float partialTick,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack item,
            float equippedProgress,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int packedLight,
            Operation<Void> original
    ) {
        RenderArmWithItemEvent event = new RenderArmWithItemEvent(
                player,
                partialTick,
                pitch,
                hand,
                swingProgress,
                item,
                equippedProgress,
                poseStack,
                nodeCollector,
                packedLight
        );

        MixinHelper.post(event);

        if (!event.isCanceled()) {
            original.call(
                    event.getPlayer(),
                    event.getPartialTick(),
                    event.getPitch(),
                    event.getHand(),
                    event.getSwingProgress(),
                    event.getItem(),
                    event.getEquippedProgress(),
                    event.getPoseStack(),
                    event.getNodeCollector(),
                    event.getPackedLight()
            );
        }
    }
}
