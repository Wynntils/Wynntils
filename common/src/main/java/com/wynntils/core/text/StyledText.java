/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.type.IterationDecision;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.ArrayUtils;

public final class StyledText {
    public static final StyledText EMPTY = new StyledText(List.of(), List.of(), List.of());

    private final Component temporaryWorkaround;

    private final List<StyledTextPart> parts;

    private final List<ClickEvent> clickEvents;
    private final List<HoverEvent> hoverEvents;

    private StyledText(
            List<StyledTextPart> parts,
            Component temporaryWorkaround,
            List<ClickEvent> clickEvents,
            List<HoverEvent> hoverEvents) {
        this.parts = parts.stream()
                .filter(styledTextPart -> !styledTextPart.isEmpty())
                .map(styledTextPart -> new StyledTextPart(styledTextPart, this))
                .collect(Collectors.toList());
        this.temporaryWorkaround = temporaryWorkaround;
        this.clickEvents = new ArrayList<>(clickEvents);
        this.hoverEvents = new ArrayList<>(hoverEvents);
    }

    private StyledText(List<StyledTextPart> parts, List<ClickEvent> clickEvents, List<HoverEvent> hoverEvents) {
        this.parts = parts.stream()
                .filter(styledTextPart -> !styledTextPart.isEmpty())
                .map(styledTextPart -> new StyledTextPart(styledTextPart, this))
                .collect(Collectors.toList());
        this.clickEvents = new ArrayList<>(clickEvents);
        this.hoverEvents = new ArrayList<>(hoverEvents);

        // We can't know the component, just use our own representation
        this.temporaryWorkaround = getComponent();
    }

    public static StyledText fromComponent(Component component) {
        Component temporaryWorkaround = component;

        List<StyledTextPart> parts = new ArrayList<>();
        List<ClickEvent> clickEvents = new ArrayList<>();
        List<HoverEvent> hoverEvents = new ArrayList<>();

        // Walk the component tree using DFS
        // Component#visit behaves weirdly, so we do it manually
        // Save the style of the parent component so we can inherit it
        Deque<Pair<Component, Style>> deque = new LinkedList<>();

        deque.add(new Pair<>(component, Style.EMPTY));

        while (!deque.isEmpty()) {
            Pair<Component, Style> currentPair = deque.pop();
            Component current = currentPair.key();
            Style parentStyle = currentPair.value();

            // We use getContents here to get this and only this component's string.
            String componentString =
                    MutableComponent.create(current.getContents()).getString();

            List<StyledTextPart> styledTextParts =
                    StyledTextPart.fromCodedString(componentString, current.getStyle(), null, parentStyle);

            // Only actual styles are inherited, string formatting codes are not
            Style styleToFollowForChildren = current.getStyle().applyTo(parentStyle);

            List<Pair<Component, Style>> siblingPairs = current.getSiblings().stream()
                    .map(sibling -> new Pair<>(sibling, styleToFollowForChildren))
                    .collect(Collectors.toList());

            Collections.reverse(siblingPairs);
            siblingPairs.forEach(deque::addFirst);

            // Disallow empty parts
            parts.addAll(
                    styledTextParts.stream().filter(part -> !part.isEmpty()).toList());
        }

        return new StyledText(parts, temporaryWorkaround, clickEvents, hoverEvents);
    }

    public static StyledText fromString(String codedString) {
        return new StyledText(
                StyledTextPart.fromCodedString(codedString, Style.EMPTY, null, Style.EMPTY), List.of(), List.of());
    }

    public static StyledText fromCodedString(CodedString codedString) {
        return fromString(codedString.getInternalCodedStringRepresentation());
    }

    public static StyledText fromJson(String json) {
        return fromComponent(Component.Serializer.fromJson(json));
    }

    public String getString() {
        return getString(PartStyle.StyleType.DEFAULT);
    }

