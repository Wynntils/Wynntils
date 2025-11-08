/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.arguments.ListArgument;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.minecraft.network.chat.Component;

public class StringFunctions {
    public static class FormatFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return StringUtils.integerToShortString(
                    arguments.getArgument("value").getIntegerValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class FormatCappedFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            CappedValue value = arguments.getArgument("value").getCappedValue();
            return StringUtils.integerToShortString(value.current()) + "/"
                    + StringUtils.integerToShortString(value.max());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("value", CappedValue.class, null)));
        }
    }

    public static class FormatRangedFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            RangedValue value = arguments.getArgument("value").getRangedValue();
            return StringUtils.integerToShortString(value.low()) + "-" + StringUtils.integerToShortString(value.high());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("value", RangedValue.class, null)));
        }
    }

    public static class FormatDurationFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return StringUtils.formatDuration(arguments.getArgument("seconds").getLongValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("seconds", Number.class, null)));
        }
    }

    public static class FormatDateFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return StringUtils.formatDateTime(arguments.getArgument("timestamp").getLongValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("timestamp", Number.class, null)));
        }
    }

    public static class StringFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return arguments.getArgument("value").getValue().toString();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("str");
        }
    }

    public static class ConcatFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            List<String> values = arguments.getArgument("values").getStringList();

            return String.join("", values);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", String.class)));
        }
    }

    public static class StringEqualsFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return arguments
                    .getArgument("first")
                    .getStringValue()
                    .equals(arguments.getArgument("second").getStringValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("first", String.class, null), new Argument<>("second", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("eq_str");
        }
    }

    public static class StringContainsFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return arguments
                    .getArgument("source")
                    .getStringValue()
                    .contains(arguments.getArgument("substring").getStringValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("source", String.class, null), new Argument<>("substring", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("contains_str");
        }
    }

    public static class ParseIntegerFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            try {
                return Integer.parseInt(arguments.getArgument("value").getStringValue());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("parse_int");
        }
    }

    public static class ParseLongFunction extends GenericFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            try {
                return Long.parseLong(arguments.getArgument("value").getStringValue());
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", String.class, null)));
        }
    }

    public static class ParseDoubleFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            try {
                return Double.parseDouble(arguments.getArgument("value").getStringValue());
            } catch (NumberFormatException ignored) {
                return 0.0d;
            }
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", String.class, null)));
        }
    }

    public static class RepeatFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            String value = arguments.getArgument("value").getStringValue();
            int times = arguments.getArgument("count").getIntegerValue();

            return String.valueOf(value).repeat(Math.max(0, times));
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("value", String.class, null), new Argument<>("count", Integer.class, null)));
        }
    }

    public static class CappedStringFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            int current = arguments.getArgument("value").getCappedValue().current();
            int max = arguments.getArgument("value").getCappedValue().max();
            String delimiter = arguments.getArgument("delimiter").getStringValue();

            return String.format("%d%s%d", current, delimiter, max);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", CappedValue.class, null), new Argument<>("delimiter", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("cap_str", "str_cap");
        }
    }

    public static class LeadingZerosFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            int value = arguments.getArgument("value").getIntegerValue();
            int length = arguments.getArgument("length").getIntegerValue();

            return String.format("%0" + length + "d", value);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", Integer.class, null), new Argument<>("length", Integer.class, null)));
        }
    }

    public static class RegexMatchFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            String value = arguments.getArgument("source").getStringValue();
            String regex = arguments.getArgument("regex").getStringValue();

            try {
                return value.matches(regex);
            } catch (PatternSyntaxException ignored) {
                return false;
            }
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("source", String.class, null), new Argument<>("regex", String.class, null)));
        }
    }

    public static class RegexFindFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            String value = arguments.getArgument("source").getStringValue();
            Pattern regex = Pattern.compile(arguments.getArgument("regex").getStringValue());

            try {
                return regex.matcher(value).find();
            } catch (PatternSyntaxException ignored) {
                return false;
            }
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("source", String.class, null), new Argument<>("regex", String.class, null)));
        }
    }

    public static class RegexReplaceFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            String value = arguments.getArgument("source").getStringValue();
            String regex = arguments.getArgument("regex").getStringValue();
            String replacement = arguments.getArgument("replacement").getStringValue();

            try {
                return value.replaceAll(regex, replacement);
            } catch (PatternSyntaxException ignored) {
                return Component.translatable("function.wynntils.generic.regexReplace.syntaxError")
                        .toString();
            }
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("source", String.class, null),
                    new Argument<>("regex", String.class, null),
                    new Argument<>("replacement", String.class, null)));
        }
    }

    public static class ToRomanNumeralsFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            int number = arguments.getArgument("number").getIntegerValue();
            return MathUtils.toRoman(number);
        }

        @Override
        protected FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("number", Integer.class, null)));
        }
    }
}
