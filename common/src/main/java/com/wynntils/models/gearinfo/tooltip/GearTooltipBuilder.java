/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.tooltip;

import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.screens.guides.GuideItemStack;
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
    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9{}]+))");
    private static final Pattern RANGE_PATTERN =
            Pattern.compile("^§([ac])([-+]\\d+)§r§2 to §r§a(\\d+)(%|/3s|/5s| tier)?§r§7 ?(.*)$");

    private static final GearTooltipStyle DEFAULT_TOOLTIP_STYLE =
            new GearTooltipStyle(GearTooltipSuffixType.PERCENT, StatListOrdering.DEFAULT, true, true, true, true, 1);

    private final Map<GearTooltipStyle, List<Component>> identificationsCache = new HashMap<>();

    private final GearInfo gearInfo;
    private final GearInstance gearInstance;
    private final List<Component> header;
    private final List<Component> footer;

    private GearTooltipBuilder(
            GearInfo gearInfo, GearInstance gearInstance, List<Component> header, List<Component> footer) {
        this.gearInfo = gearInfo;
        this.gearInstance = gearInstance;
        this.header = header;
        this.footer = footer;
    }

    /**
     * Creates a tooltip builder that provides a synthetic header and footer
     */
    public static GearTooltipBuilder buildNew(GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified) {
        List<Component> header = GearTooltipHeader.buildTooltip(gearInfo, gearInstance, hideUnidentified);
        List<Component> footer = GearTooltipFooter.buildTooltip(gearInfo, gearInstance);
        return new GearTooltipBuilder(gearInfo, gearInstance, header, footer);
    }

    /**
     * Creates a tooltip builder that parses the header and footer from an existing tooltip
     */
    public static GearTooltipBuilder fromParsedItemStack(ItemStack itemStack, GearItem gearItem) {
        GearInfo gearInfo = gearItem.getGearInfo();
        GearInstance gearInstance = gearItem.getGearInstance().orElse(null);
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);

        // guide item stacks are technically "unidentified" but should not show as such
        boolean hideUnidentified = itemStack instanceof GuideItemStack;

        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltips);
        List<Component> header = splitLore.a();
        List<Component> footer = splitLore.b();

        return new GearTooltipBuilder(gearInfo, gearInstance, header, footer);
    }

    public List<Component> getTooltipLines(GearTooltipStyle style) {
        List<Component> tooltip = new ArrayList<>();

        // Header and footer are always constant
        tooltip.addAll(header);

        // Between header and footer we have the list of identifications, which is different
        // depending on which decorations are requested
        List<Component> identifications = identificationsCache.get(style);
        if (identifications == null) {
            identifications = GearTooltipIdentifications.buildTooltip(gearInfo, gearInstance, style);
            identificationsCache.put(style, identifications);
        }
        tooltip.addAll(identifications);

        tooltip.addAll(footer);

        // FIXME: Can we get rid of this?
        return ComponentUtils.stripDuplicateBlank(tooltip);
    }

    public List<Component> getTooltipLines() {
        return getTooltipLines(DEFAULT_TOOLTIP_STYLE);
    }

    private static Pair<List<Component>, List<Component>> extractHeaderAndFooter(List<Component> lore) {
        List<Component> header = new ArrayList<>();
        List<Component> footer = new ArrayList<>();

        boolean headerEnded = false;
        boolean footerStarted = false;
        for (Component loreLine : lore) {
            String loreLineString = loreLine.getString();
            String coded = ComponentUtils.getCoded(loreLine);
            String normalizedCoded = WynnUtils.normalizeBadString(coded);
            String unformattedLoreLine = WynnUtils.normalizeBadString(loreLineString);

            if (!footerStarted && isIdentification(loreLine, unformattedLoreLine)) {
                headerEnded = true;
                // Don't keep identifications lines at all
                continue;
            }

            if (!headerEnded) {
                header.add(loreLine);
            } else {
                // From now on, we can skip looking for identification lines
                footerStarted = true;
                footer.add(loreLine);
            }
        }

        return Pair.of(header, footer);
    }

    private static boolean isIdentification(Component lore, String unformattedLoreLine) {
        // This looks quite messy, but is in effect what we did before
        // FIXME: Clean up?
        String loreString = lore.getString();
        Matcher identificationMatcher = ITEM_IDENTIFICATION_PATTERN.matcher(unformattedLoreLine);
        if (identificationMatcher.find()) {
            return true;
        }

        Matcher unidentifiedMatcher = RANGE_PATTERN.matcher(unformattedLoreLine);
        if (unidentifiedMatcher.matches()) {
            return true;
        }

        return false;
    }
}
