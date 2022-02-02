package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Event;

public abstract class RenderEvent extends Event {
    private final ElementType type;

    public RenderEvent(ElementType type) {
        this.type = type;
    }

    public ElementType getType() {
        return type;
    }

    public enum ElementType {

    }

    public static class Pre extends RenderEvent {

        public Pre(ElementType type) {
            super(type);
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    public static class Post extends RenderEvent {

        public Post(ElementType type) {
            super(type);
        }
    }
}
