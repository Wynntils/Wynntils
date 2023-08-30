/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.features.chat.GuildRankReplacementFeature;
import com.wynntils.features.ui.BulkBuyFeature;
import com.wynntils.models.items.annotators.gui.ArchetypeAbilitiesAnnotator;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestRegexs {
    @Test
    public void testArchetypeName() {
        Pattern pattern =
                TestPrivateFieldsUtil.getPrivateStaticRegexPattern(ArchetypeAbilitiesAnnotator.class, "ARCHETYPE_NAME");
        assertMatchesAny(pattern, "§e§lBoltslinger Archetype").assertGroupIs(1, "e");
        assertMatchesAny(pattern, "§d§lSharpshooter Archetype").assertGroupIs(1, "d");
        assertMatchesAny(pattern, "§2§lTrapper Archetype").assertGroupIs(1, "2");
        assertMatchesAny(pattern, "§d§lLight Bender Archetype").assertGroupIs(1, "d");
    }

    @Test
    public void testArchetypePattern() {
        Pattern pattern = TestPrivateFieldsUtil.getPrivateStaticRegexPattern(
                ArchetypeAbilitiesAnnotator.class, "ARCHETYPE_PATTERN");
        assertMatchesAny(pattern, "§a✔ §7Unlocked Abilities: §f14§7/15")
                .assertGroupIs(1, "14")
                .assertGroupIs(2, "15");
        assertMatchesAny(pattern, "§a✔ §7Unlocked Abilities: §f2§7/16")
                .assertGroupIs(1, "2")
                .assertGroupIs(2, "16");
        assertMatchesAny(pattern, "§a✔ §7Unlocked Abilities: §f0§7/15")
                .assertGroupIs(1, "0")
                .assertGroupIs(2, "15");
    }

    @Test
    public void testPricePattern() {
        Pattern pattern = TestPrivateFieldsUtil.getPrivateStaticRegexPattern(BulkBuyFeature.class, "PRICE_PATTERN");
        assertMatchesAny(pattern, "§6 - §a✔ §f10§7²");
        assertMatchesAny(pattern, "§6 - §a✔ §f1483§7²");
        assertMatchesAny(pattern, "§6 - §c✖ §f244§7²");
        assertMatchesAny(pattern, "§6 - §c✖ §f1§7²");
        assertMatchesAny(pattern, "§6 - §a✔ §f24§7²");
    }

    @Test
    public void testGuildMessagePattern() {
        Pattern pattern = TestPrivateFieldsUtil.getPrivateStaticRegexPattern(
                GuildRankReplacementFeature.class, "GUILD_MESSAGE_PATTERN");
        assertMatchesAny(pattern, "§3[§b★★★★★§3§oDisco reroller§3]");
        assertMatchesAny(pattern, "§3[§b★★★★★§3§oafKing§r§3]§");
        assertMatchesAny(pattern, "§3[§b★★★★§3§obol§r§3] ");
    }

    @Test
    public void testRecruitUsernamePattern() {
        Pattern pattern = TestPrivateFieldsUtil.getPrivateStaticRegexPattern(
                GuildRankReplacementFeature.class, "RECRUIT_USERNAME_PATTERN");
        assertMatchesAny(pattern, "§3[_user0name_").assertGroupIs(1, "_user0name_");
        assertMatchesNone(pattern, "§3[WAR]");
        assertMatchesAny(pattern, "§3[ummmmmmmmm]").assertGroupIs(1, "ummmmmmmmm");
    }

    public record MatcherAssertions(Matcher matcher) {
        public MatcherAssertions assertGroupIs(int group, String expected) {
            String resultGroup = matcher.group(group);
            Assertions.assertEquals(
                    expected,
                    resultGroup,
                    String.format(
                            "asserted regex group should be equal to string,\ngroup:\t\t%d\nexpected:\t%s\nresult:\t\t%s%n",
                            group, expected, resultGroup));
            return this;
        }

        public MatcherAssertions assertGroupIs(String group, String expected) {
            String resultGroup = matcher.group(group);
            Assertions.assertEquals(
                    expected,
                    resultGroup,
                    String.format(
                            "asserted regex group should be equal to string,\ngroup:\t\t%s\nexpected:\t%s\nresult:\t\t%s%n",
                            group, expected, resultGroup));
            return this;
        }
    }

    public MatcherAssertions assertMatchesAny(Pattern pattern, String string) {
        Matcher matcher = pattern.matcher(string);
        boolean matches = matcher.find();
        Assertions.assertTrue(
                matches,
                String.format(
                        "asserted regex matches string, however it did not match string. \nregex:\t%s\nstring:\t%s",
                        pattern, string.replaceAll("\n", "\\\\n")));
        return new MatcherAssertions(matcher);
    }

    public void assertMatchesNone(Pattern pattern, String string) {
        Matcher matcher = pattern.matcher(string);
        boolean matches = matcher.find();
        Assertions.assertFalse(
                matches,
                String.format(
                        "asserted regex doesn't match string, however it did match string. \nregex:\t%s\nstring:\t%s",
                        pattern, string.replaceAll("\n", "\\\\n")));
    }

    public class TestPrivateFieldsUtil {
        public static Field getPrivateField(Class targetClass, String name) {
            try {
                Field declaredField = targetClass.getDeclaredField(name);
                declaredField.setAccessible(true);
                return declaredField;
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        public static Pattern getPrivateStaticRegexPattern(Class targetClass, String name) {
            Field field = getPrivateField(targetClass, name);
            try {
                return (Pattern) field.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
