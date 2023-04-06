/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text;

import com.wynntils.utils.wynn.WynnUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class StyledText2 {
    public static final StyledText2 EMPTY = StyledText2.fromString("");

    private final String str;

    private StyledText2(String str) {
        this.str = str;
    }

    public static StyledText2 fromString(String s) {
        return new StyledText2(s);
    }

    public static StyledText2 fromComponentIgnoringComponentStylesAndJustUsingFormattingCodes(Component component) {
        return StyledText2.fromString(component.getString());
    }

    public String getInternalCodedStringRepresentation() {
        return str;
    }

    public String getUnformattedString() {
        return ChatFormatting.stripFormatting(str);
    }

    public MutableComponent asSingleLiteralComponentWithCodedString() {
        return Component.literal(str);
    }

    public StyledText2 getNormalized() {
        return StyledText2.fromString(WynnUtils.normalizeBadString(str));
    }

    public StyledText2 trim() {
        return StyledText2.fromString(str.trim());
    }

    public boolean isEmpty() {
        return str.isEmpty();
    }

    public boolean isBlank() {
        return str.isBlank();
    }

    public Matcher getMatcher(Pattern pattern) {
        return pattern.matcher(str);
    }

    public boolean contains(StyledText2 string) {
        return str.contains(string.str);
    }

    public boolean contains(String string) {
        return str.contains(string);
    }

    public boolean startsWith(StyledText2 prefix) {
        return str.startsWith(prefix.str);
    }

    public boolean startsWith(String prefix) {
        return str.startsWith(prefix);
    }

    public boolean endsWith(StyledText2 suffix) {
        return str.endsWith(suffix.str);
    }

    public boolean endsWith(String suffix) {
        return str.endsWith(suffix);
    }

    public static StyledText2 join(String delimiter, List<StyledText2> strings) {
        return StyledText2.fromString(
                String.join(delimiter, strings.stream().map(s -> s.str).toList()));
    }

    public StyledText2[] split(String regex) {
        return Arrays.stream(str.split(regex)).map(StyledText2::fromString).toArray(StyledText2[]::new);
    }

    public static StyledText2 join(String delimiter, StyledText2[] strings) {
        return StyledText2.fromString(
                String.join(delimiter, Arrays.stream(strings).map(s -> s.str).toList()));
    }

    public static StyledText2 concat(StyledText2... str) {
        return StyledText2.fromString(Arrays.stream(str).map(s -> s.str).collect(Collectors.joining()));
    }

    public StyledText2 prepend(StyledText2 prefix) {
        return prefix.append(str);
    }

    public StyledText2 prepend(String prefix) {
        return StyledText2.fromString(prefix + str);
    }

    public StyledText2 append(StyledText2 suffix) {
        return StyledText2.fromString(str + suffix.str);
    }

    public StyledText2 append(String suffix) {
        return StyledText2.fromString(str + suffix);
    }

    @Override
    public String toString() {
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StyledText2 that = (StyledText2) o;
        return Objects.equals(str, that.str);
    }

    @Override
    public int hashCode() {
        return Objects.hash(str);
    }
}
