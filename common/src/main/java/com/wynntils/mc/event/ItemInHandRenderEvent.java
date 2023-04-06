package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ItemInHandRenderEvent extends Event {
    private final LivingEntity livingEntity;
    private final ItemStack itemStack;
    private final ItemTransforms.TransformType transformType;
    private final boolean leftHanded;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int combinedLight;

    public ItemInHandRenderEvent(LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType transformType, boolean leftHanded, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLight) {
        this.livingEntity = livingEntity;
        this.itemStack = itemStack;
        this.transformType = transformType;
        this.leftHanded = leftHanded;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.combinedLight = combinedLight;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemTransforms.TransformType getTransformType() {
        return transformType;
    }

    public boolean isLeftHanded() {
        return leftHanded;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public MultiBufferSource getMultiBufferSource() {
        return multiBufferSource;
    }

    public int getCombinedLight() {
        return combinedLight;
    }
}
