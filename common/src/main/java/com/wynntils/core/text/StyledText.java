/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text;

import com.google.common.collect.Iterables;
import com.wynntils.utils.type.IterationDecision;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class StyledText implements Iterable<StyledTextPart> {
    public static final StyledText EMPTY = new StyledText(List.of(), List.of(), List.of());

    private final List<StyledTextPart> parts;

    private final List<ClickEvent> clickEvents;
    private final List<HoverEvent> hoverEvents;

    private StyledText(List<StyledTextPart> parts, List<ClickEvent> clickEvents, List<HoverEvent> hoverEvents) {
        this.parts = parts.stream()
                .filter(styledTextPart -> !styledTextPart.isEmpty())
                .map(styledTextPart -> new StyledTextPart(styledTextPart, this))
                .collect(Collectors.toList());
        this.clickEvents = new ArrayList<>(clickEvents);
        this.hoverEvents = new ArrayList<>(hoverEvents);
    }

    public static StyledText fromComponent(Component component) {
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

        return new StyledText(parts, clickEvents, hoverEvents);
    }

    public static StyledText fromString(String codedString) {
        return new StyledText(
                StyledTextPart.fromCodedString(codedString, Style.EMPTY, null, Style.EMPTY), List.of(), List.of());
    }

    public static StyledText fromUnformattedString(String unformattedString) {
        StyledTextPart part = new StyledTextPart(unformattedString, Style.EMPTY, null, Style.EMPTY);
        return new StyledText(List.of(part), List.of(), List.of());
    }

    public static StyledText fromPart(StyledTextPart part) {
        return new StyledText(List.of(part), List.of(), List.of());
    }

    public static StyledText fromJson(String json) {
        MutableComponent component = Component.Serializer.fromJson(json);
        return component == null ? StyledText.EMPTY : StyledText.fromComponent(component);
    }

    // We don't want to expose the actual string to the outside world
    // If you need to do an operation with this string, implement it as a method
    public String getString(PartStyle.StyleType type) {
        StringBuilder builder = new StringBuilder();

        PartStyle previousStyle = null;
        for (StyledTextPart part : parts) {
            builder.append(part.getString(previousStyle, type));
            previousStyle = part.getPartStyle();
        }

        return builder.toString();
    }

    /**
     * @return The string representation of this {@link StyledText} with default formatting codes.
     */
    public String getString() {
        return getString(PartStyle.StyleType.DEFAULT);
    }

    public String getStringWithoutFormatting() {
        return getString(PartStyle.StyleType.NONE);
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

    public int length() {
        return parts.stream().mapToInt(StyledTextPart::length).sum();
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

        return new StyledText(newParts, clickEvents, hoverEvents);
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
        return contains(styledText.getString(PartStyle.StyleType.DEFAULT), PartStyle.StyleType.DEFAULT);
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
        return startsWith(styledText.getString(PartStyle.StyleType.DEFAULT), PartStyle.StyleType.DEFAULT);
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
        return endsWith(styledText.getString(PartStyle.StyleType.DEFAULT), PartStyle.StyleType.DEFAULT);
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
        return matches(pattern, PartStyle.StyleType.DEFAULT);
    }

    public boolean matches(Pattern pattern, PartStyle.StyleType styleType) {
        return pattern.matcher(getString(styleType)).matches();
    }

    public boolean find(Pattern pattern) {
        return find(pattern, PartStyle.StyleType.DEFAULT);
    }

    public boolean find(Pattern pattern, PartStyle.StyleType styleType) {
        return pattern.matcher(getString(styleType)).find();
    }

    public StyledText append(StyledText styledText) {
        return concat(this, styledText);
    }

    public StyledText append(String codedString) {
        return append(StyledText.fromString(codedString));
    }

    public StyledText appendPart(StyledTextPart part) {
        List<StyledTextPart> newParts = new ArrayList<>(parts);
        newParts.add(part);
        return new StyledText(newParts, clickEvents, hoverEvents);
    }

    public StyledText prepend(StyledText styledText) {
        return concat(styledText, this);
    }

    public StyledText prepend(String codedString) {
        return prepend(StyledText.fromString(codedString));
    }

    public StyledText prependPart(StyledTextPart part) {
        List<StyledTextPart> newParts = new ArrayList<>(parts);
        newParts.add(0, part);
        return new StyledText(newParts, clickEvents, hoverEvents);
    }

    /**
     * Splits this {@link StyledText} into multiple {@link StyledText}s at the given index.
     * <p> Note that {@link PartStyle.StyleType.NONE} is used when splitting.
     * @param regex the regex to split at
     * @return the split {@link StyledText}s
     */
    public StyledText[] split(String regex) {
        // If this is an empty text, return an array with a single empty text
        if (parts.isEmpty()) {
            return new StyledText[] {StyledText.EMPTY};
        }

        final Pattern pattern = Pattern.compile(regex);

        List<StyledText> splitTexts = new ArrayList<>();
        List<StyledTextPart> splitParts = new ArrayList<>();

        for (int i = 0; i < parts.size(); i++) {
            StyledTextPart part = parts.get(i);
            String partString = part.getString(null, PartStyle.StyleType.NONE);

            // Avoid empty parts at the end of the list, but keep them otherwise
            int maxSplit = i == parts.size() - 1 ? 0 : -1;

            List<String> stringParts =
                    Arrays.stream(pattern.split(partString, maxSplit)).toList();

            Matcher matcher = pattern.matcher(partString);
            if (matcher.find()) {
                // If the pattern matches, then we have a split
                // Create the new styled texts

                // Each part is a new text, but the first and last one connect to the previous/next text
                for (int j = 0; j < stringParts.size(); j++) {
                    String stringPart = stringParts.get(j);
                    splitParts.add(
                            new StyledTextPart(stringPart, part.getPartStyle().getStyle(), null, Style.EMPTY));

                    // If this is the last part, then we might need to add other parts
                    if (j != stringParts.size() - 1) {
                        splitTexts.add(new StyledText(splitParts, clickEvents, hoverEvents));
                        splitParts.clear();
                    }
                }

                continue;
            }

            // If the pattern doesn't match, then we can just add the part to the list
            splitParts.add(part);
        }

        // Add the last text
        if (!splitParts.isEmpty()) {
            splitTexts.add(new StyledText(splitParts, clickEvents, hoverEvents));
        }

        return splitTexts.toArray(new StyledText[0]);
    }

    public StyledText substring(int beginIndex) {
        return substring(beginIndex, length());
    }

    /**
     * Returns a new {@link StyledText} that is a substring of this {@link StyledText}.
     * <p> Note that {@link PartStyle.StyleType.NONE} is used for calculating the index.
     * @param beginIndex the beginning index, inclusive
     * @param endIndex the ending index, exclusive
     * @return the new {@link StyledText}
     */
    public StyledText substring(int beginIndex, int endIndex) {
        if (endIndex < beginIndex) {
            throw new IndexOutOfBoundsException("endIndex must be greater than beginIndex");
        }
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("beginIndex must be greater than or equal to 0");
        }
        if (endIndex > length()) {
            throw new IndexOutOfBoundsException("endIndex must be less than or equal to length()");
        }

        List<StyledTextPart> includedParts = new ArrayList<>();

        int currentIndex = 0;

        for (StyledTextPart part : parts) {
            if (currentIndex >= beginIndex && currentIndex + part.length() < endIndex) {
                // 1. This full part is included

                includedParts.add(part);
            } else if (currentIndex + part.length() >= beginIndex || currentIndex + part.length() > endIndex) {
                // 2. This part is partially included

                int startIndexInPart = Math.max(0, beginIndex - currentIndex);
                int endIndexInPart = Math.min(part.length(), endIndex - currentIndex);

                String includedSubstring =
                        part.getString(null, PartStyle.StyleType.NONE).substring(startIndexInPart, endIndexInPart);

                includedParts.add(new StyledTextPart(
                        includedSubstring, part.getPartStyle().getStyle(), null, Style.EMPTY));
            }

            currentIndex += part.length();
        }

        return new StyledText(includedParts, clickEvents, hoverEvents);
    }

    /**
     * Replaces the first occurrence of the given regex with the given replacement.
     * <p> Note that {@link PartStyle.StyleType.NONE} is used when matching and replacing.
     * @param regex the regex to replace
     * @param replacement the replacement
     * @return the new {@link StyledText}
     */
    public StyledText replaceFirst(String regex, String replacement) {
        final Pattern pattern = Pattern.compile(regex);

        List<StyledTextPart> newParts = new ArrayList<>();

        for (StyledTextPart part : parts) {
            String partString = part.getString(null, PartStyle.StyleType.NONE);

            Matcher matcher = pattern.matcher(partString);

            if (matcher.find()) {
                String replacedString = matcher.replaceFirst(replacement);

                newParts.add(
                        new StyledTextPart(replacedString, part.getPartStyle().getStyle(), null, Style.EMPTY));

                newParts.addAll(parts.subList(parts.indexOf(part) + 1, parts.size()));
                break;
            }

            newParts.add(part);
        }

        return new StyledText(newParts, clickEvents, hoverEvents);
    }

    /**
     * Replaces all occurrences of the given regex with the given replacement.
     * <p> Note that {@link PartStyle.StyleType.NONE} is used when matching and replacing.
     * @param regex the regex to replace
     * @param replacement the replacement
     * @return the new {@link StyledText}
     */
    public StyledText replaceAll(String regex, String replacement) {
        final Pattern pattern = Pattern.compile(regex);

        List<StyledTextPart> newParts = new ArrayList<>();

        for (StyledTextPart part : parts) {
            String partString = part.getString(null, PartStyle.StyleType.NONE);

            Matcher matcher = pattern.matcher(partString);

            if (matcher.find()) {
                String replacedString = matcher.replaceAll(replacement);

                newParts.add(
                        new StyledTextPart(replacedString, part.getPartStyle().getStyle(), null, Style.EMPTY));
            } else {
                newParts.add(part);
            }
        }

        return new StyledText(newParts, clickEvents, hoverEvents);
    }

    /**
     * Returns the parts of this {@link StyledText} as a {@link StyledText} array.
     * @return the array
     */
    public StyledText[] getPartsAsTextArray() {
        return parts.stream().map(StyledText::fromPart).toArray(StyledText[]::new);
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

        return new StyledText(newParts, clickEvents, hoverEvents);
    }

    public StyledText iterateBackwards(BiFunction<StyledTextPart, List<StyledTextPart>, IterationDecision> function) {
        List<StyledTextPart> newParts = new ArrayList<>();

        for (int i = parts.size() - 1; i >= 0; i--) {
            StyledTextPart part = parts.get(i);
            List<StyledTextPart> functionParts = new ArrayList<>();
            functionParts.add(part);
            IterationDecision decision = function.apply(part, functionParts);

            newParts.addAll(0, functionParts);

            if (decision == IterationDecision.BREAK) {
                // Add the rest of the parts
                newParts.addAll(0, parts.subList(0, i));
                break;
            }
        }

        return new StyledText(newParts, clickEvents, hoverEvents);
    }

    public StyledText withoutFormatting() {
        return iterate((part, functionParts) -> {
            functionParts.set(
                    0,
                    new StyledTextPart(part.getString(null, PartStyle.StyleType.NONE), Style.EMPTY, null, Style.EMPTY));
            return IterationDecision.CONTINUE;
        });
    }

    public boolean equalsString(String string) {
        return equalsString(string, PartStyle.StyleType.DEFAULT);
    }

    public boolean equalsString(String string, PartStyle.StyleType styleType) {
        return getString(styleType).equals(string);
    }

    public StyledTextPart getFirstPart() {
        if (parts.isEmpty()) {
            return null;
        }

        return parts.get(0);
    }

    public StyledTextPart getLastPart() {
        if (parts.isEmpty()) {
            return null;
        }

        return parts.get(parts.size() - 1);
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

    private StyledTextPart getPartBefore(StyledTextPart part) {
        int index = parts.indexOf(part);
        if (index == 0) {
            return null;
        }

        return parts.get(index - 1);
    }

    @Override
    public Iterator<StyledTextPart> iterator() {
        return parts.iterator();
    }

    @Override
    public String toString() {
        return "StyledText{'" + getString(PartStyle.StyleType.INCLUDE_EVENTS) + "'}";
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
