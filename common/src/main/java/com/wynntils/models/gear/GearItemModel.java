/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.gear.type.CharmProfile;
import com.wynntils.models.gear.type.TomeProfile;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class GearItemModel extends Model {

    private static final Pattern ITEM_TIER =
            Pattern.compile("(?<Quality>Normal|Unique|Rare|Legendary|Fabled|Mythic|Set) "
                    + "(Raid Reward|Item)(?: \\[(?<Rolls>\\d+)])?");

    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9]+))");

    public static final Pattern ID_NEW_PATTERN =
            Pattern.compile("^§([ac])([-+]\\d+)(%|/3s|/5s| tier)?(?:§r§2(\\*{1,3}))? ?§r§7 ?(.*)$");

    private static final Pattern RANGE_PATTERN =
            Pattern.compile("^§([ac])([-+]\\d+)§r§2 to §r§a(\\d+)(%|/3s|/5s| tier)?§r§7 ?(.*)$");

    public GearItemModel(GearProfilesModel gearProfilesModel) {
        super(List.of(gearProfilesModel));
    }

    public TomeItem fromTomeItemStack(ItemStack itemStack, TomeProfile tomeProfile) {
        List<StatActualValue> identifications = new ArrayList<>();
        int rerolls = 0;

        // Parse lore for identifications and rerolls
        List<Component> lore = ComponentUtils.stripDuplicateBlank(itemStack.getTooltipLines(null, TooltipFlag.NORMAL));
        lore.remove(0); // remove item name

        for (Component loreLine : lore) {
            // Look for rerolls
            Optional<Integer> rerollOpt = rerollsFromLore(loreLine);
            if (rerollOpt.isPresent()) {
                rerolls = rerollOpt.get();
                continue;
            }

            // Look for identifications
            Optional<StatActualValue> gearIdOpt = gearIdentificationFromLore(loreLine);
            if (gearIdOpt.isEmpty()) continue;
            identifications.add(gearIdOpt.get());
        }

        return new TomeItem(tomeProfile, identifications, rerolls);
    }

    public CharmItem fromCharmItemStack(ItemStack itemStack, CharmProfile charmProfile) {
        List<StatActualValue> identifications = new ArrayList<>();
        int rerolls = 0;

        // Parse lore for identifications and rerolls
        List<Component> lore = ComponentUtils.stripDuplicateBlank(itemStack.getTooltipLines(null, TooltipFlag.NORMAL));
        lore.remove(0); // remove item name

        for (Component loreLine : lore) {
            // Look for rerolls
            Optional<Integer> rerollOpt = rerollsFromLore(loreLine);
            if (rerollOpt.isPresent()) {
                rerolls = rerollOpt.get();
                continue;
            }

            // Look for identifications
            Optional<StatActualValue> gearIdOpt = gearIdentificationFromLore(loreLine);
            if (gearIdOpt.isEmpty()) continue;
            identifications.add(gearIdOpt.get());
        }

        return new CharmItem(charmProfile, identifications, rerolls);
    }

    private Optional<Integer> rerollsFromLore(Component lore) {
        String unformattedLoreLine = WynnUtils.normalizeBadString(lore.getString());

        Matcher rerollMatcher = ITEM_TIER.matcher(unformattedLoreLine);
        if (!rerollMatcher.find()) return Optional.empty();

        int rerolls = 0;
        if (rerollMatcher.group("Rolls") != null) rerolls = Integer.parseInt(rerollMatcher.group("Rolls"));
        return Optional.of(rerolls);
    }

    // FIXME: this is a remnant, used by tome/charms...
    private Optional<StatActualValue> gearIdentificationFromLore(Component lore) {
        String unformattedLoreLine = WynnUtils.normalizeBadString(lore.getString());

        // Look for identifications
        Matcher statMatcher = ID_NEW_PATTERN.matcher(unformattedLoreLine);
        if (!statMatcher.matches()) return Optional.empty();

        int value = Integer.parseInt(statMatcher.group(2));
        String unit = statMatcher.group(3);
        String statDisplayName = statMatcher.group(5);

        StatType type = Models.Stat.fromDisplayName(statDisplayName, unit);
        if (type == null && Skill.isSkill(statDisplayName)) {
            // Skill point buff looks like stats when parsing
            return Optional.empty();
        }

        // FIXME: stars are not fixed here. align with normal parsing
        return Optional.of(new StatActualValue(type, value, -1));
    }
}
