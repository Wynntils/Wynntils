/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.vm.FunctionNode;
import com.wynntils.core.consumers.functions.vm.TemplateCompiler;
import com.wynntils.models.players.WynntilsUser;
import com.wynntils.utils.mc.McUtils;

import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class SocialFunctions {
    public static class FriendsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Friends.getFriends().size();
        }
    }

    public static class PartyMembersFunction extends Function<Integer> implements FunctionNode {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return getPartyMembersSize(arguments.getArgument("includeOffline").getBooleanValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("includeOffline", Boolean.class, true)));
        }

        public static int getPartyMembersSize(boolean includeOffline) {
            if(includeOffline) {
                return Models.Party.getPartyMembers().size();
            } else {
                return Models.Party.getPartyMembers().size()
                        - Models.Party.getOfflineMembers().size();
            }
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.getFirst().emit(mv);
            TemplateCompiler.emitCastToInt(t1, mv);

            TemplateCompiler.emitInvokeStatic(mv, PartyMembersFunction.class, "getPartyMembersSize", int.class, boolean.class);

            return Type.INT_TYPE;
        }
    }

    public static class PartyLeaderFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.Party.getPartyLeader().orElse("");
        }
    }

    public static class IsFriendFunction extends Function<Boolean> implements FunctionNode {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return isFriend(arguments.getArgument("player").getStringValue());
        }

        public static boolean isFriend(String name) {
            return Models.Friends.isFriend(name);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("player", String.class, null)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {

            Type t1 = arguments.getFirst().emit(mv);
            TemplateCompiler.ensureType(t1, String.class);

            TemplateCompiler.emitInvokeStatic(mv, IsFriendFunction.class, "isFriend", boolean.class, String.class);

            return Type.BOOLEAN_TYPE;
        }
    }

    public static class IsPartyMemberFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.Party.getPartyMembers()
                    .contains(arguments.getArgument("player").getStringValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("player", String.class, null)));
        }
    }

    public static class WynntilsRoleFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            WynntilsUser player = Models.Player.getWynntilsUser(McUtils.player());
            if (player == null) return "";

            Component component = player.accountType().getComponent();
            if (component == null) return "";

            return component.getString();
        }
    }

    public static class PlayerNameFunction extends Function<String> implements FunctionNode {
        @Override
        public String getValue(FunctionArguments arguments) {
            return McUtils.playerName();
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            TemplateCompiler.emitInvokeStatic(mv, McUtils.class, "playerName", String.class);

            return Type.getType(String.class);
        }
    }

    public static class PlayerUuidFunction extends Function<String> implements FunctionNode {
        @Override
        public String getValue(FunctionArguments arguments) {
            return getPlayerUUID();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("uuid");
        }


        public static String getPlayerUUID() {
            return McUtils.player().getStringUUID();
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {

            TemplateCompiler.emitInvokeStatic(mv, PlayerUuidFunction.class, "getPlayerUUID", String.class);

            return Type.getType(String.class);
        }
    }
}
