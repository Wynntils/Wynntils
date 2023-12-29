/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.models.items.annotators.gui.AbilityTreeAnnotator;
import java.lang.reflect.Field;
import java.util.regex.Pattern;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestRegex {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    public static final class PatternTester {
        private final Class<?> clazz;
        private final String fieldName;
        private final Pattern pattern;

        public PatternTester(Class<?> clazz, String fieldName) {
            this.clazz = clazz;
            this.fieldName = fieldName;
            Pattern pattern = null;

            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                pattern = (Pattern) field.get(null);
            } catch (NoSuchFieldException e) {
                Assertions.fail("Pattern field " + clazz.getSimpleName() + "." + fieldName + " does not exist");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            this.pattern = pattern;
        }

        public void shouldMatch(String s) {
            Assertions.assertTrue(
                    pattern.matcher(s).matches(),
                    "Regex failure: " + clazz.getSimpleName() + "." + fieldName + " should have matched " + s
                            + ", but it did not.");
        }

        public void shouldNotMatch(String s) {
            Assertions.assertFalse(
                    pattern.matcher(s).matches(),
                    "Regex failure: " + clazz.getSimpleName() + "." + fieldName + " should NOT have matched " + s
                            + ", but it did.");
        }
    }

    @Test
    public void AbilityTreeAnnotator_TREE_ABILITY_POINTS_PATTERN() {
        PatternTester p = new PatternTester(AbilityTreeAnnotator.class, "TREE_ABILITY_POINTS_PATTERN");
        p.shouldMatch("§b✦ Available Points: §f0§7/45");
    }
}
