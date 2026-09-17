/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.container.scriptedquery.QueryBuilder;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.handlers.container.type.ContainerContentChangeType;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.KeyInputEvent;
import com.wynntils.mc.event.SetLocalPlayerVehicleEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.character.event.CharacterDeathEvent;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.character.type.CharacterGamemode;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.character.type.SavableCharacterInfo;
import com.wynntils.models.character.type.SavableTome;
import com.wynntils.models.character.type.SavableTomeSet;
import com.wynntils.models.character.type.VehicleType;
import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.containers.CharacterCreationContainer;
import com.wynntils.models.containers.containers.CharacterInfoContainer;
import com.wynntils.models.containers.containers.MasteryTomesContainer;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.items.items.gui.CharacterCreationItem;
import com.wynntils.models.items.items.gui.CharacterItem;
import com.wynntils.models.rewards.type.TomeType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.wynn.InventoryUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Tracks persistent metadata about the player's selected character, such as
 * class type, level, reskin status, and unique character ID. This model is concerned with
 * long-lived identity and conceptual properties rather than transient
 * in-world behavior.
 */
public final class CharacterModel extends Model {
    private static final Pattern CHARACTER_ID_PATTERN = Pattern.compile("^[a-z0-9]{8}$");
    private static final Pattern INFO_MENU_CLASS_PATTERN = Pattern.compile("§7Class: §f(.+)");
    private static final Pattern INFO_MENU_LEVEL_PATTERN = Pattern.compile("§7Combat Lv: §f(\\d+)");

    public static final int CHARACTER_INFO_SLOT = 7;
    private static final int PROFESSION_INFO_SLOT = 17;
    public static final int GUILD_MENU_SLOT = 26;
    private static final int TOME_SLOT = 8;
    private static final int CONTENT_BOOK_SLOT = 62;
    private static final int TOME_MENU_CONTENT_BOOK_SLOT = 89;

    @Persisted
    public final Config<Boolean> queryCharacterInfoMenu = new Config<>(true);

    @Persisted
    private final Storage<SavableCharacterInfo> savedCharacterInfo =
            new Storage<>(new SavableCharacterInfo(ClassType.NONE, false, Collections.emptySet()));

    private List<TomeItem> equippedTomes = new ArrayList<>();

    private boolean hasCharacter;

    private ClassType classType = ClassType.NONE;
    private boolean reskinned;
    private int level;
    private Set<CharacterGamemode> gamemodes;

    // A hopefully unique string for each character ("class"). This is part of the
    // full character uuid, as presented by Wynncraft in the tooltip.
    private String id = "-";

    private String previousScanId = "";
    private boolean scanCharacterInfoPending;
    private boolean scanCharacterInfoAlreadyScanned;
    private boolean classTypeKnownThisSession;
    private boolean gamemodesKnownThisSession;

    private VehicleType vehicle = VehicleType.NONE;

