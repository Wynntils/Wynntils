/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.account;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.mod.event.WynncraftConnectionEvent;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.container.scriptedquery.QueryBuilder;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.players.type.wynnplayer.WynnPlayerInfo;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.ConfirmedBoolean;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class AccountModel extends Model {
    private static final Pattern SILVERBULL_JOIN_PATTERN =
            Pattern.compile("§3Welcome to the §b✮ Silverbull Trading Company§3!");
    private static final Pattern SILVERBULL_UPDATE_PATTERN = Pattern.compile("§7Your subscription has been extended.");
    private static final Pattern SILVERBULL_PATTERN = Pattern.compile("§8Become a Silverbull Member to");
    // Test in AccountModel_SILVERBULL_DURATION_PATTERN
    private static final Pattern SILVERBULL_DURATION_PATTERN = Pattern.compile(
            "§#00a2e8ff- §7Expiration: §f(?:(?<weeks>\\d+) weeks?)? ?(?:(?<days>\\d+) days?)? ?(?:(?<hours>\\d+) hours?)? ?(?:(?<minutes>\\d+) minutes?)? ?(?:(?<seconds>\\d+) seconds?)?");
    public static final Component SILVERBULL_STAR = Component.literal(" ✮").withStyle(ChatFormatting.AQUA);
    private static final int COSMETICS_SLOT = 25;
    private static final int SILVERBULL_SLOT = 36;

    @Persisted
    private final Storage<Long> silverbullExpiresAt = new Storage<>(0L);

    @Persisted
    private final Storage<ConfirmedBoolean> silverbullSubscriber = new Storage<>(ConfirmedBoolean.UNCONFIRMED);

    private static final int PLAYER_INFO_UPDATE_MS = 60000;
    private ScheduledFuture<?> scheduledFuture;
    private final ScheduledExecutorService timerExecutor = new ScheduledThreadPoolExecutor(1);
    private WynnPlayerInfo playerInfo;

    public AccountModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onChatReceived(ChatMessageEvent.Match e) {
        StyledText message = e.getMessage().trim();

        if (message.matches(SILVERBULL_JOIN_PATTERN) || message.matches(SILVERBULL_UPDATE_PATTERN)) {
            silverbullSubscriber.store(ConfirmedBoolean.TRUE);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldStateChanged(WorldStateEvent e) {
        if (e.getNewState() != WorldState.WORLD) return;
        scanRankInfo(e.isFirstJoinWorld());
    }

    @SubscribeEvent
    public void onConnect(WynncraftConnectionEvent.Connected e) {
        scheduledFuture = timerExecutor.scheduleWithFixedDelay(
                this::updatePlayerInfo, 0, PLAYER_INFO_UPDATE_MS, TimeUnit.MILLISECONDS);
    }

    @SubscribeEvent
    public void onDisconnect(WynncraftConnectionEvent.Disconnected e) {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(false);
        }
    }

    public boolean isSilverbullSubscriber() {
        return silverbullSubscriber.get() == ConfirmedBoolean.TRUE;
    }

    public WynnPlayerInfo getPlayerInfo() {
        return playerInfo;
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
                    .expectContainerTitle(ContainerModel.STORE_MENU_NAME)
                    .processIncomingContainer(this::parseStoreContainer));
        } else {
            WynntilsMod.info("Skipping silverbull subscription query ("
                    + (silverbullExpiresAt.get() - System.currentTimeMillis()) + " ms left)");
            return;
        }

        queryBuilder.build().executeQuery();
    }

    private void parseStoreContainer(ContainerContent container) {
        ItemStack silverbullItem = container.items().get(SILVERBULL_SLOT);

        Matcher status = LoreUtils.matchLoreLine(silverbullItem, 6, SILVERBULL_PATTERN);
        silverbullSubscriber.store(status.matches() ? ConfirmedBoolean.FALSE : ConfirmedBoolean.TRUE);
        WynntilsMod.info("Parsed Silverbull subscription status: " + silverbullSubscriber.get());
        if (silverbullSubscriber.get() != ConfirmedBoolean.TRUE) return;

        Matcher expiry = LoreUtils.matchLoreLine(silverbullItem, 7, SILVERBULL_DURATION_PATTERN);
        if (!expiry.matches()) {
            WynntilsMod.warn(
                    "Could not parse Silverbull subscription expiry from item: " + LoreUtils.getLore(silverbullItem));
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

        WynntilsMod.info("Parsed Silverbull expiry: " + expiryTime);
    }

    private void updatePlayerInfo() {
        Models.Player.getPlayerFullInfo(McUtils.player().getStringUUID()).whenComplete((wynnPlayerInfo, throwable) -> {
            if (throwable != null) {
                WynntilsMod.warn("Failed to update player info", throwable);
            } else {
                this.playerInfo = wynnPlayerInfo;
            }
        });
    }
}
