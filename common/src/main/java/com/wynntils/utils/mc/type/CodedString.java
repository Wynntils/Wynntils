package com.wynntils.utils.mc.type;

import com.wynntils.utils.wynn.WynnUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodedString {
    public static final CodedString EMPTY = new CodedString("");

    private final String str;

    public CodedString(String str) {
        this.str = str;
    }

    public String str() {
        return str;
    }

    public String getNormalized() {
        return WynnUtils.normalizeBadString(str);
    }

    public Matcher match(Pattern pattern) {
        return pattern.matcher(str);
    }
}
