/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.transcoder;

import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.type.Pair;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

        // Walk the component tree using DFS
        // Save the style of the parent component so we can inherit it
        Deque<Pair<Component, Style>> deque = new LinkedList<>();

        deque.add(new Pair<>(component, Style.EMPTY));

        while (!deque.isEmpty()) {
            Pair<Component, Style> currentPair = deque.pop();
            Component current = currentPair.key();
            Style parentStyle = currentPair.value();

            String componentString =
                    MutableComponent.create(current.getContents()).getString();

            StyleStringPart styleStringPart =
                    new StyleStringPart(componentString, current.getStyle(), this, parentStyle);

            List<Pair<Component, Style>> siblingPairs = current.getSiblings().stream()
                    .map(sibling ->
                            new Pair<>(sibling, styleStringPart.getPartStyle().getStyle()))
                    .collect(Collectors.toList());

            Collections.reverse(siblingPairs);
            siblingPairs.forEach(deque::addFirst);

            parts.add(styleStringPart);
        }
    }

    public static StyleString fromComponent(Component component) {
        return new StyleString(component);
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
            previousStyle = part.getPartStyle();
        }

        return builder.toString();
    }

    public MutableComponent getComponent() {
        if (parts.isEmpty()) {
            return Component.empty();
        }

        MutableComponent component = parts.get(0).getComponent();

        for (int i = 1; i < parts.size(); i++) {
            component.append(parts.get(i).getComponent());
        }

        return component;
    }

    public Matcher getMatcher(Pattern pattern) {
        return getMatcher(pattern, PartStyle.StyleType.DEFAULT);
    }

    public Matcher getMatcher(Pattern pattern, PartStyle.StyleType styleType) {
        return pattern.matcher(getString(styleType));
    }

    /**
     * Splits the style string into two parts at the given index.
     * The index is part of the second string.
     *
     * @param index The index to split at.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public void splitAt(int index) {
        int stringLength = 0;

        // test: test string
        // index = 6
        // 5 6

        if (index < 0) {
            throw new IndexOutOfBoundsException("Index must be non-negative.");
        }

        StyleStringPart partToSplit = null;
        int indexToSplit = 0;

        for (StyleStringPart part : parts) {
            int currentLength = part.getString(null, PartStyle.StyleType.NONE).length();
            stringLength += currentLength;

            if (index < stringLength) {
                partToSplit = part;
                indexToSplit = index - (stringLength - currentLength);
                break;
            }
        }

        if (partToSplit == null) {
            throw new IndexOutOfBoundsException("Index out of bounds.");
        }

        String partString = partToSplit.getString(null, PartStyle.StyleType.NONE);

        String firstString = partString.substring(0, indexToSplit);
        String secondString = partString.substring(indexToSplit);

        StyleStringPart partBefore = getPartBefore(partToSplit);
        Style styleBefore =
                partBefore == null ? Style.EMPTY : partBefore.getPartStyle().getStyle();

        Style style = partToSplit.getPartStyle().getStyle();
        StyleStringPart firstPart = new StyleStringPart(firstString, style, this, styleBefore);
        StyleStringPart secondPart = new StyleStringPart(
                secondString, style, this, firstPart.getPartStyle().getStyle());

        int indexOfPart = parts.indexOf(partToSplit);

        parts.add(indexOfPart, firstPart);
        parts.add(indexOfPart + 1, secondPart);
        parts.remove(partToSplit);
    }

    public StyleStringPart getPartFinding(Pattern pattern) {
        return getPartFinding(pattern, PartStyle.StyleType.DEFAULT);
    }

    public StyleStringPart getPartFinding(Pattern pattern, PartStyle.StyleType styleType) {
        PartStyle previousPartStyle = null;

        for (StyleStringPart part : parts) {
            if (pattern.matcher(part.getString(previousPartStyle, styleType)).find()) {
                return part;
            }

            previousPartStyle = part.getPartStyle();
        }

        return null;
    }

    public StyleStringPart getPartMatching(Pattern pattern) {
        return getPartMatching(pattern, PartStyle.StyleType.DEFAULT);
    }

    public StyleStringPart getPartMatching(Pattern pattern, PartStyle.StyleType styleType) {
        PartStyle previousPartStyle = null;

        for (StyleStringPart part : parts) {
            if (pattern.matcher(part.getString(previousPartStyle, styleType)).matches()) {
                return part;
            }

            previousPartStyle = part.getPartStyle();
        }

        return null;
    }

    public int getPartCount() {
        return parts.size();
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

    private StyleStringPart getPartBefore(StyleStringPart part) {
        int index = parts.indexOf(part);
        if (index == 0) {
            return null;
        }

        return parts.get(index - 1);
    }

    @Override
    public String toString() {
        return "CodedString{" + "parts="
                + ArrayUtils.toString(parts) + ", clickEvents="
                + ArrayUtils.toString(clickEvents) + ", hoverEvents="
                + ArrayUtils.toString(hoverEvents) + '}';
    }
}
