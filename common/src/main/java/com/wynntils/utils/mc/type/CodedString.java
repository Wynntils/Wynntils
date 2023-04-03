/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc.type;

import com.wynntils.utils.wynn.WynnUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class CodedString {
    public static final CodedString EMPTY = new CodedString("");

    private final String str;

    public CodedString(String str) {
        this.str = str;
    }

    public static CodedString of(String s) {
        return new CodedString(s);
    }

    public static CodedString fromComponent(Component component) {
        return CodedString.of(component.getString());
    }

    public static CodedString join(List<CodedString> strings, String delimiter) {
        return CodedString.of(
                String.join(delimiter, strings.stream().map(CodedString::str).toList()));
    }

    public static CodedString join(CodedString[] strings, String delimiter) {
        return CodedString.of(String.join(
                delimiter, Arrays.stream(strings).map(CodedString::str).toList()));
    }

    public CodedString[] split(String regex) {
        return Arrays.stream(str.split(regex)).map(CodedString::of).toArray(CodedString[]::new);
    }

    public MutableComponent asComponent() {
        return Component.literal(str);
    }

    public String str() {
        return str;
    }

    public CodedString getNormalized() {
        return new CodedString(WynnUtils.normalizeBadString(str));
    }

    public Matcher match(Pattern pattern) {
        return pattern.matcher(stripToVanillaEncoding(str));
    }

    private String stripToVanillaEncoding(String string) {
        return string;
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

    public String withoutFormatting() {
        return ChatFormatting.stripFormatting(str);
    }
}
