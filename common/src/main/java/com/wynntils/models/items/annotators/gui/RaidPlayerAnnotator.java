/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gambits.type.Gambit;
import com.wynntils.models.items.items.gui.RaidPlayerItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class RaidPlayerAnnotator implements GuiItemAnnotator {
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^([\uE022\uE024\uE01B\uE08A\uE017] )?§(#[0-9A-Fa-f]{6,8})(?<player>.+)$");
    private static final Pattern LEVEL_PATTERN = Pattern.compile("^§a✔ §7Combat Level: §f[0-9]+$");
    private static final Pattern GAMBIT_PATTERN = Pattern.compile("^§a- §7(?<gambit>.+? Gambit)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(NAME_PATTERN);
        if (!matcher.matches()) return null;

        // The pattern is pretty broad, so we need to figure out based on the lore if this is really a Raid Player
        List<StyledText> lore = LoreUtils.getLore(itemStack);
        if (lore.isEmpty()) return null;

        if (!lore.getLast().getMatcher(LEVEL_PATTERN).matches()) return null;

        String player = matcher.group("player");
        boolean isNickname = false;

        if (player.startsWith("§o")) {
            player = player.substring(2);
            isNickname = true;
        }

        List<Gambit> gambits = lore.stream()
                .map(line -> line.getMatcher(GAMBIT_PATTERN))
                .filter(Matcher::matches)
                .map(m -> Gambit.fromItemName(m.group("gambit")))
                .toList();

        return new RaidPlayerItem(player, isNickname, gambits);
    }
}
