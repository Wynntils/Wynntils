/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter;

import com.wynntils.core.components.Services;
import com.wynntils.models.items.WynnItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public class SearchQuery {
    private final String queryString;

    private final List<ItemFilter> itemFilters;

    private final List<String> plainTextTokens;

    private final List<Integer> ignoredCharIndices;

    private final List<Integer> validFilterCharIndices;

    private final List<String> errors;

    protected SearchQuery(
            String queryString,
            List<ItemFilter> itemFilters,
            List<Integer> ignoredCharIndices,
            List<Integer> validFilterCharIndices,
            List<String> errors,
            List<String> plainTextTokens) {
        this.queryString = queryString;
        this.itemFilters = itemFilters;
        this.ignoredCharIndices = ignoredCharIndices;
        this.validFilterCharIndices = validFilterCharIndices;
        this.errors = errors;
        this.plainTextTokens = plainTextTokens;
    }

    public static SearchQuery fromQueryString(String queryString) {
        List<ItemFilter> itemFilters = new ArrayList<>();
        List<Integer> ignoredCharIndices = new ArrayList<>();
        List<Integer> validFilterCharIndices = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        List<String> plainTextTokens = new ArrayList<>();

        String[] tokens = queryString.split(" ");
        int currentCharIndex = 0;
        for (String token : tokens) {
            if (token.contains(":")) {
                String filterString = token.split(":")[0];
                String valueString = token.substring(token.indexOf(':') + 1);
                try {
                    ItemFilter itemFilter = Services.ItemFilter.createFilter(filterString, valueString);
                    // We want to throw UnknownFilterException if the filter is invalid, but we dont want to thow
                    // InvalidSyntaxException just because the value is empty
                    if (valueString.isEmpty()) continue;

                    validFilterCharIndices.addAll(
                            IntStream.rangeClosed(currentCharIndex, currentCharIndex + filterString.length())
                                    .boxed()
                                    .toList());

                    if (itemFilter.prepare()) {
                        itemFilters.add(itemFilter);
                    }
                } catch (UnknownFilterException e) {
                    ignoredCharIndices.addAll(IntStream.rangeClosed(currentCharIndex, currentCharIndex + token.length())
                            .boxed()
                            .toList());
                    errors.add(e.getMessage());
                } catch (InvalidSyntaxException e) {
                    ignoredCharIndices.addAll(IntStream.rangeClosed(
                                    currentCharIndex + filterString.length() + 1, currentCharIndex + token.length())
                            .boxed()
                            .toList());
                    errors.add(e.getMessage());
                }
            } else if (!token.isEmpty()) {
                plainTextTokens.add(token);
            }
            currentCharIndex += token.length() + 1;
        }

        return new SearchQuery(
                queryString, itemFilters, ignoredCharIndices, validFilterCharIndices, errors, plainTextTokens);
    }

    /**
     * Checks if the given item matches all filters. Tokens that are not filters in the search query are ignored.
     * @param wynnItem the item to check
     * @return true if the item matches all filters, false otherwise
     */
    public boolean filterMatches(WynnItem wynnItem) {
        return !itemFilters.isEmpty() && itemFilters.stream().allMatch(o -> o.matches(wynnItem));
    }

    /**
     * Checks if the given item name contains the concatenated plain text tokens of the search query. The filter tokens
     * in the search query are ignored.
     * @param itemName the name to check
     * @return true if the name contains the concatenated plain text tokens, false otherwise
     */
    public boolean itemNameMatches(String itemName) {
        return plainTextTokens.isEmpty()
                || itemName.toLowerCase(Locale.ROOT)
                        .contains(String.join(" ", plainTextTokens).toLowerCase(Locale.ROOT));
    }

    public String getQueryString() {
        return queryString;
    }

    public List<Integer> getIgnoredCharIndices() {
        return Collections.unmodifiableList(ignoredCharIndices);
    }

    public List<Integer> getValidFilterCharIndices() {
        return Collections.unmodifiableList(validFilterCharIndices);
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
