/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.account;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.container.scriptedquery.QueryBuilder;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.ConfirmedBoolean;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class AccountModel extends Model {
    // Test in AccountModel_VETERAN_PATTERN
    private static final Pattern VETERAN_PATTERN = Pattern.compile("§7Rank: §[6dba]Vet");
    private static final Pattern SILVERBULL_JOIN_PATTERN =
            Pattern.compile("§3Welcome to the §b✮ Silverbull Trading Company§3!");
    private static final Pattern SILVERBULL_UPDATE_PATTERN = Pattern.compile("§7Your subscription has been extended.");
    // Test in AccountModel_SILVERBULL_PATTERN
    private static final Pattern SILVERBULL_PATTERN = Pattern.compile("§7Subscription: §[ac][✖✔] ((?:Ina|A)ctive)");
    // Test in AccountModel_SILVERBULL_DURATION_PATTERN
    private static final Pattern SILVERBULL_DURATION_PATTERN = Pattern.compile(
            "§7Expiration: §f(?:(?<weeks>\\d+) weeks?)? ?(?:(?<days>\\d+) days?)? ?(?:(?<hours>\\d+) hours?)? ?(?:(?<minutes>\\d+) minutes?)? ?(?:(?<seconds>\\d+) seconds?)?");
    public static final Component SILVERBULL_STAR = Component.literal(" ✮").withStyle(ChatFormatting.AQUA);
    private static final int COSMETICS_SLOT = 25;

    @Persisted
    private final Storage<ConfirmedBoolean> isVeteran = new Storage<>(ConfirmedBoolean.UNCONFIRMED);

    @Persisted
    private final Storage<Long> silverbullExpiresAt = new Storage<>(0L);

    @Persisted
    private final Storage<ConfirmedBoolean> silverbullSubscriber = new Storage<>(ConfirmedBoolean.UNCONFIRMED);

    public AccountModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onChatReceived(ChatMessageReceivedEvent e) {
        StyledText message = e.getOriginalStyledText().trim();

        if (message.matches(SILVERBULL_JOIN_PATTERN) || message.matches(SILVERBULL_UPDATE_PATTERN)) {
            silverbullSubscriber.store(ConfirmedBoolean.TRUE);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldStateChanged(WorldStateEvent e) {
        if (e.getNewState() != WorldState.WORLD) return;
        scanRankInfo(e.isFirstJoinWorld());
    }

    public ConfirmedBoolean isVeteran() {
        return isVeteran.get();
    }

    public boolean isSilverbullSubscriber() {
        return silverbullSubscriber.get() == ConfirmedBoolean.TRUE;
    }

    public void scanRankInfo(boolean forceParseUnexpired) {
        WynntilsMod.info("Scheduling rank info query");
        QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Rank Info Query");
        queryBuilder.onError(msg -> WynntilsMod.warn("Error querying Rank Info: " + msg));

        // Open compass/character menu
        queryBuilder.then(QueryStep.useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                .expectContainerTitle(ContainerModel.CHARACTER_INFO_NAME));

        if (forceParseUnexpired
                || silverbullSubscriber.get() == ConfirmedBoolean.UNCONFIRMED
                || (silverbullSubscriber.get() != ConfirmedBoolean.FALSE
                        && System.currentTimeMillis() > silverbullExpiresAt.get())) {
            // Open Cosmetics Menu
            queryBuilder.then(QueryStep.clickOnSlot(COSMETICS_SLOT)
                    .expectContainerTitle(ContainerModel.COSMETICS_MENU_NAME)
                    .processIncomingContainer(this::parseCratesBombsCosmeticsContainer));
        } else {
            WynntilsMod.info("Skipping silverbull subscription query ("
                    + (silverbullExpiresAt.get() - System.currentTimeMillis()) + " ms left)");
        }

        queryBuilder.build().executeQuery();
    }

    private void parseCratesBombsCosmeticsContainer(ContainerContent container) {
        ItemStack rankSubscriptionItem = container.items().getFirst();

        Matcher veteran = LoreUtils.matchLoreLine(rankSubscriptionItem, 0, VETERAN_PATTERN);
        isVeteran.store(veteran.matches() ? ConfirmedBoolean.TRUE : ConfirmedBoolean.FALSE);

        Matcher status = LoreUtils.matchLoreLine(rankSubscriptionItem, 0, SILVERBULL_PATTERN);
        if (!status.matches()) {
            WynntilsMod.warn("Could not parse Silverbull subscription status from item: "
                    + LoreUtils.getLore(rankSubscriptionItem));
            silverbullSubscriber.store(ConfirmedBoolean.FALSE);
            return;
        }

        silverbullSubscriber.store(status.group(1).equals("Active") ? ConfirmedBoolean.TRUE : ConfirmedBoolean.FALSE);
        if (silverbullSubscriber.get() != ConfirmedBoolean.TRUE) return;

        Matcher expiry = LoreUtils.matchLoreLine(rankSubscriptionItem, 1, SILVERBULL_DURATION_PATTERN);
        if (!expiry.matches()) {
            WynntilsMod.warn("Could not parse Silverbull subscription expiry from item: "
                    + LoreUtils.getLore(rankSubscriptionItem));
            silverbullExpiresAt.store(0L);
            return;
        }
        int weeks = expiry.group("weeks") == null ? 0 : Integer.parseInt(expiry.group("weeks"));
        int days = expiry.group("days") == null ? 0 : Integer.parseInt(expiry.group("days"));
        int hours = expiry.group("hours") == null ? 0 : Integer.parseInt(expiry.group("hours"));
        int minutes = expiry.group("minutes") == null ? 0 : Integer.parseInt(expiry.group("minutes"));
        int seconds = expiry.group("seconds") == null ? 0 : Integer.parseInt(expiry.group("seconds"));

        long expiryTime = System.currentTimeMillis()
                + TimeUnit.DAYS.toMillis(weeks * 7L + days)
                + TimeUnit.HOURS.toMillis(hours)
                + TimeUnit.MINUTES.toMillis(minutes)
                + TimeUnit.SECONDS.toMillis(seconds);
        silverbullExpiresAt.store(expiryTime);

        WynntilsMod.info(
                "Parsed Silverbull subscription status: " + silverbullSubscriber.get() + ", expires at: " + expiryTime);
    }
}
