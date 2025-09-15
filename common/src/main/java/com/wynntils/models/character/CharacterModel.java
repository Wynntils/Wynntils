/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.container.scriptedquery.QueryBuilder;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.SetLocalPlayerVehicleEvent;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.character.type.VehicleType;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.items.items.gui.CharacterItem;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

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

    private boolean hasCharacter;

    private ClassType classType = ClassType.NONE;
    private boolean reskinned;
    private int level;

    // A hopefully unique string for each character ("class"). This is part of the
    // full character uuid, as presented by Wynncraft in the tooltip.
    private String id = "-";

    private String previousScanId = "";

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

    // FIXME: Remove if this is not needed, or fix it for 2.1
    public boolean isHuntedMode() {
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldStateChanged(WorldStateEvent e) {
        // Whenever we're leaving a world, clear the current character
        if (e.getOldState() == WorldState.WORLD) {
            hasCharacter = false;
        }

        if (e.getNewState() == WorldState.WORLD) {
            // We need to parse the current character id from our inventory
            updateCharacterId();

            // We need to scan character info and profession info as well.
            scanCharacterInfo();
        }
    }

    @SubscribeEvent
    public void onContainerClick(ContainerClickEvent e) {
        if (Models.WorldState.getCurrentState() == WorldState.CHARACTER_SELECTION
                && e.getMouseButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            handleSelectedCharacter(e.getItemStack());
        }
    }

    public void handleSelectedCharacter(ItemStack itemStack) {
        if (!parseCharacter(itemStack)) return;
        hasCharacter = true;
        WynntilsMod.info("Selected character " + getCharacterString());
    }

    public void setSelectedCharacterFromCharacterSelection(ClassType classType, boolean isReskinned, int level) {
        hasCharacter = true;
        updateCharacterInfo(classType, isReskinned, level);
        WynntilsMod.info("Selected character " + getCharacterString());
    }

    public void scanCharacterInfo() {
        if (id.equals(previousScanId)) {
            hasCharacter = true;
            return;
        }

        WynntilsMod.info("Scheduling character info query");
        QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Character Info Query");
        queryBuilder.onError(msg -> WynntilsMod.warn("Error querying Character Info: " + msg));

        // Open compass/character menu
        queryBuilder.then(QueryStep.useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                .expectContainerTitle(ContainerModel.CHARACTER_INFO_NAME)
                .processIncomingContainer(this::parseCharacterContainer));

        // Scan guild container, if the player is in a guild
        Models.Guild.addGuildContainerQuerySteps(queryBuilder);

        queryBuilder.build().executeQuery();

        previousScanId = id;
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

    private void updateCharacterId() {
        ItemStack compassItem = McUtils.inventory().items.get(CHARACTER_INFO_SLOT);
        List<StyledText> compassLore = LoreUtils.getLore(compassItem);
        StyledText idLine = compassLore.getFirst();

        if (idLine == null || !idLine.matches(CHARACTER_ID_PATTERN)) {
            WynntilsMod.warn("Compass item had unexpected character ID line: " + idLine);
            return;
        }

        id = idLine.getString();
        WynntilsMod.info("Selected character: " + id);
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

        updateCharacterInfo(foundClassType, foundClassType != null && ClassType.isReskinned(className), foundLevel);
    }

    private boolean parseCharacter(ItemStack itemStack) {
        Optional<CharacterItem> characterItemOpt = Models.Item.asWynnItem(itemStack, CharacterItem.class);
        if (characterItemOpt.isEmpty()) return false;

        CharacterItem characterItem = characterItemOpt.get();

        updateCharacterInfo(characterItem.getClassType(), characterItem.isReskinned(), characterItem.getLevel());
        return true;
    }

    private void updateCharacterInfo(ClassType classType, boolean reskinned, int level) {
        this.classType = classType;
        this.reskinned = reskinned;
        this.level = level;
    }
}
