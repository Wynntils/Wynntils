/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.testutils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;

public abstract class RegexTest {
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

        public MatcherAssertions assertGroupIs(String group, String expected){
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
}
