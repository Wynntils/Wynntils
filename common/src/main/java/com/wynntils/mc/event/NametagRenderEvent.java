/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class NametagRenderEvent extends Event {
    private final AbstractClientPlayer entity;
    private final Component displayName;
    private final PoseStack poseStack;
    private final MultiBufferSource buffer;
    private final int packedLight;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final Font font;

    public NametagRenderEvent(
            AbstractClientPlayer entity,
            Component displayName,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            EntityRenderDispatcher entityRenderDispatcher,
            Font font) {
        this.entity = entity;
        this.displayName = displayName;
        this.poseStack = poseStack;
        this.buffer = buffer;
        this.packedLight = packedLight;
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.font = font;
    }

    public AbstractClientPlayer getEntity() {
        return entity;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public int getPackedLight() {
        return packedLight;
    }

    public MultiBufferSource getBuffer() {
        return buffer;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public EntityRenderDispatcher getEntityRenderDispatcher() {
        return entityRenderDispatcher;
    }

    public Font getFont() {
        return font;
    }
}
