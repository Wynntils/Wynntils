package com.wynntils.functions.generic;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.functions.ActivityFunctions;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.colors.CustomColor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class StyledTextFunctionsTest {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(ActivityFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void createsConcatenatesAndRepeatsStyledText() {
        StyledText a = StyledTextFunctions.styledTextFunction("a");
        StyledText b = StyledTextFunctions.styledTextFunction("b");

        assertNotNull(a);
        assertNotNull(StyledTextFunctions.concatStyledTextFunction(a, b));
        assertNotNull(StyledTextFunctions.concatStyledTextFunction(a, b, a));
        assertNotNull(StyledTextFunctions.repeatStyledTextFunction(a, 3));
    }

    @Test
    void appliesTextDecorations() {
        StyledText text = StyledTextFunctions.styledTextFunction("text");

        assertNotNull(StyledTextFunctions.withBoldFunction(text));
        assertNotNull(StyledTextFunctions.withBoldFunction(text, false));
        assertNotNull(StyledTextFunctions.withItalicFunction(text));
        assertNotNull(StyledTextFunctions.withItalicFunction(text, false));
        assertNotNull(StyledTextFunctions.withStrikeThroughFunction(text));
        assertNotNull(StyledTextFunctions.withStrikeThroughFunction(text, false));
        assertNotNull(StyledTextFunctions.withObfuscatedFunction(text));
        assertNotNull(StyledTextFunctions.withObfuscatedFunction(text, false));
        assertNotNull(StyledTextFunctions.withUnderlinedFunction(text));
        assertNotNull(StyledTextFunctions.withUnderlinedFunction(text, false));
    }

    @Test
    void appliesColorsAndValidFonts() {
        StyledText text = StyledTextFunctions.styledTextFunction("text");

        assertNotNull(StyledTextFunctions.withColorFunction(text, new CustomColor(10, 20, 30)));
        assertNotNull(StyledTextFunctions.withShadowColorFunction(text, new CustomColor(30, 20, 10)));
        assertNotNull(StyledTextFunctions.withResourceFontFunction(text, "minecraft:default"));
        assertNotNull(StyledTextFunctions.withAtlasSpriteFontFunction(
                text, "minecraft:blocks", "minecraft:stone"));
        assertNotNull(StyledTextFunctions.withPlayerSpriteFontFunction(
                text, "123e4567-e89b-12d3-a456-426614174000", true));
    }

    @Test
    void invalidFontInputsReturnOriginalText() {
        StyledText text = StyledTextFunctions.styledTextFunction("text");

        assertSame(text, StyledTextFunctions.withResourceFontFunction(text, "invalid id"));
        assertSame(text, StyledTextFunctions.withAtlasSpriteFontFunction(text, "invalid id", "minecraft:stone"));
        assertSame(text, StyledTextFunctions.withPlayerSpriteFontFunction(text, "not-a-uuid", false));
    }
}
