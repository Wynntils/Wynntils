/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Model;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;

public final class ContainerModel extends Model {
    private static final Pattern ABILITY_TREE_PATTERN =
            Pattern.compile("(?:Warrior|Shaman|Mage|Assassin|Archer) Abilities");

    // Test suite: https://regexr.com/7b4l0
    private static final Pattern BANK_PATTERN = Pattern.compile("§0\\[Pg\\. \\d+\\] §8[a-zA-Z0-9_]+'s§0 Bank");

    // Test suite: https://regexr.com/7b4lf
    private static final Pattern GUILD_BANK_PATTERN =
            Pattern.compile("[a-zA-Z ]+: Bank \\((?:Everyone|High Ranked)\\)");

    // Test suite: https://regexr.com/7b4m1
    private static final Pattern BLOCK_BANK_PATTERN =
            Pattern.compile("§0\\[Pg\\. \\d+\\] §8[a-zA-Z0-9_]+'s§0 Block Bank");

    // Test suite: https://regexr.com/7b4ma
    private static final Pattern MISC_BUCKET_PATTERN =
            Pattern.compile("§0\\[Pg\\. \\d+\\] §8[a-zA-Z0-9_]+'s§0 Misc\\. Bucket");

    private static final Pattern LOOT_CHEST_PATTERN = Pattern.compile("Loot Chest (.+)");

    public ContainerModel() {
        super(List.of());
    }

    public boolean isAbilityTreeScreen(Screen screen) {
        return ABILITY_TREE_PATTERN.matcher(screen.getTitle().getString()).matches();
    }

    public boolean isBankScreen(Screen screen) {
        return BANK_PATTERN.matcher(ComponentUtils.getCoded(screen.getTitle())).matches();
    }

    public boolean isGuildBankScreen(Screen screen) {
        return GUILD_BANK_PATTERN
                .matcher(ComponentUtils.getCoded(screen.getTitle()))
                .matches();
    }

    public boolean isTradeMarketScreen(Screen screen) {
        if (!(screen instanceof ContainerScreen cs)) return false;
        // No regex required, title is very simple and can be checked with .equals()
        return cs.getMenu().getRowCount() == 6
                && ComponentUtils.getCoded(screen.getTitle()).equals("Trade Market");
    }

    public boolean isBlockBankScreen(Screen screen) {
        return BLOCK_BANK_PATTERN
                .matcher(ComponentUtils.getCoded(screen.getTitle()))
                .matches();
    }

    public boolean isMiscBucketScreen(Screen screen) {
        return MISC_BUCKET_PATTERN
                .matcher(ComponentUtils.getCoded(screen.getTitle()))
                .matches();
    }

    public boolean isLootChest(Screen screen) {
        return screen instanceof ContainerScreen && lootChestMatcher(screen).matches();
    }

    public boolean isLootChest(String title) {
        return title.startsWith("Loot Chest");
    }

    public boolean isLootOrRewardChest(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return false;

        String title = screen.getTitle().getString();
        return isLootChest(title) || title.startsWith("Daily Rewards") || title.contains("Objective Rewards");
    }

    public Matcher lootChestMatcher(Screen screen) {
        return LOOT_CHEST_PATTERN.matcher(
                WynnUtils.normalizeBadString(ComponentUtils.getUnformatted(screen.getTitle())));
    }
}
