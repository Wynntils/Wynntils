/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.discoveries;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText2;
import com.wynntils.handlers.container.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DiscoveryContainerQueries {
    private static final int NEXT_PAGE_SLOT = 8;
    private static final int DISCOVERIES_SLOT = 35;
    private static final int SECRET_DISCOVERIES_SLOT = 44;

    private static final int DISCOVERIES_PER_PAGE =
            41; // 6 * 7 items, but - 1 because last item is missing because of Wynn bug

    private static final Pattern DISCOVERY_COUNT_PATTERN =
            Pattern.compile("§6Total Discoveries: §r§e\\[(\\d+)/\\d+\\]");
    private static final Pattern SECRET_DISCOVERY_COUNT_PATTERN =
            Pattern.compile("§bTotal Secret Discoveries: §r§3\\[(\\d+)/\\d+\\]");
    public static final StyledText2 DISCOVERIES_STRING = StyledText2.fromString("§6§lDiscoveries");
    public static final StyledText2 SECRET_DISCOVERIES_STRING = StyledText2.fromString("§b§lSecret Discoveries");

    private List<DiscoveryInfo> newDiscoveries;

    public void queryDiscoveries() {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Discovery Count Query")
                .onError(msg -> WynntilsMod.warn("Problem getting discovery count in Quest Book: " + msg))
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(Models.Quest.getQuestBookTitleRegex(1))
                .processContainer((c) -> {
                    ItemStack discoveriesItem = c.items().get(DISCOVERIES_SLOT);
                    ItemStack secretDiscoveriesItem = c.items().get(SECRET_DISCOVERIES_SLOT);

                    if (!ComponentUtils.getCoded(discoveriesItem.getHoverName()).equals(DISCOVERIES_STRING)
                            || !ComponentUtils.getCoded(secretDiscoveriesItem.getHoverName())
                                    .equals(SECRET_DISCOVERIES_STRING)) {
                        WynntilsMod.error("Returned early because discovery items were not found.");

                        return;
                    }

                    int discoveryCount = -1;
                    for (StyledText2 line : LoreUtils.getLore(discoveriesItem)) {
                        Matcher matcher = line.getMatcher(DISCOVERY_COUNT_PATTERN);

                        if (matcher.matches()) {
                            discoveryCount = Integer.parseInt(matcher.group(1));
                            break;
                        }
                    }

                    if (discoveryCount == -1) {
                        WynntilsMod.error("Could not find discovery count in discovery item.");

                        return;
                    }

                    for (StyledText2 line : LoreUtils.getLore(secretDiscoveriesItem)) {
                        Matcher matcher = line.getMatcher(SECRET_DISCOVERY_COUNT_PATTERN);

                        if (matcher.matches()) {
                            int secretDiscoveryCount = Integer.parseInt(matcher.group(1));

                            if (secretDiscoveryCount == -1) {
                                WynntilsMod.error("Could not find secret discovery count in secret discovery item.");

                                return;
                            }

                            Models.Discovery.setDiscoveriesTooltip(LoreUtils.getTooltipLines(discoveriesItem));
                            Models.Discovery.setSecretDiscoveriesTooltip(
                                    LoreUtils.getTooltipLines(secretDiscoveriesItem));

                            int discoveryPages = discoveryCount / DISCOVERIES_PER_PAGE
                                    + (discoveryCount % DISCOVERIES_PER_PAGE == 0 ? 0 : 1);
                            int secretDiscoveryPages = secretDiscoveryCount / DISCOVERIES_PER_PAGE
                                    + (secretDiscoveryCount % DISCOVERIES_PER_PAGE == 0 ? 0 : 1);
                            buildDiscoveryQuery(discoveryPages, secretDiscoveryPages);
                            break;
                        }
                    }
                });

        queryBuilder.build().executeQuery();
    }

    private void buildDiscoveryQuery(int discoveryPages, int secretDiscoveryPages) {
        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Discovery Query")
                .onError(msg -> {
                    WynntilsMod.warn("Problem querying discoveries: " + msg);
                    McUtils.sendMessageToClient(
                            Component.literal("Error updating discoveries.").withStyle(ChatFormatting.RED));
                })
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .matchTitle(Models.Quest.getQuestBookTitleRegex(1))
                .processContainer(c -> {})
                .clickOnSlot(DISCOVERIES_SLOT)
                .matchTitle(getDiscoveryPageTitleRegex(1))
                .processContainer(c -> processDiscoveryPage(c, 1, discoveryPages, false));

        for (int i = 2; i <= discoveryPages; i++) {
            final int page = i; // Lambdas need final variables
            queryBuilder
                    .clickOnSlotWithName(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(page))
                    .matchTitle(getDiscoveryPageTitleRegex(page))
                    .processContainer(c -> processDiscoveryPage(c, page, discoveryPages, false));
        }

        queryBuilder
                .clickOnSlot(SECRET_DISCOVERIES_SLOT)
                .matchTitle(getDiscoveryPageTitleRegex(1))
                .processContainer(c -> processDiscoveryPage(c, 1, secretDiscoveryPages, true));

        for (int i = 2; i <= secretDiscoveryPages; i++) {
            final int page = i; // Lambdas need final variables
            queryBuilder
                    .clickOnSlotWithName(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(page))
                    .matchTitle(getDiscoveryPageTitleRegex(page))
                    .processContainer(c -> processDiscoveryPage(c, page, secretDiscoveryPages, true));
        }

        queryBuilder.build().executeQuery();
    }

    private void processDiscoveryPage(ContainerContent container, int page, int lastPage, boolean secretDiscovery) {
        if (page == 1) {
            newDiscoveries = new ArrayList<>();
        }

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int slot = row * 9 + col;

                ItemStack itemStack = container.items().get(slot);
                DiscoveryInfo discoveryInfo = DiscoveryInfo.parseFromItemStack(itemStack);
                if (discoveryInfo == null) continue;

                newDiscoveries.add(discoveryInfo);
            }
        }

        if (page == lastPage) {
            // Last page finished
            if (secretDiscovery) {
                // Secret discoveries finished
                Models.Discovery.setSecretDiscoveries(newDiscoveries);
            } else {
                // Normal discoveries finished
                Models.Discovery.setDiscoveries(newDiscoveries);
            }
        }
    }

    private static String getDiscoveryPageTitleRegex(int pageNum) {
        // FIXME: We ignore pageNum, as we do not have a valid way of only querying dynamic amounts of pages
        return "^§0\\[Pg. \\d+\\] §8.*§0 Discoveries$";
    }

    private StyledText2 getNextPageButtonName(int nextPageNum) {
        return StyledText2.fromString("[§f§lPage " + nextPageNum + "§a >§2>§a>§2>§a>]");
    }
}
