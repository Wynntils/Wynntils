package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class RenderArmWithItemEvent extends Event implements ICancellableEvent {
    private AbstractClientPlayer player;
    private float partialTick;
    private float pitch;
    private InteractionHand hand;
    private float swingProgress;
    private ItemStack item;
    private float equippedProgress;
    private PoseStack poseStack;
    private SubmitNodeCollector nodeCollector;
    private int packedLight;

    public RenderArmWithItemEvent(
            AbstractClientPlayer player,
            float partialTick,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack item,
            float equippedProgress,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int packedLight
    ) {
        this.player = player;
        this.partialTick = partialTick;
        this.pitch = pitch;
        this.hand = hand;
        this.swingProgress = swingProgress;
        this.item = item;
        this.equippedProgress = equippedProgress;
        this.poseStack = poseStack;
        this.nodeCollector = nodeCollector;
        this.packedLight = packedLight;
    }

    public AbstractClientPlayer getPlayer() {
        return player;
    }

    public void setPlayer(AbstractClientPlayer player) {
        this.player = player;
    }

    public float getPartialTick() {
        return partialTick;
    }

    public void setPartialTick(float partialTick) {
        this.partialTick = partialTick;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public void setHand(InteractionHand hand) {
        this.hand = hand;
    }

    public float getSwingProgress() {
        return swingProgress;
    }

    public void setSwingProgress(float swingProgress) {
        this.swingProgress = swingProgress;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public float getEquippedProgress() {
        return equippedProgress;
    }

    public void setEquippedProgress(float equippedProgress) {
        this.equippedProgress = equippedProgress;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public void setPoseStack(PoseStack poseStack) {
        this.poseStack = poseStack;
    }

    public SubmitNodeCollector getNodeCollector() {
        return nodeCollector;
    }

    public void setNodeCollector(SubmitNodeCollector nodeCollector) {
        this.nodeCollector = nodeCollector;
    }

    public int getPackedLight() {
        return packedLight;
    }

    public void setPackedLight(int packedLight) {
        this.packedLight = packedLight;
    }
}
