/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.eventbus.api.Event;

public abstract class RenderEvent extends Event {
    private final ElementType type;
    private final PoseStack stack;

    public RenderEvent(ElementType type, PoseStack stack) {
        this.type = type;
        this.stack = stack;
    }

    public ElementType getType() {
        return type;
    }

    public PoseStack getStack() {
        return stack;
    }

    public enum ElementType {
        EXPERIENCE;
    }

    public static class Pre extends RenderEvent {

        public Pre(ElementType type, PoseStack stack) {
            super(type, stack);
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    public static class Post extends RenderEvent {

        public Post(ElementType type, PoseStack stack) {
            super(type, stack);
        }
    }
}
