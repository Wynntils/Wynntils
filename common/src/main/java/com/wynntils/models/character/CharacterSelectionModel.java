/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character;

import com.wynntils.core.components.Model;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.models.character.type.ClassInfo;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.screens.characterselector.CharacterSelectorScreen;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.type.CodedString;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public final class CharacterSelectionModel extends Model {
    private static final Pattern NEW_CLASS_ITEM_NAME_PATTERN = Pattern.compile("§l§a\\[\\+\\] Create a new character");
    private static final Pattern CLASS_ITEM_NAME_PATTERN = Pattern.compile("§l§6\\[>\\] Select (.+)");
    private static final Pattern CLASS_ITEM_CLASS_PATTERN =
            Pattern.compile("§e- §r§7Class: (§r)?(§c§l☠§r)?(§6§l❂§r)?(§3§l⛏§r)?(§5§l⚔§r)?(\\s)?(§r)?§f(?<name>.+)");
    private static final Pattern CLASS_ITEM_LEVEL_PATTERN = Pattern.compile("§e- §r§7Level: §r§f(\\d+)");
    private static final Pattern CLASS_ITEM_XP_PATTERN = Pattern.compile("§e- §r§7XP: §r§f(\\d+)%");
    private static final Pattern CLASS_ITEM_SOUL_POINTS_PATTERN = Pattern.compile("§e- §r§7Soul Points: §r§f(\\d+)");
    private static final Pattern CLASS_ITEM_FINISHED_QUESTS_PATTERN =
            Pattern.compile("§e- §r§7Finished Quests: §r§f(\\d+)/\\d+");

    private static final String DEFAULT_CLASS_NAME = "This Character";

    private static final int EDIT_BUTTON_SLOT = 8;
    public static final CodedString CHARACTER_SELECTION_TITLE = CodedString.of("§8§lSelect a Character");

    private CharacterSelectorScreen currentScreen;
    private int containerId = -1;
    private int firstNewCharacterSlot = -1;
    private final List<ClassInfo> classInfoList = new ArrayList<>();

    public CharacterSelectionModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onScreenOpened(ScreenOpenedEvent.Post event) {
        if (event.getScreen() instanceof CharacterSelectorScreen characterSelectorScreen) {
            currentScreen = characterSelectorScreen;

            currentScreen.setClassInfoList(classInfoList);
            currentScreen.setFirstNewCharacterSlot(firstNewCharacterSlot);
        }
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent event) {
        if (!ComponentUtils.getCoded(event.getTitle()).equals(CHARACTER_SELECTION_TITLE)) {
            return;
        }

        containerId = event.getContainerId();
    }

    @SubscribeEvent
    public void onContainerItemsSet(ContainerSetContentEvent.Pre event) {
        if (event.getContainerId() != containerId) {
            return;
        }

        classInfoList.clear();

        List<ItemStack> items = event.getItems();
        for (int i = 0; i < items.size(); i++) {
            ItemStack itemStack = items.get(i);
            CodedString itemName = ComponentUtils.getCoded(itemStack.getHoverName());
            Matcher classItemMatcher = itemName.match(CLASS_ITEM_NAME_PATTERN);
            if (classItemMatcher.matches()) {
                ClassInfo classInfo = getClassInfoFromItem(itemStack, i, classItemMatcher.group(1));
                classInfoList.add(classInfo);
                continue;
            }

            if (firstNewCharacterSlot == -1
                    && itemName.match(NEW_CLASS_ITEM_NAME_PATTERN).matches()) {
                firstNewCharacterSlot = i;
            }
        }

        if (currentScreen != null) {
            currentScreen.setClassInfoList(classInfoList);
            currentScreen.setFirstNewCharacterSlot(firstNewCharacterSlot);
        }
    }

    private ClassInfo getClassInfoFromItem(ItemStack itemStack, int slot, String className) {
        ClassType classType = null;
        int level = 0;
        int xp = 0;
        int soulPoints = 0;
        int finishedQuests = 0;
        for (CodedString line : LoreUtils.getLore(itemStack)) {
            Matcher matcher = line.match(CLASS_ITEM_CLASS_PATTERN);

            if (matcher.matches()) {
                String classTypeString = matcher.group("name");
                classType = ClassType.fromName(classTypeString);
                if (DEFAULT_CLASS_NAME.equals(className)) {
                    className = classTypeString;
                }
                continue;
            }

            matcher = line.match(CLASS_ITEM_LEVEL_PATTERN);
            if (matcher.matches()) {
                level = Integer.parseInt(matcher.group(1));
                continue;
            }

            matcher = line.match(CLASS_ITEM_XP_PATTERN);
            if (matcher.matches()) {
                xp = Integer.parseInt(matcher.group(1));
                continue;
            }

            matcher = line.match(CLASS_ITEM_SOUL_POINTS_PATTERN);
            if (matcher.matches()) {
                soulPoints = Integer.parseInt(matcher.group(1));
                continue;
            }

            matcher = line.match(CLASS_ITEM_FINISHED_QUESTS_PATTERN);
            if (matcher.matches()) {
                finishedQuests = Integer.parseInt(matcher.group(1));
            }
        }

        return new ClassInfo(className, itemStack, slot, classType, level, xp, soulPoints, finishedQuests);
    }

    public void playWithCharacter(int slot) {
        ContainerUtils.clickOnSlot(
                slot,
                currentScreen.getActualClassSelectionScreen().getMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                currentScreen.getActualClassSelectionScreen().getMenu().getItems());
    }

    public void deleteCharacter(int slot) {
        ContainerUtils.clickOnSlot(
                slot,
                currentScreen.getActualClassSelectionScreen().getMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_RIGHT,
                currentScreen.getActualClassSelectionScreen().getMenu().getItems());
    }

    public void editCharacters(AbstractContainerMenu menu) {
        ContainerUtils.clickOnSlot(EDIT_BUTTON_SLOT, menu.containerId, GLFW.GLFW_MOUSE_BUTTON_LEFT, menu.getItems());
    }

    public void createNewClass() {
        ContainerUtils.clickOnSlot(
                currentScreen.getFirstNewCharacterSlot(),
                currentScreen.getActualClassSelectionScreen().getMenu().containerId,
                GLFW.GLFW_MOUSE_BUTTON_LEFT,
                currentScreen.getActualClassSelectionScreen().getMenu().getItems());
    }
}
