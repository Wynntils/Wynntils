/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.wynntils.utils.StringUtils;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Class that parses a String into a specific format of "[Key] = Value"
 *
 * <p>Ex: https://api.wynntils.com/webapi provides such a format
 */
public class WebReader {
    private static final Pattern LINE_MATCHER =
            Pattern.compile("\\[(?<Key>[^\\[\\]]+)\\]\\s*=\\s*(?<Value>.+)");

    private Map<String, String> values;
    private Map<String, List<String>> lists;

    public WebReader(String url) throws Exception {
        parseWebsite(url);
    }

    public WebReader(File file) throws Exception {
        parseFile(file);
    }

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

    private void parseFile(File file) throws Exception {
        if (!parseData(FileUtils.readFileToString(file, StandardCharsets.UTF_8))) {
            throw new Exception("Invalid WebReader result");
        }
    }

    private void parseWebsite(String url) throws Exception {
        URLConnection st = new URL(url).openConnection();
        st.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316"
                        + " Firefox/3.6.2");

        if (!parseData(IOUtils.toString(st.getInputStream(), StandardCharsets.UTF_8))) {
            throw new Exception("Invalid WebReader result");
        }
    }

    private boolean parseData(String data) {
        values = new HashMap<>();
        lists = new HashMap<>();

        for (String str : data.split("\\r?\\n")) {
            Matcher result = LINE_MATCHER.matcher(str);
            result.find();

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

    public List<String> getList(String key) {
        return lists.getOrDefault(key, new ArrayList<>());
    }

    @Override
    public String toString() {
        return "WebReader{" + "values=" + values + ", lists=" + lists + '}';
    }
}
