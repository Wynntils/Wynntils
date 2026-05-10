/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.vm.FunctionNode;
import com.wynntils.core.consumers.functions.vm.TemplateCompiler;
import com.wynntils.services.hades.HadesUser;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CappedValue;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.resources.language.I18n;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class HadesPartyFunctions {
    public abstract static class HadesPartyFunctionBase<T> extends Function<T> implements FunctionNode {
        @Override
        public T getValue(FunctionArguments arguments) {
            int index = arguments.getArgument("index").getIntegerValue();
            return hadesPartyFunctionGetValue(index, this);
        }

        public static <K> K hadesPartyFunctionGetValue(int index, HadesPartyFunctionBase<K> func) {
            List<HadesUser> members = Models.War.getHadesUsers();

            // If there are no War members get regular party members and order them
            if (members.isEmpty()) {
                List<String> partyMembers = Models.Party.getPartyMembers();
                members = Services.Hades.getHadesUsers()
                        .filter(hadesUser -> partyMembers.contains(hadesUser.getName()))
                        .sorted(Comparator.comparing(hadesUser -> partyMembers.indexOf(hadesUser.getName())))
                        .toList();
            }
            return !members.isEmpty() && index >= 0 && index < members.size()
                    ? func.processMember(members.get(index))
                    : func.whenAbsent();
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

        public abstract Class<?> getReturnType();

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {

            Type t1 = arguments.getFirst().emit(mv);
            TemplateCompiler.ensureType(t1, Type.INT_TYPE);

            Type returnType = Type.getType(getReturnType());

            mv.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    Type.getInternalName(getClass()),
                    "INSTANCE",
                    Type.getDescriptor(getClass())
            );

            TemplateCompiler.emitInvokeStatic(
                    mv,
                    HadesPartyFunctionBase.class,
                    "hadesPartyFunctionGetValue",
                    Object.class,
                    int.class,
                    HadesPartyFunctionBase.class
            );

            mv.visitTypeInsn(Opcodes.CHECKCAST, returnType.getInternalName());

            return returnType;
        }
    }

    public static class HadesPartyMemberHealthFunction extends HadesPartyFunctionBase<CappedValue> {

        public static final HadesPartyMemberHealthFunction INSTANCE = new HadesPartyMemberHealthFunction();
        @Override
        public CappedValue processMember(HadesUser member) {
            return member.getHealth();
        }

        @Override
        public CappedValue whenAbsent() {
            return CappedValue.EMPTY;
        }

        @Override
        public Class<?> getReturnType() {
            return CappedValue.class;
        }
    }

    public static class HadesPartyMemberManaFunction extends HadesPartyFunctionBase<CappedValue> {
        public static final HadesPartyMemberManaFunction INSTANCE = new HadesPartyMemberManaFunction();
        @Override
        public CappedValue processMember(HadesUser member) {
            return member.getMana();
        }

        @Override
        public CappedValue whenAbsent() {
            return CappedValue.EMPTY;
        }

        @Override
        public Class<?> getReturnType() {
            return CappedValue.class;
        }
    }

    public static class HadesPartyMemberLocationFunction extends HadesPartyFunctionBase<Location> {
        public static final HadesPartyMemberLocationFunction INSTANCE = new HadesPartyMemberLocationFunction();
        @Override
        public Location processMember(HadesUser member) {
            return member.getMapLocation().asLocation();
        }

        @Override
        public Location whenAbsent() {
            return new Location(0, 0, 0);
        }

        @Override
        public Class<?> getReturnType() {
            return Location.class;
        }
    }

    public static class HadesPartyMemberNameFunction extends HadesPartyFunctionBase<String> {
        public static final HadesPartyMemberNameFunction INSTANCE = new HadesPartyMemberNameFunction();
        @Override
        public String processMember(HadesUser member) {
            return member.getName();
        }

        @Override
        public String whenAbsent() {
            return "";
        }

        @Override
        public Class<?> getReturnType() {
            return String.class;
        }
    }

    public static class HadesPartyMemberUuidFunction extends HadesPartyFunctionBase<String> {
        public static final HadesPartyMemberUuidFunction INSTANCE = new HadesPartyMemberUuidFunction();
        @Override
        public String processMember(HadesUser member) {
            return member.getUuid().toString();
        }

        @Override
        public String whenAbsent() {
            return "";
        }

        @Override
        public Class<?> getReturnType() {
            return String.class;
        }
    }
}