    public CharacterModel() {
        super(List.of());
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

    public Set<CharacterGamemode> getGamemodes() {
        return Collections.unmodifiableSet(gamemodes);
    }

    // FIXME: Remove if this is not needed, or fix it for 2.1
    public boolean isHuntedMode() {
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldStateChanged(WorldStateEvent e) {
        // Whenever we're leaving a world, clear the current character
        if (e.getOldState() == WorldState.WORLD) {
            hasCharacter = false;
            classTypeKnownThisSession = false;
            gamemodesKnownThisSession = false;
        }

        if (e.getNewState() == WorldState.WORLD) {
            if (e.getOldState() == WorldState.INTERIM && e.isFirstJoinWorld()) {
                restoreCharacterInfoFromStorage();
            }

            scanCharacterInfoPending = true;
            scanCharacterInfoAlreadyScanned = false;
            scanCharacterInfo();
        } else {
            scanCharacterInfoPending = false;
            scanCharacterInfoAlreadyScanned = false;
        }

        if (e.getNewState() == WorldState.CHARACTER_SELECTION) {
            gamemodes = Collections.emptySet();
        }
    }

    @SubscribeEvent
    public void onContainerClick(ContainerClickEvent e) {
        if (Models.WorldState.getCurrentState() == WorldState.CHARACTER_SELECTION
                && e.getMouseButton() != InputConstants.MOUSE_BUTTON_RIGHT) {
            handleSelectedCharacter(e.getItemStack());
        }
    }

    @SubscribeEvent
    public void handleCreateCharacter(ContainerClickEvent event) {
        Container currentContainer = Models.Container.getCurrentContainer();

        if (!(currentContainer instanceof CharacterCreationContainer)) return;

        ItemStack itemStack = event.getItemStack();
        if (itemStack.isEmpty()) return;

        Optional<CharacterCreationItem> characterCreationItemOpt =
                Models.Item.asWynnItem(itemStack, CharacterCreationItem.class);
        if (characterCreationItemOpt.isEmpty()) return;

        CharacterCreationItem characterCreationItem = characterCreationItemOpt.get();

        setSelectedCharacterFromCharacterSelection(
                characterCreationItem.getClassType(),
                characterCreationItem.isReskinned(),
                1, // New character is always level 1
                characterCreationItem.getGamemodes());
    }

    public void handleSelectedCharacter(ItemStack itemStack) {
        if (!parseCharacter(itemStack)) return;
        hasCharacter = true;
        WynntilsMod.info("Selected character " + getCharacterString());
    }

    @SubscribeEvent
    public void onKey(KeyInputEvent event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        if (event.getKey() == GLFW.GLFW_KEY_H) {
            WynntilsMod.info("gamemodes: " + gamemodes);
        }
    }

    public boolean hasGamemode(CharacterGamemode gamemode) {
        return gamemodes.stream().anyMatch(gm -> gm == gamemode);
    }

    @SubscribeEvent
    public void onCharacterDeath(CharacterDeathEvent e) {
        if (!gamemodes.contains(CharacterGamemode.HARDCORE)) return;

        gamemodes = EnumSet.copyOf(gamemodes);
        gamemodes.remove(CharacterGamemode.HARDCORE);

        savedCharacterInfo.store(new SavableCharacterInfo(classType, reskinned, gamemodes));
    }

    public void setSelectedCharacterFromCharacterSelection(
            ClassType classType, boolean isReskinned, int level, Set<CharacterGamemode> gamemodes) {
        hasCharacter = true;
        updateCharacterInfo(classType, isReskinned, level, gamemodes);
        WynntilsMod.info("Selected character " + getCharacterString());
    }

    public void scanCharacterInfo() {
        scanCharacterInfo(null);
    }

    public void scanCharacterInfo(Runnable onComplete) {
        if (!updateCharacterId()) {
            scanCharacterInfoPending = true;
            scanCharacterInfoAlreadyScanned = false;
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        if (id.equals(previousScanId)) {
            hasCharacter = true;
            scanCharacterInfoPending = false;
            scanCharacterInfoAlreadyScanned = true;
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        if (!queryCharacterInfoMenu.get()
                && !Models.Guild.queryGuildInfoMenu.get()
                && !Models.Guild.queryGuildDiplomacyMenu.get()) return;

        WynntilsMod.info("Scheduling character info query");
        QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Character Info Query");
        queryBuilder.onError(msg -> {
            WynntilsMod.warn("Error querying Character Info: " + msg);
            if (onComplete != null) {
                onComplete.run();
            }
        });

        // Open compass/character menu
        queryBuilder.then(QueryStep.useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                .expectContainer(CharacterInfoContainer.class)
                .processIncomingContainer(this::parseCharacterContainer));

        if (Models.Guild.queryGuildInfoMenu.get() || Models.Guild.queryGuildDiplomacyMenu.get()) {
            // Scan guild container, if the player is in a guild
            Models.Guild.addGuildContainerQuerySteps(queryBuilder);
        }

        queryBuilder
                .execute(() -> {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .build()
                .executeQuery();

        previousScanId = id;
        scanCharacterInfoPending = false;
        scanCharacterInfoAlreadyScanned = true;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetSlot(SetSlotEvent.Post event) {
        if (!scanCharacterInfoPending || scanCharacterInfoAlreadyScanned) return;
        if (!Objects.equals(event.getContainer(), McUtils.inventory())) return;
        if (event.getSlot() != InventoryUtils.COMPASS_SLOT_NUM) return;

        scanCharacterInfo();
    }

    public VehicleType getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleType vehicle) {
        this.vehicle = vehicle;
    }

    @SubscribeEvent
    public void onVehicleSet(SetLocalPlayerVehicleEvent event) {
        if (event.getVehicle() == null) {
            setVehicle(VehicleType.NONE);
            return;
        }
        Entity vehicle = event.getVehicle();
        if (vehicle instanceof AbstractHorse) {
            setVehicle(VehicleType.HORSE);
        } else if (vehicle instanceof Display) {
            setVehicle(VehicleType.DISPLAY);
        } else {
            setVehicle(VehicleType.OTHER);
        }
    }

    private void parseCharacterContainer(ContainerContent container) {
        ItemStack characterInfoItem = container.items().get(CHARACTER_INFO_SLOT);
        ItemStack professionInfoItem = container.items().get(PROFESSION_INFO_SLOT);
        ItemStack guildInfoItem = container.items().get(GUILD_MENU_SLOT);

        Models.Profession.resetValueFromItem(professionInfoItem);
        Models.Guild.parseGuildInfoFromGuildMenu(guildInfoItem);

        parseCharacterFromCharacterMenu(characterInfoItem);
        hasCharacter = true;
        WynntilsMod.postEvent(new CharacterUpdateEvent());
        WynntilsMod.info("Deducing character " + getCharacterString());
    }

    private boolean updateCharacterId() {
        ItemStack compassItem = McUtils.inventory().items.get(CHARACTER_INFO_SLOT);
        List<StyledText> compassLore = LoreUtils.getLore(compassItem);
        if (compassLore.isEmpty()) {
            WynntilsMod.warn("Compass item had no character ID line");
            return false;
        }
        StyledText idLine = compassLore.getFirst();

        if (idLine == null || !idLine.matches(CHARACTER_ID_PATTERN)) {
            WynntilsMod.warn("Compass item had unexpected character ID line: " + idLine);
            return false;
        }

        id = idLine.getString();
        WynntilsMod.info("Selected character: " + id);
        return true;
    }

    private String getCharacterString() {
        return "CharacterInfo{" + "classType="
                + classType + ", reskinned="
                + reskinned + ", level="
                + level + ", id="
                + id + '}';
    }

    private void parseCharacterFromCharacterMenu(ItemStack characterInfoItem) {
        List<StyledText> lore = LoreUtils.getLore(characterInfoItem);

        int foundLevel = 0;
        String className = "";

        for (StyledText line : lore) {
            Matcher levelMatcher = line.getMatcher(INFO_MENU_LEVEL_PATTERN);
            if (levelMatcher.matches()) {
                foundLevel = Integer.parseInt(levelMatcher.group(1));
                continue;
            }

            Matcher classMatcher = line.getMatcher(INFO_MENU_CLASS_PATTERN);

            if (classMatcher.matches()) {
                className = classMatcher.group(1);
            }
        }
        ClassType foundClassType = ClassType.fromName(className);

        updateCharacterInfo(
                foundClassType, foundClassType != null && ClassType.isReskinned(className), foundLevel, null);
    }

    private boolean parseCharacter(ItemStack itemStack) {
        Optional<CharacterItem> characterItemOpt = Models.Item.asWynnItem(itemStack, CharacterItem.class);
        if (characterItemOpt.isEmpty()) return false;

        CharacterItem characterItem = characterItemOpt.get();

        updateCharacterInfo(
                characterItem.getClassType(),
                characterItem.isReskinned(),
                characterItem.getLevel(),
                characterItem.getGamemodes());
        return true;
    }

    private void updateCharacterInfo(
            ClassType classType, boolean reskinned, int level, Set<CharacterGamemode> gamemodes) {
        this.classType = classType;
        this.reskinned = reskinned;
        this.level = level;
        classTypeKnownThisSession = true;

        if (gamemodes != null) {
            this.gamemodes = gamemodes;
            gamemodesKnownThisSession = true;
        }

        savedCharacterInfo.store(new SavableCharacterInfo(classType, reskinned, gamemodes));
    }

    private void restoreCharacterInfoFromStorage() {
        if (classTypeKnownThisSession && gamemodesKnownThisSession) return;

        SavableCharacterInfo backup = savedCharacterInfo.get();

        if (backup != null) {
            if (!classTypeKnownThisSession) {
                classType = backup.classType();
                reskinned = backup.reskinned();
            }
            if (!gamemodesKnownThisSession) {
                gamemodes = backup.gamemodes();
            }
            hasCharacter = true;
        }
    }

    /**
     * Queries the compass menu for assigned skill points, then (if tomes are unlocked)
     * queries the mastery tome menu for the skill point tome and every other equipped tome.
     */
    public void queryAssignedAndTomeSkillPoints() {
        Models.SkillPoint.resetAssignedAndTomeSkillPoints();
        equippedTomes = new ArrayList<>();

        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Total and Tome Skill Point Query")
                .onError(msg -> WynntilsMod.warn("Failed to query skill points: " + msg))
                .then(QueryStep.useItemInHotbar(CHARACTER_INFO_SLOT)
                        .expectContainer(CharacterInfoContainer.class)
                        .verifyContentChange((container, changes, changeType) ->
                                verifyChange(container, changes, changeType, CONTENT_BOOK_SLOT))
                        .processIncomingContainer(Models.SkillPoint::processAssignedSkillPoints))
                .conditionalThen(
                        this::checkTomesUnlocked,
                        QueryStep.clickOnSlot(TOME_SLOT)
                                .expectContainer(MasteryTomesContainer.class)
                                .verifyContentChange((container, changes, changeType) ->
                                        verifyChange(container, changes, changeType, TOME_MENU_CONTENT_BOOK_SLOT))
                                .processIncomingContainer(this::processTomeMenu))
                .execute(Models.SkillPoint::calculateTotalSkillPoints)
                .build();

        query.executeQuery();
    }

    private void processTomeMenu(ContainerContent content) {
        Models.SkillPoint.processTomeSkillPoints(content);
        processOtherTomes(content);
    }

    private void processOtherTomes(ContainerContent content) {
        for (ItemStack itemStack : content.items()) {
            Optional<TomeItem> tomeItemOptional = Models.Item.asWynnItem(itemStack, TomeItem.class);
            if (tomeItemOptional.isEmpty()) continue;
            TomeItem tomeItem = tomeItemOptional.get();
            if (tomeItem.getItemInfo().type() == null) continue;

            equippedTomes.add(tomeItem);
        }
    }

    private boolean checkTomesUnlocked(ContainerContent content) {
        return LoreUtils.getStringLore(content.items().get(TOME_SLOT)).contains("✔");
    }

    private boolean verifyChange(
            ContainerContent content,
            Int2ObjectFunction<ItemStack> changes,
            ContainerContentChangeType changeType,
            int contentBookSlot) {
        return changeType == ContainerContentChangeType.SET_CONTENT
                && changes.containsKey(contentBookSlot)
                && (content.items().get(contentBookSlot).getItem() == Items.POTION);
    }

    public SavableTomeSet getCurrentTomeSet() {
        EncodingSettings encodingSettings = new EncodingSettings(true, true);
        List<SavableTome> tomes = new ArrayList<>();

        for (TomeItem tome : equippedTomes) {
            TomeType type = tome.getItemInfo().type();
            if (type == null) continue;

            ErrorOr<EncodedByteBuffer> errorOrEncoded = Models.ItemEncoding.encodeItem(tome, encodingSettings);
            if (errorOrEncoded.hasError()) {
                WynntilsMod.warn("Failed to encode tome " + tome.getName() + ": " + errorOrEncoded.getError());
                continue;
            }

            String encoded = errorOrEncoded.getValue().toBase64String();
            tomes.add(new SavableTome(type, tome.getName(), encoded));
        }

        return new SavableTomeSet(tomes);
    }
}
