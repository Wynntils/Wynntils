package com.wynntils.testutils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RegexTest {
    public Matcher assertMatchesAny(Pattern pattern, String string){
        Matcher matcher = pattern.matcher(string);
        boolean matches = matcher.find();
        if(!matches){
            throw new AssertionError(String.format("asserted regex matches string, however it did not match string. \nregex:\t%s\nstring:\t%s",pattern,string.replaceAll("\n","\\\\n")));
        }
        return matcher;
    }
}
