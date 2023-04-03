/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.transcoder;

import com.wynntils.utils.mc.ComponentUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.ArrayUtils;

public final class StyleString {
    private final Component temporaryWorkaround;

    private final List<StyleStringPart> parts;

    private final List<ClickEvent> clickEvents = new LinkedList<>();
    private final List<HoverEvent> hoverEvents = new LinkedList<>();

    StyleString(Component component) {
        temporaryWorkaround = component;

        parts = new LinkedList<>();

        StyleStringPart lastPart = null;

        for (Component current : component.toFlatList()) {
            final StyleStringPart finalLastPart = lastPart;

            current.visit(
                    (style, string) -> {
                        parts.add(new StyleStringPart(string, style, this, finalLastPart));
                        return Optional.empty();
                    },
                    Style.EMPTY);

            lastPart = parts.get(parts.size() - 1);
        }
    }

    // We don't want to expose the actual string to the outside world
    // If you need to do an operation with this string, implement it as a method
    public String getString(PartStyle.StyleType type) {
        if (type == PartStyle.StyleType.FULL) {
            return ComponentUtils.getCoded(temporaryWorkaround);
        }

        StringBuilder builder = new StringBuilder();

        PartStyle previousStyle = null;
        for (StyleStringPart part : parts) {
            builder.append(part.getString(previousStyle, type));
            previousStyle = part.getCodedStyle();
        }

        return builder.toString();
    }

    public Component getComponent() {
        MutableComponent component = Component.empty();

        parts.forEach(part -> component.append(part.getComponent()));

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

    @Override
    public String toString() {
        return "CodedString{" + "parts="
                + ArrayUtils.toString(parts) + ", clickEvents="
                + ArrayUtils.toString(clickEvents) + ", hoverEvents="
                + ArrayUtils.toString(hoverEvents) + '}';
    }
}
