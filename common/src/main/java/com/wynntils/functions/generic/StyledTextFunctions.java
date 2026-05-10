/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.arguments.ListArgument;
import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.templates.Template;
import com.wynntils.core.consumers.functions.vm.FunctionNode;
import com.wynntils.core.consumers.functions.vm.TemplateCompiler;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ResolvableProfile;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class StyledTextFunctions {
    public static class StyledTextFunction extends GenericFunction<StyledText> implements FunctionNode {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            return StyledText.fromString(arguments.getArgument("value").getStringValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("st");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.getFirst().emit(mv);
            TemplateCompiler.emitToString(t1, mv);

            TemplateCompiler.emitInvokeStatic(mv, StyledText.class, "fromString", StyledText.class, String.class);

            return Type.getType(StyledText.class);
        }
    }

    public static class ConcatStyledTextFunction extends GenericFunction<StyledText> implements FunctionNode {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            List<StyledText> values = arguments.getArgument("values").getStyledTextList();
            return StyledText.join("", values);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new ListArgument<>("values", StyledText.class)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("concat_st");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            mv.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
            mv.visitInsn(Opcodes.DUP);

            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    "java/util/ArrayList",
                    "<init>",
                    "()V",
                    false
            );

            for (Expression arg : arguments) {

                mv.visitInsn(Opcodes.DUP);

                arg.emit(mv);

                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/util/ArrayList",
                        "add",
                        "(Ljava/lang/Object;)Z",
                        false
                );

                mv.visitInsn(Opcodes.POP);
            }

            mv.visitLdcInsn("");
            mv.visitInsn(Opcodes.SWAP);

            TemplateCompiler.emitInvokeStatic(mv, StyledText.class, "join", StyledText.class, String.class, Iterable.class);

            return Type.getType(StyledText.class);
        }
    }

    public static class WithColorFunction extends GenericFunction<StyledText> implements FunctionNode {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            CustomColor customColor = arguments.getArgument("color").getColorValue();

            return withColor(styledText, customColor);
        }

        public static StyledText withColor(StyledText styledText, CustomColor customColor) {
            return styledText.map(part -> {
                if (part.getPartStyle().getColor() != CustomColor.NONE) {
                    return part;
                }

                return part.withStyle(style -> style.withColor(customColor));
            });
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null), new Argument<>("color", CustomColor.class, null)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {

            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.ensureType(t1, StyledText.class);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.ensureType(t2, CustomColor.class);

            TemplateCompiler.emitInvokeStatic(mv, WithColorFunction.class, "withColor", StyledText.class, StyledText.class, CustomColor.class);

            return Type.getType(StyledText.class);
        }
    }

    public static class WithBoldFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            Boolean isBold = arguments.getArgument("isBold").getBooleanValue();

            return styledText.map(part -> part.withStyle(style -> style.withBold(isBold)));
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null), new Argument<>("isBold", Boolean.class, null)));
        }
    }

    public static class WithItalicFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            Boolean isItalic = arguments.getArgument("isItalic").getBooleanValue();

            return styledText.map(part -> part.withStyle(style -> style.withItalic(isItalic)));
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null), new Argument<>("isItalic", Boolean.class, null)));
        }
    }

    public static class WithStrikethroughFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            Boolean isStrikethrough = arguments.getArgument("isStrikethrough").getBooleanValue();

            return styledText.map(part -> part.withStyle(style -> style.withStrikethrough(isStrikethrough)));
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null),
                    new Argument<>("isStrikethrough", Boolean.class, null)));
        }
    }

    public static class WithObfuscatedFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            Boolean isObfuscated = arguments.getArgument("isObfuscated").getBooleanValue();

            return styledText.map(part -> part.withStyle(style -> style.withObfuscated(isObfuscated)));
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null),
                    new Argument<>("isObfuscated", Boolean.class, null)));
        }
    }

    public static class WithAtlasSpriteFontFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            String atlas = arguments.getArgument("atlas").getStringValue();
            String sprite = arguments.getArgument("sprite").getStringValue();
            Identifier atlasLocation = Identifier.tryParse(atlas);
            Identifier spriteLocation = Identifier.tryParse(sprite);

            if (atlasLocation == null || spriteLocation == null) return styledText;

            FontDescription fontDescription = new FontDescription.AtlasSprite(atlasLocation, spriteLocation);

            return styledText.map(part -> {
                if (part.getPartStyle().getFont() != FontDescription.DEFAULT) {
                    return part;
                }

                return part.withStyle(style -> style.withFont(fontDescription));
            });
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null),
                    new Argument<>("atlas", String.class, null),
                    new Argument<>("sprite", String.class, null)));
        }
    }

    public static class WithPlayerSpriteFontFunction extends GenericFunction<StyledText> implements FunctionNode {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            String uuid = arguments.getArgument("uuid").getStringValue();
            boolean hat = arguments.getArgument("hat").getBooleanValue();

            return withPlayerSpriteFont(styledText, uuid, hat);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null),
                    new Argument<>("uuid", String.class, null),
                    new Argument<>("hat", Boolean.class, null)));
        }


        public static StyledText withPlayerSpriteFont(StyledText styledText, String uuid, boolean hat) {
            UUID uuidObject;
            try {
                uuidObject = UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                return styledText;
            }

            FontDescription fontDescription =
                    new FontDescription.PlayerSprite(ResolvableProfile.createUnresolved(uuidObject), hat);

            return styledText.map(part -> {
                if (part.getPartStyle().getFont() != FontDescription.DEFAULT) {
                    return part;
                }

                return part.withStyle(style -> style.withFont(fontDescription));
            });
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {

            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.ensureType(t1, StyledText.class);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.ensureType(t2, String.class);

            Type t3 = arguments.get(2).emit(mv);
            TemplateCompiler.ensureType(t3, Type.BOOLEAN_TYPE);

            TemplateCompiler.emitInvokeStatic(mv, WithPlayerSpriteFontFunction.class, "withPlayerSpriteFont", StyledText.class, StyledText.class, String.class, boolean.class);

            return Type.getType(StyledText.class);
        }
    }

    public static class WithResourceFontFunction extends GenericFunction<StyledText> implements FunctionNode {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            String font = arguments.getArgument("font").getStringValue();
            return withResourceFont(styledText, font);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null), new Argument<>("font", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("with_font");
        }


        public static StyledText withResourceFont(StyledText styledText, String font) {
            Identifier fontLocation = Identifier.tryParse(font);

            if (fontLocation == null) return styledText;

            FontDescription fontDescription = new FontDescription.Resource(fontLocation);
            return styledText.map(part -> {
                if (part.getPartStyle().getFont() != FontDescription.DEFAULT) {
                    return part;
                }

                return part.withStyle(style -> style.withFont(fontDescription));
            });
        }
        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {

            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.ensureType(t1, StyledText.class);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.ensureType(t2, String.class);

            TemplateCompiler.emitInvokeStatic(mv, WithResourceFontFunction.class, "withResourceFont", StyledText.class, StyledText.class, String.class);

            return Type.getType(StyledText.class);
        }
    }

    public static class WithShadowColorFunction extends GenericFunction<StyledText> implements FunctionNode {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            CustomColor customColor = arguments.getArgument("color").getColorValue();
            return withShadowColor(styledText, customColor);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null), new Argument<>("color", CustomColor.class, null)));
        }

        public static StyledText withShadowColor(StyledText styledText, CustomColor customColor) {

            return styledText.map(part -> {
                if (part.getPartStyle().getShadowColor() != CustomColor.NONE) {
                    return part;
                }

                return part.withStyle(style -> style.withShadowColor(customColor));
            });
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {

            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.ensureType(t1, StyledText.class);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.ensureType(t2, CustomColor.class);

            TemplateCompiler.emitInvokeStatic(mv, WithShadowColorFunction.class, "withShadowColor", StyledText.class, StyledText.class, CustomColor.class);

            return Type.getType(StyledText.class);
        }
    }

    public static class WithUnderlinedFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            Boolean isUnderlined = arguments.getArgument("isUnderlined").getBooleanValue();

            return styledText.map(part -> part.withStyle(style -> style.withUnderlined(isUnderlined)));
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null),
                    new Argument<>("isUnderlined", Boolean.class, null)));
        }
    }
}
