/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Service;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.services.itemfilter.filters.AnyStatFilters;
import com.wynntils.services.itemfilter.filters.PercentageStatFilter;
import com.wynntils.services.itemfilter.filters.RangedStatFilters;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.statproviders.ActualStatProvider;
import com.wynntils.services.itemfilter.statproviders.CountedItemStatProvider;
import com.wynntils.services.itemfilter.statproviders.DurabilityStatProvider;
import com.wynntils.services.itemfilter.statproviders.EmeraldValueStatProvider;
import com.wynntils.services.itemfilter.statproviders.GearRestrictionStatProvider;
import com.wynntils.services.itemfilter.statproviders.GearTypeStatProvider;
import com.wynntils.services.itemfilter.statproviders.ItemTypeStatProvider;
import com.wynntils.services.itemfilter.statproviders.LevelStatProvider;
import com.wynntils.services.itemfilter.statproviders.MajorIdStatProvider;
import com.wynntils.services.itemfilter.statproviders.PowderSlotsStatProvider;
import com.wynntils.services.itemfilter.statproviders.ProfessionStatProvider;
import com.wynntils.services.itemfilter.statproviders.QualityTierStatProvider;
import com.wynntils.services.itemfilter.statproviders.RarityStatProvider;
import com.wynntils.services.itemfilter.statproviders.TierStatProvider;
import com.wynntils.services.itemfilter.statproviders.UsesStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatFilterFactory;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.services.itemfilter.type.StatValue;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;

public class ItemFilterService extends Service {
    private static final String SORT_KEY = "sort";

    private final List<ItemStatProvider<?>> itemStatProviders = new ArrayList<>();
    private final List<Pair<Class<?>, StatFilterFactory<? extends StatFilter<?>>>> statFilters = new ArrayList<>();

    public ItemFilterService() {
        super(List.of());

        registerStatProviders();
        registerStatFilters();
    }

    public List<ItemStatProvider<?>> getItemStatProviders() {
        return itemStatProviders;
    }

    public List<? extends StatFilterFactory<? extends StatFilter<?>>> getStatFilters() {
        return statFilters.stream().map(Pair::value).toList();
    }

