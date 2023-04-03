/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
import com.wynntils.core.chat.transcoder.PartStyle;
import com.wynntils.core.chat.transcoder.StyleString;
import com.wynntils.core.chat.transcoder.StyleStringPart;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestStyleString {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void advancedComponentTree_shouldProduceCorrectString() {
        final Component component = Component.empty()
                .append(Component.literal("italicred")
                        .withStyle(ChatFormatting.ITALIC)
                        .withStyle(ChatFormatting.RED)
                        .append(Component.literal("blue")
                                .withStyle(ChatFormatting.BLUE)
                                .withStyle(style -> style.withHoverEvent(
                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("hover")))))
                        .append(Component.literal("nonitalic").withStyle(Style.EMPTY.withItalic(false)))
                        .append(Component.literal("inherited")
                                .append(Component.literal("bold").withStyle(ChatFormatting.BOLD))))
                .append(Component.literal("after")
                        .withStyle(style ->
                                style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/command"))));

        final String expectedIncludeEvents = "§c§oitalicred§r§9§o§<1>blue§r§cnonitalic§oinherited§lbold§r§[1]after";
        final String expectedDefault = "§c§oitalicred§r§9§oblue§r§cnonitalic§oinherited§lbold§rafter";
        final String expectedNoFormat = "italicredbluenonitalicinheritedboldafter";

        StyleString styleString = StyleString.fromComponent(component);

        Assertions.assertEquals(
                expectedIncludeEvents,
                styleString.getString(PartStyle.StyleType.INCLUDE_EVENTS),
                "StyleString.getString(INCLUDE_EVENTS) returned an unexpected value.");
        Assertions.assertEquals(
                expectedDefault,
                styleString.getString(PartStyle.StyleType.DEFAULT),
                "StyleString.getString(DEFAULT) returned an unexpected value.");
        Assertions.assertEquals(
                expectedNoFormat,
                styleString.getString(PartStyle.StyleType.NONE),
                "StyleString.getString(NONE) returned an unexpected value.");
    }

    @Test
    public void styleString_shouldProduceCorrectComponent() {
        final Component component = Component.literal("a").append(Component.literal("b"));

        StyleString styleString = StyleString.fromComponent(component);

        Assertions.assertEquals(
                component, styleString.getComponent(), "StyleString.getComponent() returned an unexpected value.");
    }

    @Test
    public void styleString_shouldProduceCorrectMatcher() {
        final Component component = Component.literal("This is a test string, where there are ")
                .append(Component.literal("multiple").withStyle(ChatFormatting.BOLD))
                .append(Component.literal(" components."));

        final Pattern unformattedPattern = Pattern.compile("This is a test string, where there are (.+) components\\.");
        final Pattern formattedPattern =
                Pattern.compile("This is a test string, where there are §l(.+)§r components\\.");

        final String expectedMatch = "multiple";

        StyleString styleString = StyleString.fromComponent(component);

        Matcher formattedMatcher = styleString.getMatcher(formattedPattern);
        Matcher unformattedMatcher = styleString.getMatcher(unformattedPattern, PartStyle.StyleType.NONE);

        Assertions.assertTrue(formattedMatcher.matches(), "StyleString.matches(DEFAULT) returned an unexpected value.");
        Assertions.assertEquals(
                expectedMatch,
                formattedMatcher.group(1),
                "StyleString.matches(DEFAULT).group() returned an unexpected value.");
        Assertions.assertTrue(unformattedMatcher.matches(), "StyleString.matches(NONE) returned an unexpected value.");
        Assertions.assertEquals(
                expectedMatch,
                unformattedMatcher.group(1),
                "StyleString.matches(NONE).group() returned an unexpected value.");
    }

    @Test
    public void styleString_shouldSplitCorrectly() {
        final Component component = Component.literal("This is a test string, where there are ")
                .append(Component.literal("multiple").withStyle(ChatFormatting.BOLD))
                .append(Component.literal(" components."));

        StyleString styleString = StyleString.fromComponent(component);

        Assertions.assertEquals(
                3, styleString.getPartCount(), "StyleString.getParts().size() returned an unexpected value.");

        final int splitAt = 22;

        styleString.splitAt(splitAt);

        Assertions.assertEquals(
                4,
                styleString.getPartCount(),
                "StyleString.splitAt() did not split the string correctly. The number of parts is incorrect.");
    }

    @Test
    public void styleString_incorrectSplitIndexShouldThrow() {
        final Component component = Component.literal("Test component");

        StyleString styleString = StyleString.fromComponent(component);

        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> styleString.splitAt(10000),
                "StyleString#splitAt() did not throw an exception when given a too big index.");
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> styleString.splitAt(-1),
                "StyleString#splitAt() did not throw an exception when given a negative index.");
    }

    @Test
    public void styleString_getPartFindingShouldFind() {
        final String partText = "This is a test string, where there are ";
        final Component component = Component.literal(partText)
                .append(Component.literal("multiple").withStyle(ChatFormatting.BOLD))
                .append(Component.literal(" components."));

        final Pattern pattern = Pattern.compile("\\bthere\\b");

        StyleString styleString = StyleString.fromComponent(component);

        StyleStringPart partFinding = styleString.getPartFinding(pattern);

        Assertions.assertNotNull(
                partFinding, "StyleString.getPartFinding() returned null when it should have found a part.");

        String partString = partFinding.getString(null, PartStyle.StyleType.NONE);

        Assertions.assertEquals(
                partText, partString, "StyleString.getPartFinding() returned a part with the wrong text.");
    }

    @Test
    public void styleString_getPartMatchingShouldFind() {
        final Component component = Component.literal("Test string")
                .append(Component.literal("Test string").withStyle(ChatFormatting.BOLD));

        final Pattern pattern = Pattern.compile("§lTest string");

        StyleString styleString = StyleString.fromComponent(component);

        StyleStringPart partMatching = styleString.getPartMatching(pattern);

        Assertions.assertNotNull(
                partMatching, "StyleString.getPartMatching() returned null when it should have found a part.");

        String partString = partMatching.getString(null, PartStyle.StyleType.NONE);

        Assertions.assertEquals(
                "Test string", partString, "StyleString.getPartMatching() returned a part with the wrong text.");
        Assertions.assertTrue(
                partMatching.getPartStyle().isBold(),
                "StyleString.getPartMatching() returned a part with the wrong style.");
    }
}
