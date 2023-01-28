/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.tooltip;

import com.wynntils.core.components.Models;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class GearTooltipBuilder {
    private static final Pattern ITEM_TIER =
            Pattern.compile("(?<Quality>Normal|Unique|Rare|Legendary|Fabled|Mythic|Set) Item(?: \\[(?<Rolls>\\d+)])?");
    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9{}]+))");
    private static final Pattern RANGE_PATTERN =
            Pattern.compile("^§([ac])([-+]\\d+)§r§2 to §r§a(\\d+)(%|/3s|/5s| tier)?§r§7 ?(.*)$");

    private final GearInfo gearInfo;
    private final GearInstance gearInstance;

    private List<Component> topTooltip;
    private List<Component> bottomTooltip;

    private final Map<GearTooltipVariableStats.IdentificationPresentationStyle, List<Component>> middleTooltipCache =
            new HashMap<>();

    private GearTooltipBuilder(GearInfo gearInfo, GearInstance gearInstance) {
        this.gearInfo = gearInfo;
        this.gearInstance = gearInstance;

        topTooltip = GearTooltipPreVariable.buildTooltip(gearInfo);
        bottomTooltip = GearTooltipPostVariable.buildTooltip(gearInfo, gearInstance);
    }

    private GearTooltipBuilder(
            GearInfo gearInfo, GearInstance gearInstance, List<Component> topTooltip, List<Component> bottomTooltip) {
        this.gearInfo = gearInfo;
        this.gearInstance = gearInstance;

        this.topTooltip = topTooltip;
        this.bottomTooltip = bottomTooltip;
    }

    public static GearTooltipBuilder fromGearInfo(GearInfo gearInfo) {
        return new GearTooltipBuilder(gearInfo, null);
    }

    public static GearTooltipBuilder fromGearItem(GearItem gearItem) {
        return new GearTooltipBuilder(
                gearItem.getGearInfo(), gearItem.getGearInstance().orElse(null));
    }

    public static GearTooltipBuilder fromItemStack(ItemStack itemStack, GearItem gearItem) {
        GearInfo gearInfo = gearItem.getGearInfo();
        GearInstance gearInstance = gearItem.getGearInstance().orElse(null);
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);

        // Skip first line which contains name
        Pair<List<Component>, List<Component>> splittedLore = splitLore(tooltips.subList(1, tooltips.size()), gearInfo);

        return new GearTooltipBuilder(gearInfo, gearInstance, splittedLore.a(), splittedLore.b());
    }

    public List<Component> getTooltipLines(GearTooltipVariableStats.IdentificationPresentationStyle style) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());

        // Top and bottom are always constant
        tooltip.addAll(topTooltip);

        // In the middle we have the list of identifications, which is different
        // depending on which decorations are requested
        List<Component> middleTooltip = middleTooltipCache.get(style);
        if (middleTooltip == null) {
            middleTooltip = GearTooltipVariableStats.buildTooltip(gearInfo, gearInstance, style);
            middleTooltipCache.put(style, middleTooltip);
        }

        tooltip.addAll(middleTooltip);

        tooltip.addAll(bottomTooltip);

        // FIXME: Can we get rid of this?
        return ComponentUtils.stripDuplicateBlank(tooltip);
    }

    private static Pair<List<Component>, List<Component>> splitLore(List<Component> lore, GearInfo gearInfo) {
        List<Component> topTooltip = new ArrayList<>();
        List<Component> bottomTooltip = new ArrayList<>();

        List<Component> baseTooltip = topTooltip;

        boolean setBonusIDs = false;
        for (Component loreLine : lore) {
            String loreLineString = loreLine.getString();
            String coded = ComponentUtils.getCoded(loreLine);
            String normalizedCoded = WynnUtils.normalizeBadString(coded);
            String unformattedLoreLine = WynnUtils.normalizeBadString(loreLineString);

            if (unformattedLoreLine.equals("Set Bonus:")) {
                baseTooltip.add(loreLine);
                setBonusIDs = true;
                continue;
            }

            if (setBonusIDs) {
                baseTooltip.add(loreLine);
                if (unformattedLoreLine.isBlank()) {
                    setBonusIDs = false;
                }
                continue;
            }

            if (unformattedLoreLine.contains("] Powder Slots")) {
                baseTooltip.add(loreLine);
                continue;
            }

            Matcher rerollMatcher = ITEM_TIER.matcher(unformattedLoreLine);
            if (rerollMatcher.find()) {
                baseTooltip.add(loreLine);
                continue;
            }

            if (!isIdLine(loreLine, gearInfo, unformattedLoreLine)) {
                baseTooltip.add(loreLine);
                continue;
            }

            // if we've reached this point, we have an id. It should not be stored anywhere
            if (baseTooltip == topTooltip) {
                // switch to bottom part
                baseTooltip = bottomTooltip;
            }
        }

        return Pair.of(topTooltip, bottomTooltip);
    }

    private static boolean isIdLine(Component lore, GearInfo item, String unformattedLoreLine) {
        // This looks quite messy, but is in effect what we did before
        // FIXME: Clean up?
        String loreString = lore.getString();
        Matcher identificationMatcher = ITEM_IDENTIFICATION_PATTERN.matcher(unformattedLoreLine);
        if (identificationMatcher.find()) return true;

        Matcher unidentifiedMatcher = RANGE_PATTERN.matcher(unformattedLoreLine);
        if (unidentifiedMatcher.matches()) return true;

        return false;
    }

    private Component getHoverName() {
        String prefix = gearInstance == null ? Models.GearItem.UNIDENTIFIED_PREFIX : "";

        return Component.literal(prefix + gearInfo.name())
                .withStyle(gearInfo.tier().getChatFormatting());
    }
}
