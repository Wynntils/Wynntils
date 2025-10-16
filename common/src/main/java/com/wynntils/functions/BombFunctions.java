/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.worlds.type.BombInfo;
import com.wynntils.models.worlds.type.BombSortOrder;
import com.wynntils.utils.type.Time;
import java.util.List;
import net.minecraft.client.resources.language.I18n;

public class BombFunctions {
    private abstract static class BombFunctionBase<T> extends Function<T> {
        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("index", Integer.class, null),
                    new Argument<>("group", Boolean.class, null),
                    new Argument<>("sortOrder", String.class, null)));
        }

        @Override
        public T getValue(FunctionArguments arguments) {
            int index = arguments.getArgument("index").getIntegerValue();
            boolean group = arguments.getArgument("group").getBooleanValue();
            BombSortOrder sortOrder =
                    BombSortOrder.fromString(arguments.getArgument("sortOrder").getStringValue());
            List<BombInfo> bombInfo =
                    Models.Bomb.getBombBellStream(group, sortOrder).toList();

            return (!bombInfo.isEmpty() && index >= 0 && index < bombInfo.size())
                    ? processInfo(bombInfo.get(index))
                    : invalidValue();
        }

        @Override
        public String getArgumentDescription(String argumentName) {
            return I18n.get("function.wynntils.bombFunctionBase.argument." + argumentName);
        }

        public abstract T processInfo(BombInfo info);

        public abstract T invalidValue();
    }

    public static class BombFormattedStringFunction extends BombFunctionBase<String> {
        @Override
        public String processInfo(BombInfo info) {
            return info.asString();
        }

        @Override
        public String invalidValue() {
            return "";
        }
    }

    public static class BombTypeFunction extends BombFunctionBase<String> {
        @Override
        public String processInfo(BombInfo info) {
            return info.bomb().getDisplayName();
        }

        @Override
        public String invalidValue() {
            return "";
        }
    }

    public static class BombWorldFunction extends BombFunctionBase<String> {
        @Override
        public String processInfo(BombInfo info) {
            return info.server();
        }

        @Override
        public String invalidValue() {
            return "";
        }
    }

    public static class BombStartTimeFunction extends BombFunctionBase<Time> {
        @Override
        public Time processInfo(BombInfo info) {
            return Time.of(info.startTime());
        }

        @Override
        public Time invalidValue() {
            return Time.NONE;
        }
    }

    public static class BombLengthFunction extends BombFunctionBase<Float> {
        @Override
        public Float processInfo(BombInfo info) {
            return info.length();
        }

        @Override
        public Float invalidValue() {
            return -1f;
        }
    }

    public static class BombEndTimeFunction extends BombFunctionBase<Time> {
        @Override
        public Time processInfo(BombInfo info) {
            return Time.of(info.endTime());
        }

        @Override
        public Time invalidValue() {
            return Time.NONE;
        }
    }

    public static class BombOwnerFunction extends BombFunctionBase<String> {
        @Override
        public String processInfo(BombInfo info) {
            return info.user();
        }

        @Override
        public String invalidValue() {
            return "";
        }
    }

    public static class BombRemainingTimeFunction extends BombFunctionBase<Time> {
        @Override
        public Time processInfo(BombInfo info) {
            return Time.of(info.getRemainingLong());
        }

        @Override
        public Time invalidValue() {
            return Time.NONE;
        }
    }
}
