/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.WynnItem;
import com.wynntils.services.itemfilter.filters.LevelItemFilterFactory;
import com.wynntils.services.itemfilter.filters.ProfessionItemFilterFactory;
import com.wynntils.services.itemfilter.type.ItemFilter;
import com.wynntils.services.itemfilter.type.ItemFilterFactory;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;

public class ItemFilterService extends Service {
    private final Map<String, ItemFilterFactory> filterFactories = new HashMap<>();

    public ItemFilterService() {
        super(List.of());
        registerAllFilters();
    }

    /**
     * Returns a filter factory for the given keyword, or an error string if the keyword does not match any filter.
     * @param keyword the keyword to get the filter factory for
     * @return the filter factory, or an error string if the keyword does not match any filter
     */
    public ErrorOr<? extends ItemFilterFactory> getFilterFactory(String keyword) {
        if (!filterFactories.containsKey(keyword)) {
            return ErrorOr.error(I18n.get("feature.wynntils.itemFilter.unknownFilter", keyword));
        }

        return ErrorOr.of(filterFactories.get(keyword));
    }

    /**
     * @return an unmodifiable collection of all registered filter factories
     */
    public Collection<ItemFilterFactory> getFilterFactories() {
        return Collections.unmodifiableCollection(filterFactories.values());
    }

    public ItemSearchQuery createSearchQuery(String queryString) {
        List<ItemFilter> itemFilters = new ArrayList<>();
        List<Integer> ignoredCharIndices = new ArrayList<>();
        List<Integer> validFilterCharIndices = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        List<String> plainTextTokens = new ArrayList<>();

        String[] tokens = queryString.split(" ");

        // Keeps track of the index of the first char of the current token in the query string.
        // Because we always add +1 to account for the space char, we need to start at -1
        int tokenStartIndex = -1;
        String lastToken = "";
        for (String token : tokens) {
            // For clarity, because we use some "continue" statements, we need to update the tokenStartIndex here.
            // We keep the current token to add its length on the next iteration.
            tokenStartIndex += lastToken.length() + 1;
            lastToken = token;

            if (token.contains(":")) {
                String filterString = token.split(":")[0];
                String valueString = token.substring(token.indexOf(':') + 1);
                ErrorOr<? extends ItemFilterFactory> factoryOrError =
                        Services.ItemFilter.getFilterFactory(filterString);

                // If the filter does not exist, mark the token as ignored and continue to the next token
                if (factoryOrError.hasError()) {
                    ignoredCharIndices.addAll(IntStream.rangeClosed(tokenStartIndex, tokenStartIndex + token.length())
                            .boxed()
                            .toList());
                    errors.add(factoryOrError.getError());
                    continue;
                }

                // The filter exists, highlight the keyword...
                validFilterCharIndices.addAll(
                        IntStream.rangeClosed(tokenStartIndex, tokenStartIndex + filterString.length())
                                .boxed()
                                .toList());

                // ...and try to create the filter only if the filter value is not empty, because a filter without a
                // value is pointless and we don't want to show the error the factory might return because of that.
                // We still want to highlight the filter keyword though, that's why we handle the empty value here.
                if (valueString.isEmpty()) continue;

                ErrorOr<? extends ItemFilter> filterOrError =
                        factoryOrError.getValue().create(valueString);

                // If the filter value is invalid, mark the value as ignored and continue to the next token
                if (filterOrError.hasError()) {
                    ignoredCharIndices.addAll(IntStream.rangeClosed(
                                    tokenStartIndex + filterString.length() + 1, tokenStartIndex + token.length())
                            .boxed()
                            .toList());
                    errors.add(filterOrError.getError());
                    continue;
                }

                // The filter value is valid, add the filter to the list
                itemFilters.add(filterOrError.getValue());
            } else if (!token.isEmpty()) {
                // The token is not a filter, add it to the list of plain text tokens
                plainTextTokens.add(token);
            }
        }

        return new ItemSearchQuery(
                queryString, itemFilters, ignoredCharIndices, validFilterCharIndices, errors, plainTextTokens);
    }

    /**
     * Checks if the given item matches the search query. The item must match all filters and contain all plain text
     * tokens. Therefore, if there are no plain text tokens, and no filters, this would be considered a match.
     * <br>
     * If the item is not a WynnItem, this method always returns false.
     *
     * @param searchQuery the search query
     * @param itemStack the item to check
     * @return true if the item matches the search query, false otherwise
     */
    public boolean matches(ItemSearchQuery searchQuery, ItemStack itemStack) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) {
            return false;
        }

        return filterMatches(searchQuery, wynnItemOpt.get())
                && itemNameMatches(
                        searchQuery,
                        StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting());
    }

    /**
     * Checks if the given item matches all filters. Tokens that are not filters in the search query are ignored. If no
     * filters are present, this method always returns true.
     *
     * @param wynnItem the item to check
     * @return true if the item matches all filters, false otherwise
     */
    private boolean filterMatches(ItemSearchQuery searchQuery, WynnItem wynnItem) {
        return searchQuery.itemFilters().stream().allMatch(o -> o.matches(wynnItem));
    }

    /**
     * Checks if the given item name contains the concatenated plain text tokens of the search query. The filter tokens
     * in the search query are ignored. If there are no plain text tokens, this method always returns true.
     *
     * @param itemName the name to check
     * @return true if the name contains the concatenated plain text tokens, false otherwise
     */
    private boolean itemNameMatches(ItemSearchQuery searchQuery, String itemName) {
        return searchQuery.plainTextTokens().isEmpty()
                || itemName.toLowerCase(Locale.ROOT)
                        .contains(
                                String.join(" ", searchQuery.plainTextTokens()).toLowerCase(Locale.ROOT));
    }

    private void registerAllFilters() {
        registerFilter(new LevelItemFilterFactory());
        registerFilter(new ProfessionItemFilterFactory());
    }

    private void registerFilter(ItemFilterFactory factory) {
        filterFactories.put(factory.getKeyword(), factory);
    }
}
