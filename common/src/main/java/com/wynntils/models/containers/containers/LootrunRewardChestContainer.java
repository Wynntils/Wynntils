/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.containers;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.containers.Container;
import java.util.List;
import java.util.regex.Pattern;

public class LootrunRewardChestContainer extends Container {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\uDAFF\uDFF2\uE00A");

    public static final int CLOSE_CHEST_SLOT = 4;
    public static final StyledText CLOSE_CHEST_ITEM_NAME = StyledText.fromString("§c§lClose Chest");
    public static final Pattern REROLL_CONFIRM_PATTERN = Pattern.compile("§7Click again to confirm");
    public static final List<Integer> REROLL_REWARDS_SLOTS = List.of(5, 6);

    public LootrunRewardChestContainer() {
        super(TITLE_PATTERN);
    }
}
