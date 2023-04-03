/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
import com.wynntils.core.chat.transcoder.PartStyle;
import com.wynntils.core.chat.transcoder.StyledText;
import com.wynntils.core.chat.transcoder.StyledTextPart;
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

public class TestStyledText {
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

        StyledText styledText = StyledText.fromComponent(component);

        Assertions.assertEquals(
                expectedIncludeEvents,
                styledText.getString(PartStyle.StyleType.INCLUDE_EVENTS),
                "StyleString.getString(INCLUDE_EVENTS) returned an unexpected value.");
        Assertions.assertEquals(
                expectedDefault,
                styledText.getString(PartStyle.StyleType.DEFAULT),
                "StyleString.getString(DEFAULT) returned an unexpected value.");
        Assertions.assertEquals(
                expectedNoFormat,
                styledText.getString(PartStyle.StyleType.NONE),
                "StyleString.getString(NONE) returned an unexpected value.");
    }

    @Test
    public void styledTextshouldProduceCorrectComponent() {
        final Component component = Component.literal("a").append(Component.literal("b"));

        StyledText styledText = StyledText.fromComponent(component);

        Assertions.assertEquals(
                component, styledText.getComponent(), "StyleString.getComponent() returned an unexpected value.");
    }

    @Test
    public void styledTextshouldProduceCorrectMatcher() {
        final Component component = Component.literal("This is a test string, where there are ")
                .append(Component.literal("multiple").withStyle(ChatFormatting.BOLD))
                .append(Component.literal(" components."));

        final Pattern unformattedPattern = Pattern.compile("This is a test string, where there are (.+) components\\.");
        final Pattern formattedPattern =
                Pattern.compile("This is a test string, where there are §l(.+)§r components\\.");

        final String expectedMatch = "multiple";

        StyledText styledText = StyledText.fromComponent(component);

        Matcher formattedMatcher = styledText.getMatcher(formattedPattern);
        Matcher unformattedMatcher = styledText.getMatcher(unformattedPattern, PartStyle.StyleType.NONE);

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
    public void styledTextshouldSplitCorrectly() {
        final Component component = Component.literal("This is a test string, where there are ")
                .append(Component.literal("multiple").withStyle(ChatFormatting.BOLD))
                .append(Component.literal(" components."));

        StyledText styledText = StyledText.fromComponent(component);

        Assertions.assertEquals(
                3, styledText.getPartCount(), "StyleString.getParts().size() returned an unexpected value.");

        final int splitAt = 22;

        styledText.splitAt(splitAt);

        Assertions.assertEquals(
                4,
                styledText.getPartCount(),
                "StyleString.splitAt() did not split the string correctly. The number of parts is incorrect.");
    }

    @Test
    public void styledTextincorrectSplitIndexShouldThrow() {
        final Component component = Component.literal("Test component");

        StyledText styledText = StyledText.fromComponent(component);

        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> styledText.splitAt(10000),
                "StyleString#splitAt() did not throw an exception when given a too big index.");
        Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> styledText.splitAt(-1),
                "StyleString#splitAt() did not throw an exception when given a negative index.");
    }

    @Test
    public void styledTextgetPartFindingShouldFind() {
        final String partText = "This is a test string, where there are ";
        final Component component = Component.literal(partText)
                .append(Component.literal("multiple").withStyle(ChatFormatting.BOLD))
                .append(Component.literal(" components."));

        final Pattern pattern = Pattern.compile("\\bthere\\b");

        StyledText styledText = StyledText.fromComponent(component);

        StyledTextPart partFinding = styledText.getPartFinding(pattern);

        Assertions.assertNotNull(
                partFinding, "StyleString.getPartFinding() returned null when it should have found a part.");

        String partString = partFinding.getString(null, PartStyle.StyleType.NONE);

        Assertions.assertEquals(
                partText, partString, "StyleString.getPartFinding() returned a part with the wrong text.");
    }

    @Test
    public void styledTextgetPartMatchingShouldFind() {
        final Component component = Component.literal("Test string")
                .append(Component.literal("Test string").withStyle(ChatFormatting.BOLD));

        final Pattern pattern = Pattern.compile("§lTest string");

        StyledText styledText = StyledText.fromComponent(component);

        StyledTextPart partMatching = styledText.getPartMatching(pattern);

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
