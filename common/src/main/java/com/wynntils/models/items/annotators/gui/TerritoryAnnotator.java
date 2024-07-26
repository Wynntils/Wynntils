/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.models.territories.type.TerritoryUpgrade;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TerritoryAnnotator implements GuiItemAnnotator {
    private static final Pattern NAME_PATTERN =
            Pattern.compile("§.(?:§l)?(?:\\[§c§l!§4§l\\] §f)?(?<name>[^\\(\\)]+)(?<hq> \\(HQ\\))?");
    private static final Pattern GENERATOR_PATTERN =
            Pattern.compile("§.(?:[ⒷⒸⓀⒿ] )?\\+([0-9]*) (Emeralds|Ore|Wood|Fish|Crops) per Hour");
    private static final Pattern STORAGE_PATTERN =
            Pattern.compile("§.((?<type>[ⒷⒸⓀⒿ]) )?(?<current>[0-9]+)\\/(?<max>[0-9]+) stored");
    private static final Pattern TREASURY_PATTERN = Pattern.compile("§d✦ Treasury Bonus: §f([\\d\\.]+)%");
    private static final Pattern ALERTS_PATTERN = Pattern.compile("§c- §7(.+?)");
    private static final Pattern UPGRADES_PATTERN = Pattern.compile("§d- §7(.+?)§8 \\[Lv. (\\d+)]");
    private static final Pattern TERRITORY_ITEM_LAST_LINE_PATTERN =
            Pattern.compile("(§7Left-Click to view territory)|(§aClick to Select)|(§cClick to Unselect)");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(NAME_PATTERN);
        if (!matcher.matches()) return null;

        Deque<StyledText> lore = LoreUtils.getLore(itemStack);

        // Check if the last line is the view territory line,
        // otherwise it's not a territory item
        // This is a required check to not match "Guild Output",
        // as well as a quick performance optimization
        if (lore.isEmpty() || !lore.getLast().matches(TERRITORY_ITEM_LAST_LINE_PATTERN)) return null;

        String territoryName = matcher.group("name");
        boolean isHeadquarters = matcher.group("hq") != null;
        boolean isSelected = itemStack.getItem().equals(Items.GOLDEN_SHOVEL) && itemStack.getDamageValue() == 20;
        Map<GuildResource, Integer> generation = new EnumMap<>(GuildResource.class);
        Map<GuildResource, CappedValue> storage = new EnumMap<>(GuildResource.class);
        float treasuryBonus = -1;
        List<String> alerts = new ArrayList<>();
        Map<TerritoryUpgrade, Integer> upgrades = new EnumMap<>(TerritoryUpgrade.class);

        for (StyledText styledText : lore) {
            // Generator
            Matcher generatorMatcher = styledText.getMatcher(GENERATOR_PATTERN);
            if (generatorMatcher.matches()) {
                int amount = Integer.parseInt(generatorMatcher.group(1));
                GuildResource resource = GuildResource.fromName(generatorMatcher.group(2));
                generation.put(resource, amount);
                continue;
            }

            // Storage
            Matcher storageMatcher = styledText.getMatcher(STORAGE_PATTERN);
            if (storageMatcher.matches()) {
                int amount = Integer.parseInt(storageMatcher.group("current"));
                int max = Integer.parseInt(storageMatcher.group("max"));
                String type = storageMatcher.group("type");
                GuildResource resource = type == null ? GuildResource.EMERALDS : GuildResource.fromSymbol(type);
                storage.put(resource, new CappedValue(amount, max));
                continue;
            }

            // Treasury Bonus
            Matcher treasuryMatcher = styledText.getMatcher(TREASURY_PATTERN);
            if (treasuryMatcher.matches()) {
                treasuryBonus = Float.parseFloat(treasuryMatcher.group(1));
                continue;
            }

            // Alerts
            Matcher alertsMatcher = styledText.getMatcher(ALERTS_PATTERN);
            if (alertsMatcher.matches()) {
                alerts.add(alertsMatcher.group(1));
                continue;
            }

            // Upgrades
            Matcher upgradesMatcher = styledText.getMatcher(UPGRADES_PATTERN);
            if (upgradesMatcher.matches()) {
                TerritoryUpgrade upgrade = TerritoryUpgrade.fromName(upgradesMatcher.group(1));
                int level = Integer.parseInt(upgradesMatcher.group(2));
                upgrades.put(upgrade, level);
            }
        }

        return new TerritoryItem(
                territoryName,
                isHeadquarters,
                isSelected,
                Collections.unmodifiableMap(generation),
                Collections.unmodifiableMap(storage),
                treasuryBonus,
                Collections.unmodifiableList(alerts),
                upgrades);
    }
}
