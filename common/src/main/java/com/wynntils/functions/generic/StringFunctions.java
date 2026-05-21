/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.RangedValue;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.minecraft.network.chat.Component;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class StringFunctions {
    @TemplateFunction(name = "format")
    public static String formatFunction(int value) {
        return StringUtils.integerToShortString(value);
    }


    @TemplateFunction(name = "format_capped")
    public static String formatCappedFunction(CappedValue value) {
        return StringUtils.integerToShortString(value.current()) + "/" + StringUtils.integerToShortString(value.max());
    }

    @TemplateFunction(name = "format_ranged")
    public static String formatRangedFunction(RangedValue value) {
        return StringUtils.integerToShortString(value.low()) + "-" + StringUtils.integerToShortString(value.high());

    }

    @TemplateFunction(name = "format_duration")
    public static String formatDurationFunction(long seconds) {
        return StringUtils.formatDuration(seconds);

    }


    @TemplateFunction(name = "format_date")
    public static String formatDateFunction(long timestamp) {
        return StringUtils.formatDateTime(timestamp);

    }

    @TemplateFunction(name = "string", aliases = "str")
    public static String stringFunction(Object value) {
        return value.toString();
    }


    @TemplateFunction(name = "concat")
    public static String concatFunction(String a, String b) {
        return a + b;
    }


    @TemplateFunction(name = "concat")
    public static String concatFunction(String... values) {
        String sum = "";
        for (String value : values) {
            sum += value;
        }
        return sum;
    }


    @TemplateFunction(name = "string_equals", aliases = "eq_str")
    public static boolean stringEqualsFunction(String first, String second) {
        return first.equals(second);
    }


    @TemplateFunction(name = "string_contains", aliases = "contains_str")
    public static boolean stringContainsFunction(String haystack, String needle) {
        return haystack.contains(needle);
    }


    @TemplateFunction(name = "parse_integer", aliases = "parse_int")
    public static int parseIntegerFunction(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    @TemplateFunction(name = "parse_long")
    public static long parseLongFunction(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }


    @TemplateFunction(name = "parse_double")
    public static double parseDoubleFunction(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return 0.0d;
        }
    }


    @TemplateFunction(name = "repeat")
    public static String repeat(String value, int times) {
        return String.valueOf(value).repeat(Math.max(0, times));
    }


    @TemplateFunction(name = "capped_string", aliases = {"cap_str", "str_cap"})
    public static String cappedStringFunction(CappedValue value, String delimiter) {
        return String.format("%d%s%d", value.current(), delimiter, value.max());
    }


    @TemplateFunction(name = "leading_zeros")
    public static String leadingZerosFunction(int value, int length) {
        return String.format("%0" + length + "d", value);
    }


    @TemplateFunction(name = "regex_match")
    public static boolean regexMatchFunction(String value, String regex) {
        try {
            return value.matches(regex);
        } catch (PatternSyntaxException ignored) {
            return false;
        }
    }


    @TemplateFunction(name = "regex_find")
    public static boolean regexFindFunction(String value, String regex) {
        try {
            return Pattern.compile(regex).matcher(value).find();
        } catch (PatternSyntaxException ignored) {
            return false;
        }
    }


    @TemplateFunction(name = "regex_replace")
    public static String regexReplaceFunction(String value, String regex, String replacement) {
        try {
            return value.replaceAll(regex, replacement);
        } catch (PatternSyntaxException ignored) {
            return Component.translatable("function.wynntils.generic.regexReplace.syntaxError").toString();
        }
    }


    @TemplateFunction(name = "to_roman_numerals")
    public static String toRomanNumeralsFunction(int number) {
        return MathUtils.toRoman(number);
    }


    @TemplateFunction(name = "from_codepoint")
    public static String fromCodepointFunction(int codepoint) {
        try {
            return new String(Character.toChars(codepoint));
        } catch (IllegalArgumentException ex) {
            return "Invalid Codepoint";
        }
    }

}
