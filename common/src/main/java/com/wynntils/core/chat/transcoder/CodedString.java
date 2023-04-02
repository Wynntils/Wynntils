/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.transcoder;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.ArrayUtils;

public final class CodedString {
    private final List<CodedStringPart> parts;

    private final List<ClickEvent> clickEvents = new LinkedList<>();
    private final List<HoverEvent> hoverEvents = new LinkedList<>();

    private Component componentCache;

    CodedString(Component component) {
        parts = new LinkedList<>();

        for (Component current : component.toFlatList()) {
            current.visit(
                    (style, string) -> {
                        parts.add(new CodedStringPart(string, style, this));
                        return Optional.empty();
                    },
                    Style.EMPTY);
        }
    }

    // We don't want to expose the actual string to the outside world
    // If you need to do an operation with this string, implement it as a method
    private String getCoded() {
        StringBuilder builder = new StringBuilder();

        for (CodedStringPart part : parts) {
            builder.append(part.getCoded());
        }

        return builder.toString();
    }

    public Component getComponent() {
        if (componentCache != null) {
            return componentCache;
        }

        MutableComponent component = Component.empty();

        parts.forEach(part -> component.append(part.getComponent()));

        componentCache = component;

        return component;
    }

    int addClickEvent(ClickEvent clickEvent) {
        // Check if the event is already in the list
        for (int i = 0; i < clickEvents.size(); i++) {
            ClickEvent event = clickEvents.get(i);
            if (event.equals(clickEvent)) {
                return i + 1;
            }
        }

        clickEvents.add(clickEvent);

        return clickEvents.size();
    }

    int addHoverEvent(HoverEvent hoverEvent) {
        // Check if the event is already in the list
        for (int i = 0; i < hoverEvents.size(); i++) {
            HoverEvent event = hoverEvents.get(i);
            if (event.equals(hoverEvent)) {
                return i + 1;
            }
        }

        hoverEvents.add(hoverEvent);

        return hoverEvents.size();
    }

    void invalidateCache() {
        componentCache = null;
    }

    @Override
    public String toString() {
        return "CodedString{" + "parts="
                + ArrayUtils.toString(parts) + ", clickEvents="
                + ArrayUtils.toString(clickEvents) + ", hoverEvents="
                + ArrayUtils.toString(hoverEvents) + '}';
    }
}
