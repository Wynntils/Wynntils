/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Service;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.elements.type.Skill;
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
import com.wynntils.services.itemfilter.statproviders.HealthStatProvider;
import com.wynntils.services.itemfilter.statproviders.ItemTypeStatProvider;
import com.wynntils.services.itemfilter.statproviders.LevelStatProvider;
import com.wynntils.services.itemfilter.statproviders.MajorIdStatProvider;
import com.wynntils.services.itemfilter.statproviders.OverallStatProvider;
import com.wynntils.services.itemfilter.statproviders.PowderSlotsStatProvider;
import com.wynntils.services.itemfilter.statproviders.PriceStatProvider;
import com.wynntils.services.itemfilter.statproviders.ProfessionStatProvider;
import com.wynntils.services.itemfilter.statproviders.QualityTierStatProvider;
import com.wynntils.services.itemfilter.statproviders.RarityStatProvider;
import com.wynntils.services.itemfilter.statproviders.SkillReqStatProvider;
import com.wynntils.services.itemfilter.statproviders.SkillStatProvider;
import com.wynntils.services.itemfilter.statproviders.TierStatProvider;
import com.wynntils.services.itemfilter.statproviders.UsesStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.SortDirection;
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
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;

public class ItemFilterService extends Service {
    private static final String SORT_KEY = "sort";
    private static final String SORT_REVERSE_KEY = "^";
    private static final String SORT_LIST_SEPARATOR = ",";

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

    public ItemSearchQuery createSearchQuery(String queryString, boolean supportsSorting) {
        List<StatProviderAndFilterPair<?>> filters = new ArrayList<>();
        List<Pair<SortDirection, ItemStatProvider<?>>> sortStatProviders = new ArrayList<>();

        List<Pair<ChatFormatting, Pair<Integer, Integer>>> colorRanges = new ArrayList<>();

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
                    if (!supportsSorting) {
                        colorRanges.add(Pair.of(
                                ChatFormatting.RED, Pair.of(tokenStartIndex, tokenStartIndex + token.length())));
                        errors.add(I18n.get("service.wynntils.itemFilter.sortingNotSupported"));
                        continue;
                    }

                    ErrorOr<List<Pair<SortDirection, ItemStatProvider<?>>>> statSortListOrError =
                            getStatSortOrder(inputString);

                    if (statSortListOrError.hasError()) {
                        colorRanges.add(Pair.of(
                                ChatFormatting.RED, Pair.of(tokenStartIndex, tokenStartIndex + token.length())));
                        errors.add(statSortListOrError.getError());
                        continue;
                    }

                    // Highlight the keyword
                    colorRanges.add(Pair.of(
                            ChatFormatting.LIGHT_PURPLE,
                            Pair.of(tokenStartIndex, tokenStartIndex + keyString.length())));

                    // Highlight the value
                    char[] inputStringCharArray = inputString.toCharArray();

                    // Highlight the reverse key and the list separator
                    for (int i = 0; i < inputStringCharArray.length; i++) {
                        char c = inputStringCharArray[i];
                        String stringValue = String.valueOf(c);
                        if (stringValue.equals(SORT_REVERSE_KEY)) {
                            colorRanges.add(Pair.of(
                                    ChatFormatting.GOLD,
                                    Pair.of(
                                            tokenStartIndex + keyString.length() + i + 1,
                                            tokenStartIndex + keyString.length() + i + 2)));
                        } else if (stringValue.equals(",")) {
                            colorRanges.add(Pair.of(
                                    ChatFormatting.GOLD,
                                    Pair.of(
                                            tokenStartIndex + keyString.length() + i + 1,
                                            tokenStartIndex + keyString.length() + i + 2)));
                        }
                    }

                    // The filtered stats are yellow, unless highlighted before
                    colorRanges.add(Pair.of(
                            ChatFormatting.YELLOW,
                            Pair.of(tokenStartIndex + keyString.length() + 1, tokenStartIndex + token.length())));

                    sortStatProviders.addAll(statSortListOrError.getValue());

                    continue;
                }

                ErrorOr<ItemStatProvider<?>> itemStatProviderOrError = getItemStatProvider(keyString);

                // If the filter does not exist, mark the token as ignored and continue to the next token
                if (itemStatProviderOrError.hasError()) {
                    colorRanges.add(
                            Pair.of(ChatFormatting.RED, Pair.of(tokenStartIndex, tokenStartIndex + token.length())));
                    errors.add(itemStatProviderOrError.getError());
                    continue;
                }

                // The filter exists, highlight the keyword...
                colorRanges.add(
                        Pair.of(ChatFormatting.YELLOW, Pair.of(tokenStartIndex, tokenStartIndex + keyString.length())));

                // Highlight the filter string, even if we don't have an input string yet
                if (inputString.isEmpty()) continue;

                ItemStatProvider<?> itemStatProvider = itemStatProviderOrError.getValue();
                ErrorOr<StatFilter<?>> statFilter = getStatFilter(itemStatProvider.getType(), inputString);

