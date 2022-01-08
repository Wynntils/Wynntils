/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class WebReader {

    String url;
    File file;

    private Map<String, String> values;
    private Map<String, List<String>> lists;

    public WebReader(String url) throws Exception {
        this.url = url;

        parseWebsite();
    }

    public WebReader(File file) throws Exception {
        this.file = file;

        parseFile();
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

    private void parseFile() throws Exception {
        try (FileReader fr = new FileReader(file)) {}

        if (!parseData(FileUtils.readFileToString(file, StandardCharsets.UTF_8))) {
            throw new Exception("Invalid WebReader result");
        }
    }

    private void parseWebsite() throws Exception {
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
            if (str.contains("[") && str.contains("]")) {
                String[] split;
                if (str.contains(" = ")) {
                    split = str.split(" = ");
                } else if (str.contains("=")) {
                    split = str.split("=");
                } else {
                    return false;
                }

                values.put(split[0].replace("[", "").replace("]", ""), split[1]);

                if (split[1].contains(",")) {
                    String[] array = split[1].split(",");

                    List<String> values = new ArrayList<>();
                    for (String x : array) {
                        if (x.startsWith(" ")) {
                            x = x.substring(1);
                        }
                        values.add(x);
                    }

                    lists.put(split[0].replace("[", "").replace("]", ""), values);
                } else {
                    List<String> values = new ArrayList<>();
                    values.add(split[1]);
                    lists.put(split[0].replace("[", "").replace("]", ""), values);
                }
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
}