    // We don't want to expose the actual string to the outside world
    // If you need to do an operation with this string, implement it as a method
    public String getString(PartStyle.StyleType type) {
        if (type == PartStyle.StyleType.FULL) {
            return ComponentUtils.getCoded(temporaryWorkaround).getInternalCodedStringRepresentation();
        }

        StringBuilder builder = new StringBuilder();

        PartStyle previousStyle = null;
        for (StyledTextPart part : parts) {
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

    public static StyledText join(StyledText styledTextSeparator, StyledText... texts) {
        List<StyledTextPart> parts = new ArrayList<>();
        List<ClickEvent> clickEvents = new ArrayList<>();
        List<HoverEvent> hoverEvents = new ArrayList<>();

        final int length = texts.length;
        for (int i = 0; i < length; i++) {
            StyledText text = texts[i];
            parts.addAll(text.parts);

            if (i != length - 1) {
                parts.addAll(styledTextSeparator.parts);
            }

            clickEvents.addAll(text.clickEvents);
            hoverEvents.addAll(text.hoverEvents);
        }

        clickEvents.addAll(styledTextSeparator.clickEvents);
        hoverEvents.addAll(styledTextSeparator.hoverEvents);

        return new StyledText(parts, clickEvents, hoverEvents);
    }

    public static StyledText join(StyledText styledTextSeparator, Iterable<StyledText> texts) {
        return join(styledTextSeparator, Iterables.toArray(texts, StyledText.class));
    }

    public static StyledText join(String codedStringSeparator, StyledText... texts) {
        return join(StyledText.fromString(codedStringSeparator), texts);
    }

    public static StyledText join(String codedStringSeparator, Iterable<StyledText> texts) {
        return join(StyledText.fromString(codedStringSeparator), Iterables.toArray(texts, StyledText.class));
    }

    public static StyledText concat(StyledText... texts) {
        List<StyledTextPart> parts = new ArrayList<>();
        List<ClickEvent> clickEvents = new ArrayList<>();
        List<HoverEvent> hoverEvents = new ArrayList<>();

        for (StyledText text : texts) {
            parts.addAll(text.parts);
            clickEvents.addAll(text.clickEvents);
            hoverEvents.addAll(text.hoverEvents);
        }

        return new StyledText(parts, clickEvents, hoverEvents);
    }

    public static StyledText concat(Iterable<StyledText> texts) {
        return concat(Iterables.toArray(texts, StyledText.class));
    }

    public StyledText getNormalized() {
        return new StyledText(
                parts.stream().map(StyledTextPart::asNormalized).collect(Collectors.toList()),
                temporaryWorkaround,
                clickEvents,
                hoverEvents);
    }

    public StyledText trim() {
        if (parts.isEmpty()) {
            return this;
        }

        List<StyledTextPart> newParts = new ArrayList<>(parts);
        newParts.set(0, newParts.get(0).stripLeading());

        int lastIndex = newParts.size() - 1;
        newParts.set(lastIndex, newParts.get(lastIndex).stripTrailing());

        return new StyledText(newParts, temporaryWorkaround, clickEvents, hoverEvents);
    }

    public boolean isEmpty() {
        return parts.isEmpty();
    }

    public boolean isBlank() {
        return parts.stream().allMatch(StyledTextPart::isBlank);
    }

    public boolean contains(String codedString) {
        return contains(codedString, PartStyle.StyleType.DEFAULT);
    }

    public boolean contains(StyledText styledText) {
        return contains(styledText.getString(), PartStyle.StyleType.DEFAULT);
    }

    public boolean contains(String codedString, PartStyle.StyleType styleType) {
        return getString(styleType).contains(codedString);
    }

    public boolean contains(StyledText styledText, PartStyle.StyleType styleType) {
        return contains(styledText.getString(styleType), styleType);
    }

    public boolean startsWith(String codedString) {
        return startsWith(codedString, PartStyle.StyleType.DEFAULT);
    }

    public boolean startsWith(StyledText styledText) {
        return startsWith(styledText.getString(), PartStyle.StyleType.DEFAULT);
    }

    public boolean startsWith(String codedString, PartStyle.StyleType styleType) {
        return getString(styleType).startsWith(codedString);
    }

    public boolean startsWith(StyledText styledText, PartStyle.StyleType styleType) {
        return startsWith(styledText.getString(styleType), styleType);
    }

    public boolean endsWith(String codedString) {
        return endsWith(codedString, PartStyle.StyleType.DEFAULT);
    }

    public boolean endsWith(StyledText styledText) {
        return endsWith(styledText.getString(), PartStyle.StyleType.DEFAULT);
    }

    public boolean endsWith(String codedString, PartStyle.StyleType styleType) {
        return getString(styleType).endsWith(codedString);
    }

    public boolean endsWith(StyledText styledText, PartStyle.StyleType styleType) {
        return endsWith(styledText.getString(styleType), styleType);
    }

    public Matcher getMatcher(Pattern pattern) {
        return getMatcher(pattern, PartStyle.StyleType.DEFAULT);
    }

    public Matcher getMatcher(Pattern pattern, PartStyle.StyleType styleType) {
        return pattern.matcher(getString(styleType));
    }

    public boolean matches(Pattern pattern) {
        return getMatcher(pattern).matches();
    }

    public boolean matches(Pattern pattern, PartStyle.StyleType styleType) {
        return getMatcher(pattern, styleType).matches();
    }

    public StyledText append(StyledText styledText) {
        return concat(this, styledText);
    }

    public StyledText append(String codedString) {
        return append(StyledText.fromString(codedString));
    }

    public StyledText prepend(StyledText styledText) {
        return concat(styledText, this);
    }

    public StyledText prepend(String codedString) {
        return prepend(StyledText.fromString(codedString));
    }

    public StyledText iterate(BiFunction<StyledTextPart, List<StyledTextPart>, IterationDecision> function) {
        List<StyledTextPart> newParts = new ArrayList<>();

        for (int i = 0; i < parts.size(); i++) {
            StyledTextPart part = parts.get(i);
            List<StyledTextPart> functionParts = new ArrayList<>();
            functionParts.add(part);
            IterationDecision decision = function.apply(part, functionParts);

            newParts.addAll(functionParts);

            if (decision == IterationDecision.BREAK) {
                // Add the rest of the parts
                newParts.addAll(parts.subList(i + 1, parts.size()));
                break;
            }
        }

        return new StyledText(newParts, temporaryWorkaround, clickEvents, hoverEvents);
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

    public int length() {
        return parts.stream().mapToInt(StyledTextPart::length).sum();
    }

    public Optional<JsonObject> toJsonObject() {
        String json = StringUtils.substringBeforeLast(getString(), "}") + "}"; // remove extra unnecessary info
        try {
            return Optional.of(JsonParser.parseString(json).getAsJsonObject());
        } catch (JsonSyntaxException e) {
            return Optional.empty();
        }
    }

    public PartStyle getStyleAt(int index) {
        for (StyledTextPart part : parts) if ((index -= part.length()) < 0) return part.getPartStyle();
        return PartStyle.NONE;
    }

    public int getWidth() {
        return parts.stream().mapToInt(StyledTextPart::width).sum();
    }

    public StyledText[] split(
            String s) { // todo: doesn't work with color codes because the parts don't keep their color
        return Arrays.stream(getString().split(s)).map(StyledText::fromString).toArray(StyledText[]::new);
    }

    public StyledText subtext(
            int start, int end) { // todo: doesn't work with color codes because the parts don't keep their color
        return StyledText.fromString(getString().substring(start, end));
    }

    public StyledText subtext(
            int start) { // todo: doesn't work with color codes because the parts don't keep their color
        return StyledText.fromString(getString().substring(start));
    }

    public StyledText replaceAll(String regex, String replacement) {
        return StyledText.fromString(getString().replaceAll(regex, replacement));
    }

    public StyledText replace(String target, String replacement) {
        return StyledText.fromString(getString().replace(target, replacement));
    }

    private StyledTextPart getPartBefore(StyledTextPart part) {
        int index = parts.indexOf(part);
        if (index == 0) {
            return null;
        }

        return parts.get(index - 1);
    }

    @Override
    public String toString() {
        return "StyledText{" + "parts="
                + ArrayUtils.toString(parts) + ", clickEvents="
                + ArrayUtils.toString(clickEvents) + ", hoverEvents="
                + ArrayUtils.toString(hoverEvents) + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StyledText that = (StyledText) o;
        return Objects.deepEquals(parts, that.parts)
                && Objects.deepEquals(clickEvents, that.clickEvents)
                && Objects.deepEquals(hoverEvents, that.hoverEvents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parts, clickEvents, hoverEvents);
    }
}