                // If the inputString is invalid, mark the value as ignored and continue to the next token
                if (statFilter.hasError()) {
                    colorRanges.add(Pair.of(
                            ChatFormatting.RED,
                            Pair.of(tokenStartIndex + keyString.length() + 1, tokenStartIndex + token.length())));
                    errors.add(statFilter.getError());
                    continue;
                }

                // Highlight the value
                colorRanges.add(Pair.of(
                        ChatFormatting.GOLD,
                        Pair.of(tokenStartIndex + keyString.length() + 1, tokenStartIndex + token.length())));

                StatProviderAndFilterPair<?> statProviderAndFilterPair =
                        StatProviderAndFilterPair.fromPair(itemStatProvider, statFilter.getValue());

                // The inputString is valid, add the filter to the list
                filters.add(statProviderAndFilterPair);
            } else if (!token.isEmpty()) {
                // The token is not a filter, add it to the list of plain text tokens
                plainTextTokens.add(token);
            }
        }

        return new ItemSearchQuery(queryString, filters, sortStatProviders, colorRanges, errors, plainTextTokens);
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
        if (searchQuery.isEmpty()) return true;
        if (itemStack.isEmpty()) return false;

        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return false;

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

        // Sorted stat providers must be filtered as "any" filters
        filteredList = filteredList.filter(itemStack -> {
            Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);

            WynnItem wynnItem = wynnItemOpt.get();

            for (Pair<SortDirection, ItemStatProvider<?>> pair : searchQuery.sortStatProviders()) {
                ItemStatProvider<?> statProvider = pair.value();
                if (statProvider.getValue(wynnItem).isEmpty()) {
                    return false;
                }
            }

            return true;
        });

        filteredList = filteredList.sorted((itemStack1, itemStack2) -> {
            Optional<WynnItem> wynnItem1Opt = Models.Item.getWynnItem(itemStack1);
            Optional<WynnItem> wynnItem2Opt = Models.Item.getWynnItem(itemStack2);

            if (wynnItem1Opt.isEmpty() || wynnItem2Opt.isEmpty()) {
                return 0;
            }

            WynnItem wynnItem1 = wynnItem1Opt.get();
            WynnItem wynnItem2 = wynnItem2Opt.get();

            for (Pair<SortDirection, ItemStatProvider<?>> providerPair : searchQuery.sortStatProviders()) {
                int compare = providerPair.value().compare(wynnItem1, wynnItem2);

                if (compare != 0) {
                    return switch (providerPair.key()) {
                        case ASCENDING -> -compare;
                        case DESCENDING -> compare;
                    };
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

    private ErrorOr<List<Pair<SortDirection, ItemStatProvider<?>>>> getStatSortOrder(String inputString) {
        List<Pair<SortDirection, String>> providerNamesWithDirection = Arrays.stream(
                        inputString.split(SORT_LIST_SEPARATOR))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> {
                    if (s.startsWith(SORT_REVERSE_KEY)) {
                        return Pair.of(SortDirection.ASCENDING, s.substring(1));
                    }

                    return Pair.of(SortDirection.DESCENDING, s);
                })
                .toList();

        List<Pair<SortDirection, ErrorOr<ItemStatProvider<?>>>> errorsOrProviders = providerNamesWithDirection.stream()
                .map(pair -> Pair.of(pair.key(), getItemStatProvider(pair.value())))
                .toList();

        Optional<Pair<SortDirection, ErrorOr<ItemStatProvider<?>>>> firstError = errorsOrProviders.stream()
                .filter(pair -> pair.value().hasError())
                .findFirst();

        if (firstError.isPresent()) {
            return ErrorOr.error(firstError.get().value().getError());
        }

        List<Pair<SortDirection, ItemStatProvider<?>>> providers = new ArrayList<>();

        for (Pair<SortDirection, ErrorOr<ItemStatProvider<?>>> pair : errorsOrProviders) {
            providers.add(Pair.of(pair.key(), pair.value().getValue()));
        }

        return ErrorOr.of(providers);
    }

    private void registerStatProviders() {
        // Keep some kind of order here, because it is used when displaying the filter helper in the GUI

        // Price Stats
        registerStatProvider(new PriceStatProvider());
        registerStatProvider(new EmeraldValueStatProvider());

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
        registerStatProvider(new HealthStatProvider());

        // Profession Stats
        registerStatProvider(new ProfessionStatProvider());
        registerStatProvider(new QualityTierStatProvider());

        // Dynamic Item Stats
        registerStatProvider(new OverallStatProvider());
        for (Skill skill : Models.Element.getGearSkillOrder()) {
            registerStatProvider(new SkillStatProvider(skill));
            registerStatProvider(new SkillReqStatProvider(skill));
        }
        for (StatType statType : Models.Stat.getAllStatTypes()) {
            registerStatProvider(new ActualStatProvider(statType));
        }
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