    public ItemSearchQuery createSearchQuery(String queryString) {
        List<StatProviderAndFilterPair<?>> filters = new ArrayList<>();
        List<ItemStatProvider<?>> sortStatProviders = new ArrayList<>();
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
                String keyString = token.substring(0, token.indexOf(':'));
                String inputString = token.substring(token.indexOf(':') + 1);

                // Handle the special case of the sort key
                if (keyString.equalsIgnoreCase(SORT_KEY)) {
                    ErrorOr<List<ItemStatProvider<?>>> statSortListOrError = getStatSortOrder(inputString);

                    if (statSortListOrError.hasError()) {
                        ignoredCharIndices.addAll(
                                IntStream.rangeClosed(tokenStartIndex, tokenStartIndex + token.length())
                                        .boxed()
                                        .toList());
                        errors.add(statSortListOrError.getError());
                        continue;
                    }

                    validFilterCharIndices.addAll(
                            IntStream.rangeClosed(tokenStartIndex, tokenStartIndex + keyString.length())
                                    .boxed()
                                    .toList());
                    sortStatProviders.addAll(statSortListOrError.getValue());
                    continue;
                }

                ErrorOr<ItemStatProvider<?>> itemStatProviderOrError = getItemStatProvider(keyString);

                // If the filter does not exist, mark the token as ignored and continue to the next token
                if (itemStatProviderOrError.hasError()) {
                    ignoredCharIndices.addAll(IntStream.rangeClosed(tokenStartIndex, tokenStartIndex + token.length())
                            .boxed()
                            .toList());
                    errors.add(itemStatProviderOrError.getError());
                    continue;
                }

                // The filter exists, highlight the keyword...
                validFilterCharIndices.addAll(
                        IntStream.rangeClosed(tokenStartIndex, tokenStartIndex + keyString.length())
                                .boxed()
                                .toList());

                // Highlight the filter string, even if we don't have an input string yet
                if (inputString.isEmpty()) continue;

                ItemStatProvider<?> itemStatProvider = itemStatProviderOrError.getValue();
                ErrorOr<StatFilter<?>> statFilter = getStatFilter(itemStatProvider.getType(), inputString);

                // If the inputString is invalid, mark the value as ignored and continue to the next token
                if (statFilter.hasError()) {
                    ignoredCharIndices.addAll(IntStream.rangeClosed(
                                    tokenStartIndex + keyString.length() + 1, tokenStartIndex + token.length())
                            .boxed()
                            .toList());
                    errors.add(statFilter.getError());
                    continue;
                }

                StatProviderAndFilterPair<?> statProviderAndFilterPair =
                        StatProviderAndFilterPair.fromPair(itemStatProvider, statFilter.getValue());

                // The inputString is valid, add the filter to the list
                filters.add(statProviderAndFilterPair);
            } else if (!token.isEmpty()) {
                // The token is not a filter, add it to the list of plain text tokens
                plainTextTokens.add(token);
            }
        }

        return new ItemSearchQuery(
                queryString,
                filters,
                sortStatProviders,
                ignoredCharIndices,
                validFilterCharIndices,
                errors,
                plainTextTokens);
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
        if (searchQuery.isEmpty()) {
            return true;
        }

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
     * Filters and sorts the given list of items according to the given search query.
     * @param searchQuery the search query
     * @param originalList the list of items to filter and sort
     * @return the filtered and sorted list of items
     */
    public <T extends ItemStack> List<T> filterAndSort(ItemSearchQuery searchQuery, List<T> originalList) {
        Stream<T> filteredList = originalList.stream().filter(itemStack -> matches(searchQuery, itemStack));

        filteredList = filteredList.sorted((itemStack1, itemStack2) -> {
            Optional<WynnItem> wynnItem1Opt = Models.Item.getWynnItem(itemStack1);
            Optional<WynnItem> wynnItem2Opt = Models.Item.getWynnItem(itemStack2);

            if (wynnItem1Opt.isEmpty() || wynnItem2Opt.isEmpty()) {
                return 0;
            }

            WynnItem wynnItem1 = wynnItem1Opt.get();
            WynnItem wynnItem2 = wynnItem2Opt.get();

            for (ItemStatProvider<?> itemStatProvider : searchQuery.sortStatProviders()) {
                int compare = itemStatProvider.compare(wynnItem1, wynnItem2);

                if (compare != 0) {
                    return compare;
                }
            }

            return 0;
        });

        return filteredList.toList();
    }

    /**
     * Returns an item stat provider for the given alias, or an error string if the alias does not match any stat providers.
     * @param name an alias of the stat provider
     * @return the item stat provider, or an error string if the alias does not match any stat providers.
     */
    private ErrorOr<ItemStatProvider<?>> getItemStatProvider(String name) {
        Optional<ItemStatProvider<?>> itemStatProviderOpt = itemStatProviders.stream()
                .filter(filter ->
                        filter.getName().equals(name) || filter.getAliases().contains(name))
                .findFirst();

        if (itemStatProviderOpt.isPresent()) {
            return ErrorOr.of(itemStatProviderOpt.get());
        } else {
            return ErrorOr.error(I18n.get("service.wynntils.itemFilter.unknownStat", name));
        }
    }

    /**
     * Returns a stat filter for the given value, or an error string if the value does not match any stat filters.
     *
     * @param type
     * @param value the value to parse
     * @return the stat filter, or an error string if the value does not match any stat filters.
     */
    private <T> ErrorOr<StatFilter<?>> getStatFilter(Class<T> type, String value) {
        Optional<? extends StatFilter<?>> statFilterFactoryOpt = statFilters.stream()
                .filter(filter -> filter.key().equals(type))
                .map(filter -> filter.value().create(value))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        if (statFilterFactoryOpt.isPresent()) {
            return ErrorOr.of(statFilterFactoryOpt.get());
        } else {
            return ErrorOr.error(I18n.get("service.wynntils.itemFilter.invalidFilter", value, type.getSimpleName()));
        }
    }

    /**
     * Checks if the given item matches all filters. Tokens that are not filters in the search query are ignored. If no
     * filters are present, this method always returns true.
     *
     * @param wynnItem the item to check
     * @return true if the item matches all filters, false otherwise
     */
    private boolean filterMatches(ItemSearchQuery searchQuery, WynnItem wynnItem) {
        return searchQuery.filters().stream().allMatch(o -> o.matches(wynnItem));
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

    private ErrorOr<List<ItemStatProvider<?>>> getStatSortOrder(String inputString) {
        List<ErrorOr<ItemStatProvider<?>>> errorsOrProviders = Arrays.stream(inputString.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(this::getItemStatProvider)
                .toList();

        Optional<ErrorOr<ItemStatProvider<?>>> firstError =
                errorsOrProviders.stream().filter(ErrorOr::hasError).findFirst();

        if (firstError.isPresent()) {
            return ErrorOr.error(firstError.get().getError());
        }

        List<? extends ItemStatProvider<?>> providers =
                errorsOrProviders.stream().map(ErrorOr::getValue).toList();

        return ErrorOr.of((List<ItemStatProvider<?>>) providers);
    }

    private void registerStatProviders() {
        // Keep some kind of order here, because it is used when displaying the filter helper in the GUI

        // Constant Item Stats
        registerStatProvider(new LevelStatProvider());
        registerStatProvider(new RarityStatProvider());
        registerStatProvider(new ItemTypeStatProvider());
        registerStatProvider(new GearTypeStatProvider());
        registerStatProvider(new CountedItemStatProvider());
        registerStatProvider(new DurabilityStatProvider());
        registerStatProvider(new TierStatProvider());
        registerStatProvider(new UsesStatProvider());
        registerStatProvider(new GearRestrictionStatProvider());
        registerStatProvider(new MajorIdStatProvider());
        registerStatProvider(new PowderSlotsStatProvider());

        // Profession Stats
        registerStatProvider(new ProfessionStatProvider());
        registerStatProvider(new QualityTierStatProvider());

        // Dynamic Item Stats
        for (StatType statType : Models.Stat.getAllStatTypes()) {
            registerStatProvider(new ActualStatProvider(statType));
        }

        registerStatProvider(new EmeraldValueStatProvider());
    }

    private void registerStatProvider(ItemStatProvider<?> statProvider) {
        itemStatProviders.add(statProvider);
    }

    private void registerStatFilters() {
        // The order is not strictly relevant,
        // but it is used when displaying the filter helper in the GUI

        registerStatFilter(Integer.class, new AnyStatFilters.AnyIntegerStatFilter.AnyIntegerStatFilterFactory());
        registerStatFilter(String.class, new AnyStatFilters.AnyStringStatFilter.AnyStringStatFilterFactory());
        registerStatFilter(
                CappedValue.class, new AnyStatFilters.AnyCappedValueStatFilter.AnyCappedValueStatFilterFactory());
        registerStatFilter(StatValue.class, new AnyStatFilters.AnyStatValueStatFilter.AnyStatValueStatFilterFactory());
        registerStatFilter(
                Integer.class, new RangedStatFilters.RangedIntegerStatFilter.RangedIntegerStatFilterFactory());
        registerStatFilter(
                CappedValue.class,
                new RangedStatFilters.RangedCappedValueStatFilter.RangedCappedValueStatFilterFactory());
        registerStatFilter(
                StatValue.class, new RangedStatFilters.RangedStatValueStatFilter.RangedStatValueStatFilterFactory());

        registerStatFilter(StatValue.class, new PercentageStatFilter.PercentageStatFilterFactory());

        // String is the fallback type, so it should be registered last
        registerStatFilter(String.class, new StringStatFilter.StringStatFilterFactory());
    }

    private <T> void registerStatFilter(Class<T> clazz, StatFilterFactory<? extends StatFilter<T>> statFilterFactory) {
        statFilters.add(Pair.of(clazz, statFilterFactory));
    }
}
