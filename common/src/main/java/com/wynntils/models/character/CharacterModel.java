/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.MenuEvent.MenuClosedEvent;
import com.wynntils.mc.event.PlayerTeleportEvent;
import com.wynntils.models.character.event.CharacterDeathEvent;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Position;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CharacterModel extends Model {
    private static final Pattern CLASS_MENU_CLASS_PATTERN = Pattern.compile("§e- §7Class: §f(.+)");
    private static final Pattern CLASS_MENU_LEVEL_PATTERN = Pattern.compile("§e- §7Level: §f(\\d+)");
    private static final Pattern INFO_MENU_CLASS_PATTERN = Pattern.compile("§7Class: §f(.+)");
    private static final Pattern INFO_MENU_LEVEL_PATTERN = Pattern.compile("§7Combat Lv: §f(\\d+)");
    // Test suite: https://regexr.com/7i87d
    private static final Pattern SILVERBULL_PATTERN = Pattern.compile("§7Subscription: §[ac][✖✔] ((?:Ina|A)ctive)");
    // Test suite: https://regexr.com/7irg0
    private static final Pattern SILVERBULL_DURATION_PATTERN = Pattern.compile(
            "§7Expiration: §f(?:(?<weeks>\\d+) weeks?)? ?(?:(?<days>\\d+) days?)? ?(?:(?<hours>\\d+) hours?)?");
    // Test suite: https://regexr.com/7ntns
    private static final Pattern VETERAN_PATTERN = Pattern.compile("§7Rank: §[6dba]Vet");

    private static final int RANK_SUBSCRIPTION_INFO_SLOT = 0;
    private static final int CHARACTER_INFO_SLOT = 7;
    private static final int SOUL_POINT_SLOT = 8;
    private static final int PROFESSION_INFO_SLOT = 17;
    private static final int GUILD_INFO_SLOT = 26;

    // we need a .* in front because the message may have a custom timestamp prefix (or some other mod could do
    // something weird)
    private static final Pattern WYNN_DEATH_MESSAGE = Pattern.compile(".* §4§lYou have died\\.\\.\\.");
    private Position lastPositionBeforeTeleport;
    private Location lastDeathLocation;

    private boolean inCharacterSelection;
    private boolean hasCharacter;

    private ClassType classType;
    private boolean reskinned;
    private int level;

    private boolean isVeteran = false;

    @Persisted
    public final Storage<Long> silverbullExpiresAt = new Storage<>(0L);

    @Persisted
    public final Storage<Boolean> silverbullSubscriber = new Storage<>(false);

    // A hopefully unique string for each character ("class"). This is part of the
    // full character uuid, as presented by Wynncraft in the tooltip.
    private String id = "-";

    public CharacterModel() {
        super(List.of());
    }

    public boolean isSilverbullSubscriber() {
        return silverbullSubscriber.get();
    }

    public ClassType getClassType() {
        if (!hasCharacter) return ClassType.NONE;

        return classType;
    }

    public boolean isReskinned() {
        if (!hasCharacter) return false;

        return reskinned;
    }

    /** Returns the current class name, wrt reskinned or not.
     */
    public String getActualName() {
        return getClassType().getActualName(isReskinned());
    }

    public boolean hasCharacter() {
        return hasCharacter;
    }

    public String getId() {
        // We can't return an empty string, otherwise we risk making our config file messed up (empty string map key for
        // ItemLockFeature)
        if (!hasCharacter) return "-";

        return id;
    }

    public boolean isVeteran() {
        return isVeteran;
    }

    @SubscribeEvent
    public void onMenuClosed(MenuClosedEvent e) {
        inCharacterSelection = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldStateChanged(WorldStateEvent e) {
        // Whenever we're leaving a world, clear the current character
        if (e.getOldState() == WorldState.WORLD) {
            hasCharacter = false;
            // This should not be needed, but have it as a safeguard
            inCharacterSelection = false;
        }

        if (e.getNewState() == WorldState.CHARACTER_SELECTION) {
            inCharacterSelection = true;
        }

        if (e.getNewState() == WorldState.WORLD) {
            // We need to parse the current character id from our inventory
            updateCharacterId();

            WynntilsMod.info("Scheduling character info query");
            // We need to scan character info and profession info as well.
            scanCharacterInfoPage();
            if (System.currentTimeMillis() > silverbullExpiresAt.get()) {
                scanSilverbullSubscriptionItem();
            } else {
                WynntilsMod.info("Skipping silverbull subscription query ("
                        + (silverbullExpiresAt.get() - System.currentTimeMillis()) + " ms left)");
            }
        }
    }

    private void scanCharacterInfoPage() {
        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Character Info Query")
                .onError(msg -> WynntilsMod.warn("Error querying Character Info: " + msg))

                // Open compass/character menu
                .then(QueryStep.useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                        .expectContainerTitle(ContainerModel.CHARACTER_INFO_NAME)
                        .processIncomingContainer(this::parseCharacterContainer))
                .build();

        query.executeQuery();
    }

    private void scanSilverbullSubscriptionItem() {
        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Silverbull Subscription Query")
                .onError(msg -> WynntilsMod.warn("Error querying Silverbull subscription: " + msg))

                // Open /use menu
                .then(QueryStep.sendCommand("use")
                        .expectContainerTitle(ContainerModel.COSMETICS_MENU_NAME)
                        .processIncomingContainer(this::parseCratesBombsCosmeticsContainer))
                .build();

        query.executeQuery();
    }

    private void parseCharacterContainer(ContainerContent container) {
        ItemStack characterInfoItem = container.items().get(CHARACTER_INFO_SLOT);
        ItemStack professionInfoItem = container.items().get(PROFESSION_INFO_SLOT);
        ItemStack guildInfoItem = container.items().get(GUILD_INFO_SLOT);

        Models.Profession.resetValueFromItem(professionInfoItem);
        Models.Guild.parseGuildInfoFromGuildMenu(guildInfoItem);

        parseCharacterFromCharacterMenu(characterInfoItem);
        hasCharacter = true;
        WynntilsMod.postEvent(new CharacterUpdateEvent());
        WynntilsMod.info("Deducing character " + getCharacterString());
    }

    private void parseCratesBombsCosmeticsContainer(ContainerContent container) {
        ItemStack rankSubscriptionItem = container.items().get(RANK_SUBSCRIPTION_INFO_SLOT);

        Matcher veteran = LoreUtils.matchLoreLine(rankSubscriptionItem, 0, VETERAN_PATTERN);

        isVeteran = veteran.matches();

        Matcher status = LoreUtils.matchLoreLine(rankSubscriptionItem, 0, SILVERBULL_PATTERN);
        if (!status.matches()) {
            WynntilsMod.warn("Could not parse Silverbull subscription status from item: "
                    + LoreUtils.getLore(rankSubscriptionItem));
            silverbullSubscriber.store(false);
            return;
        }

        silverbullSubscriber.store(status.group(1).equals("Active"));
        if (!silverbullSubscriber.get()) return;

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

        long expiryTime =
                System.currentTimeMillis() + TimeUnit.DAYS.toMillis(weeks * 7L + days) + TimeUnit.HOURS.toMillis(hours);
        silverbullExpiresAt.store(expiryTime);

        WynntilsMod.info(
                "Parsed Silverbull subscription status: " + silverbullSubscriber.get() + ", expires at: " + expiryTime);
    }

    private void updateCharacterId() {
        ItemStack soulPointItem = McUtils.inventory().items.get(SOUL_POINT_SLOT);

        List<StyledText> soulLore = LoreUtils.getLore(soulPointItem);

        String id = "";
        for (StyledText line : soulLore) {
            if (line.startsWith(ChatFormatting.DARK_GRAY.toString())) {
                id = line.getString(PartStyle.StyleType.NONE);
                break;
            }
        }

        WynntilsMod.info("Selected character: " + id);

        this.id = id;
    }

    private String getCharacterString() {
        return "CharacterInfo{" + "classType="
                + classType + ", reskinned="
                + reskinned + ", level="
                + level + ", id="
                + id + ", silverbullSubscriber="
                + silverbullSubscriber.get() + '}';
    }

    private void parseCharacterFromCharacterMenu(ItemStack characterInfoItem) {
        List<StyledText> lore = LoreUtils.getLore(characterInfoItem);

        int level = 0;
        String className = "";

        for (StyledText line : lore) {
            Matcher levelMatcher = line.getMatcher(INFO_MENU_LEVEL_PATTERN);
            if (levelMatcher.matches()) {
                level = Integer.parseInt(levelMatcher.group(1));
                continue;
            }

            Matcher classMatcher = line.getMatcher(INFO_MENU_CLASS_PATTERN);

            if (classMatcher.matches()) {
                className = classMatcher.group(1);
            }
        }
        ClassType classType = ClassType.fromName(className);

        updateCharacterInfo(classType, classType != null && ClassType.isReskinned(className), level);
    }

    @SubscribeEvent
    public void onContainerClick(ContainerClickEvent e) {
        if (inCharacterSelection) {
            if (e.getItemStack().getItem() == Items.AIR) return;
            parseCharacter(e.getItemStack());
            hasCharacter = true;
            WynntilsMod.postEvent(new CharacterUpdateEvent());
            WynntilsMod.info("Selected character " + getCharacterString());
        }
    }

    private void parseCharacter(ItemStack itemStack) {
        List<StyledText> lore = LoreUtils.getLore(itemStack);

        int level = 0;
        String className = "";

        for (StyledText line : lore) {
            Matcher levelMatcher = line.getMatcher(CLASS_MENU_LEVEL_PATTERN);
            if (levelMatcher.matches()) {
                level = Integer.parseInt(levelMatcher.group(1));
                continue;
            }

            Matcher classMatcher = line.getMatcher(CLASS_MENU_CLASS_PATTERN);

            if (classMatcher.matches()) {
                className = classMatcher.group(1);
            }
        }
        ClassType classType = ClassType.fromName(className);

        updateCharacterInfo(classType, classType != null && ClassType.isReskinned(className), level);
    }

    private void updateCharacterInfo(ClassType classType, boolean reskinned, int level) {
        this.classType = classType;
        this.reskinned = reskinned;
        this.level = level;
    }

    @SubscribeEvent
    public void onChatReceived(ChatMessageReceivedEvent e) {
        if (!e.getStyledText().matches(WYNN_DEATH_MESSAGE)) return;
        lastDeathLocation = Location.containing(lastPositionBeforeTeleport);
        WynntilsMod.postEvent(new CharacterDeathEvent(lastDeathLocation));
    }

    @SubscribeEvent
    public void beforePlayerTeleport(PlayerTeleportEvent e) {
        if (McUtils.player() == null) return;
        lastPositionBeforeTeleport = McUtils.player().position();
    }
}
