/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.models.gearinfo.type.GearTier;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class RewardsModel extends Model {
    public static final Pattern STAT_LINE_PATTERN =
            Pattern.compile("^§([ac])([-+]\\d+)(%|/3s|/5s| tier)?(?:§r§2(\\*{1,3}))? ?§r§7 ?(.*)$");
    private static final Pattern REROLL_PATTERN =
            Pattern.compile("(?<Quality>Normal|Unique|Rare|Legendary|Fabled|Mythic|Set) "
                    + "(Raid Reward|Item)(?: \\[(?<Rolls>\\d+)])?");

    public RewardsModel() {
        super(List.of());
    }

    public TomeInfo getTomeInfo(Matcher matcher, TomeType tomeType, GearTier gearTier, String variant, String tier) {
        // TODO: replace with API lookup
        TomeInfo tomeInfo = new TomeInfo(matcher.group(1), gearTier, variant, tomeType, tier);
        return tomeInfo;
    }

    public CharmInfo getCharmInfo(Matcher matcher, GearTier tier, String type) {
        // TODO: replace with API lookup
        CharmInfo charmInfo = new CharmInfo(matcher.group(1), tier, type);
        return charmInfo;
    }

    public CharmItem fromCharmItemStack(ItemStack itemStack, CharmInfo charmInfo) {
        Pair<List<StatActualValue>, Integer> parsedLore = parseLore(itemStack);
        return new CharmItem(charmInfo, parsedLore.a(), parsedLore.b());
    }

    public TomeItem fromTomeItemStack(ItemStack itemStack, TomeInfo tomeInfo) {
        Pair<List<StatActualValue>, Integer> parsedLore = parseLore(itemStack);
        return new TomeItem(tomeInfo, parsedLore.a(), parsedLore.b());
    }

    private Pair<List<StatActualValue>, Integer> parseLore(ItemStack itemStack) {
        List<StatActualValue> identifications = new ArrayList<>();
        int rerolls = 0;

        // Parse lore for identifications and rerolls
        List<Component> lore = ComponentUtils.stripDuplicateBlank(itemStack.getTooltipLines(null, TooltipFlag.NORMAL));
        lore.remove(0); // remove item name

        for (Component loreLine : lore) {
            // Look for rerolls
            Optional<Integer> rerollOpt = getRerolls(loreLine);
            if (rerollOpt.isPresent()) {
                rerolls = rerollOpt.get();
                continue;
            }

            // Look for identifications
            Optional<StatActualValue> statValueOpt = getStatValue(loreLine);
            if (statValueOpt.isEmpty()) continue;
            identifications.add(statValueOpt.get());
        }

        return Pair.of(identifications, rerolls);
    }

    private Optional<Integer> getRerolls(Component lore) {
        String unformattedLoreLine = WynnUtils.normalizeBadString(lore.getString());

        Matcher rerollMatcher = REROLL_PATTERN.matcher(unformattedLoreLine);
        if (!rerollMatcher.find()) return Optional.empty();

        int rerolls = 0;
        if (rerollMatcher.group("Rolls") != null) rerolls = Integer.parseInt(rerollMatcher.group("Rolls"));
        return Optional.of(rerolls);
    }

    private Optional<StatActualValue> getStatValue(Component lore) {
        String unformattedLoreLine = WynnUtils.normalizeBadString(lore.getString());

        // Look for identifications
        Matcher statMatcher = STAT_LINE_PATTERN.matcher(unformattedLoreLine);
        if (!statMatcher.matches()) return Optional.empty();

        int value = Integer.parseInt(statMatcher.group(2));
        String unit = statMatcher.group(3);
        String statDisplayName = statMatcher.group(5);

        StatType type = Models.Stat.fromDisplayName(statDisplayName, unit);

        // FIXME: Do charms and tomes have stars?
        return Optional.of(new StatActualValue(type, value, 0));
    }
}
