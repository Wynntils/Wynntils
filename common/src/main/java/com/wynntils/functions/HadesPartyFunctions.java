/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.services.hades.HadesUser;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import net.minecraft.client.resources.language.I18n;

public class HadesPartyFunctions {
    private abstract static class HadesPartyFunctionBase<T> extends Function<T> {
        @Override
        public T getValue(FunctionArguments arguments) {
            int index = arguments.getArgument("index").getIntegerValue();
            List<HadesUser> members = Services.Hades.getHadesUsers().toList();
            return !members.isEmpty() && index >= 0 && index < members.size()
                    ? processMember(members.get(index))
                    : whenAbsent();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("index", Integer.class, null)));
        }

        @Override
        public String getArgumentDescription(String argumentName) {
            return I18n.get("function.wynntils.hadesPartyFunctionBase.argument." + argumentName);
        }

        public abstract T processMember(HadesUser member);

        public abstract T whenAbsent();
    }

    public static class HadesPartyMemberHealthFunction extends HadesPartyFunctionBase<CappedValue> {
        @Override
        public CappedValue processMember(HadesUser member) {
            return member.getHealth();
        }

        @Override
        public CappedValue whenAbsent() {
            return CappedValue.EMPTY;
        }
    }

    public static class HadesPartyMemberManaFunction extends HadesPartyFunctionBase<CappedValue> {
        @Override
        public CappedValue processMember(HadesUser member) {
            return member.getMana();
        }

        @Override
        public CappedValue whenAbsent() {
            return CappedValue.EMPTY;
        }
    }

    public static class HadesPartyMemberLocationFunction extends HadesPartyFunctionBase<Location> {
        @Override
        public Location processMember(HadesUser member) {
            return member.getMapLocation().asLocation();
        }

        @Override
        public Location whenAbsent() {
            return new Location(0, 0, 0);
        }
    }

    public static class HadesPartyMemberNameFunction extends HadesPartyFunctionBase<String> {
        @Override
        public String processMember(HadesUser member) {
            return member.getName();
        }

        @Override
        public String whenAbsent() {
            return "";
        }
    }
}
