/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.wiki;

import com.google.gson.JsonObject;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WikiService extends Service {
    private static final Pattern WIKI_REDIRECT_PATTERN = Pattern.compile("#REDIRECT \\[\\[(?<redirectname>.+)\\]\\]");

    public WikiService() {
        super(List.of());
    }

    public void openPage(String pageName) {
        Managers.Net.openLink(UrlId.LINK_WIKI_LOOKUP, Map.of("title", pageName));
    }

    public void openPageResolvingRedirects(String pageName) {
        resolvePage(pageName, wikiPage -> openPage(wikiPage.pageName()), () -> openPage(pageName));
    }

    public void resolvePage(String pageName, Consumer<WikiPage> onSuccess, Runnable onError) {
        ApiResponse apiResponse = Managers.Net.callApi(UrlId.API_WIKI_DISCOVERY_QUERY, Map.of("name", pageName));

        apiResponse.handleJsonObject(
                json -> handleWikiJsonResponse(json, pageName, onSuccess, onError), throwable -> onError.run());
    }

    private void handleWikiJsonResponse(
            JsonObject json, String pageName, Consumer<WikiPage> onSuccess, Runnable onError) {
        if (json.has("error")) {
            onError.run();
            return;
        }

        String wikiText = json.get("parse")
                .getAsJsonObject()
                .get("wikitext")
                .getAsJsonObject()
                .get("*")
                .getAsString();

        Matcher redirectMatcher = WIKI_REDIRECT_PATTERN.matcher(wikiText);
        if (redirectMatcher.matches()) {
            resolvePage(redirectMatcher.group("redirectname"), onSuccess, onError);
            return;
        }

        onSuccess.accept(new WikiPage(pageName, wikiText));
    }

    public record WikiPage(String pageName, String wikiText) {}
}
