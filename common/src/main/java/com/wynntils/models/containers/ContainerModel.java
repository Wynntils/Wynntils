/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;

public final class ContainerModel extends Model {
    // Test in ContainerModel_ABILITY_TREE_PATTERN
    public static final Pattern ABILITY_TREE_PATTERN =
            Pattern.compile("(?:Warrior|Shaman|Mage|Assassin|Archer) Abilities");

    // Test in ContainerModel_LOOT_CHEST_PATTERN
    private static final Pattern LOOT_CHEST_PATTERN = Pattern.compile("Loot Chest (§.)\\[.+\\]");

    public static final String CHARACTER_INFO_NAME = "Character Info";
    public static final String COSMETICS_MENU_NAME = "Crates, Bombs & Cosmetics";
    public static final String MASTERY_TOMES_NAME = "Mastery Tomes";

    private static final StyledText SEASKIPPER_TITLE = StyledText.fromString("V.S.S. Seaskipper");

    public ContainerModel() {
        super(List.of());
    }

    public boolean isCharacterInfoScreen(Screen screen) {
        return StyledText.fromComponent(screen.getTitle())
                .getStringWithoutFormatting()
                .equals(CHARACTER_INFO_NAME);
    }

    public boolean isLootChest(Screen screen) {
        return screen instanceof ContainerScreen && lootChestMatcher(screen).matches();
    }

    public boolean isLootChest(String title) {
        return title.startsWith("Loot Chest");
    }

    public boolean isRewardChest(String title) {
        return title.startsWith("Daily Rewards")
                || title.contains("Objective Rewards")
                || title.contains("Challenge Rewards")
                || title.contains("Flying Chest");
    }

    public boolean isLootOrRewardChest(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return false;

        String title = screen.getTitle().getString();
        return isLootOrRewardChest(title);
    }

    public boolean isLootOrRewardChest(String title) {
        return isLootChest(title) || isRewardChest(title);
    }

    public boolean isSeaskipper(Component component) {
        return StyledText.fromComponent(component).equals(SEASKIPPER_TITLE);
    }

    public Matcher lootChestMatcher(Screen screen) {
        return StyledText.fromComponent(screen.getTitle()).getNormalized().getMatcher(LOOT_CHEST_PATTERN);
    }
}
