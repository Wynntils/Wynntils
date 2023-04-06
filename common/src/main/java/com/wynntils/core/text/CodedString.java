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

/**
 * This should not be used for new code. All uses of this class should be transitioned to
 * use {@link StyledText} instead.
 */
@Deprecated
public class CodedString {
    public static final CodedString EMPTY = CodedString.fromString("");

    private final String str;

    private CodedString(String str) {
        this.str = str;
    }

    /**
     * This should not be used for new code. All uses of this class should be transitioned to
     * use {@link StyledText} instead.
     */
    @Deprecated
    public static CodedString fromString(String s) {
        return new CodedString(s);
    }

    /**
     * This should not be used for new code. All uses of this class should be transitioned to
     * use {@link StyledText} instead.
     */
    @Deprecated
    public static CodedString fromComponentIgnoringComponentStylesAndJustUsingFormattingCodes(Component component) {
        return CodedString.fromString(component.getString());
    }

    public static CodedString fromStyledText(StyledText styledText) {
        return fromString(styledText.getString(PartStyle.StyleType.FULL));
    }

    /**
     * This exposes the internal representation, and should be avoided.
     */
    @Deprecated
    public String getInternalCodedStringRepresentation() {
        return str;
    }

    public String getUnformattedString() {
        return ChatFormatting.stripFormatting(str);
    }

    public MutableComponent asSingleLiteralComponentWithCodedString() {
        return Component.literal(str);
    }

    public CodedString getNormalized() {
        return CodedString.fromString(WynnUtils.normalizeBadString(str));
    }

    public CodedString trim() {
        return CodedString.fromString(str.trim());
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

    public boolean contains(CodedString string) {
        return str.contains(string.str);
    }

    public boolean contains(String string) {
        return str.contains(string);
    }

    public boolean startsWith(CodedString prefix) {
        return str.startsWith(prefix.str);
    }

    public boolean startsWith(String prefix) {
        return str.startsWith(prefix);
    }

    public boolean endsWith(CodedString suffix) {
        return str.endsWith(suffix.str);
    }

    public boolean endsWith(String suffix) {
        return str.endsWith(suffix);
    }

    public static CodedString join(String delimiter, List<CodedString> strings) {
        return CodedString.fromString(
                String.join(delimiter, strings.stream().map(s -> s.str).toList()));
    }

    public CodedString[] split(String regex) {
        return Arrays.stream(str.split(regex)).map(CodedString::fromString).toArray(CodedString[]::new);
    }

    public static CodedString join(String delimiter, CodedString[] strings) {
        return CodedString.fromString(
                String.join(delimiter, Arrays.stream(strings).map(s -> s.str).toList()));
    }

    public static CodedString concat(CodedString... str) {
        return CodedString.fromString(Arrays.stream(str).map(s -> s.str).collect(Collectors.joining()));
    }

    public CodedString prepend(CodedString prefix) {
        return prefix.append(str);
    }

    public CodedString prepend(String prefix) {
        return CodedString.fromString(prefix + str);
    }

    public CodedString append(CodedString suffix) {
        return CodedString.fromString(str + suffix.str);
    }

    public CodedString append(String suffix) {
        return CodedString.fromString(str + suffix);
    }

    @Override
    public String toString() {
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodedString that = (CodedString) o;
        return Objects.equals(str, that.str);
    }

    @Override
    public int hashCode() {
        return Objects.hash(str);
    }
}
