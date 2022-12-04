/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that parses a String into a specific format of "[Key] = Value"
 *
 * <p>Ex: https://api.wynntils.com/webapi provides such a format
 */
public class WebReader {
    private static final Pattern LINE_MATCHER = Pattern.compile("\\[(?<Key>[^\\[\\]]+)\\]\\s*=\\s*(?<Value>.+)");

    private Map<String, String> values;
    private Map<String, List<String>> lists;

    private WebReader() {}

    public static WebReader fromString(String data) {
        WebReader result = new WebReader();
        if (!result.parseData(data)) {
            return null;
        }

        return result;
    }

    public Map<String, String> getValues() {
        return values;
    }

    private boolean parseData(String data) {
        values = new HashMap<>();
        lists = new HashMap<>();

        for (String str : data.split("\\r?\\n")) {
            Matcher result = LINE_MATCHER.matcher(str);
            if (!result.find()) {
                WynntilsMod.error("Malformed data line: " + str);
                continue;
            }

            String key = result.group("Key");
            String value = result.group("Value");

            if (value.contains(",")) {
                List<String> values = StringUtils.parseStringToList(value);

                lists.put(key, values);
            } else {
                values.put(key, value);
            }
        }

        return true;
    }

    public String get(String key) {
        return values.getOrDefault(key, null);
    }

    public boolean hasKey(String key) {
        return values.containsKey(key);
    }

    public List<String> getList(String key) {
        return lists.getOrDefault(key, new ArrayList<>());
    }

    @Override
    public String toString() {
        return "WebReader{" + "values=" + values + ", lists=" + lists + '}';
    }
}
