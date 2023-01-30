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
    private static final String UNIDENTIFIED_PREFIX = "Unidentified ";
    private static final Pattern REROLL_PATTERN =
            Pattern.compile("(?<Quality>Normal|Unique|Rare|Legendary|Fabled|Mythic|Set) Item(?: \\[(?<Rolls>\\d+)])?");
    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9{}]+))");
    private static final Pattern RANGE_PATTERN =
            Pattern.compile("^§([ac])([-+]\\d+)§r§2 to §r§a(\\d+)(%|/3s|/5s| tier)?§r§7 ?(.*)$");

    private static final GearTooltipStyle DEFAULT_TOOLTIP_STYLE =
            new GearTooltipStyle(GearTooltipSuffixType.PERCENT, StatListOrdering.DEFAULT, true, true, true, true, 1);

    private final GearInfo gearInfo;
    private final GearInstance gearInstance;
    private final boolean hideUnidentified;

    private List<Component> header;
    private List<Component> footer;

    private final Map<GearTooltipStyle, List<Component>> identificationsCache = new HashMap<>();

    private GearTooltipBuilder(GearInfo gearInfo, GearInstance gearInstance) {
        this.gearInfo = gearInfo;
        this.gearInstance = gearInstance;
        this.hideUnidentified = false;

        header = GearTooltipHeader.buildTooltip(gearInfo);
        footer = GearTooltipFooter.buildTooltip(gearInfo, gearInstance);
    }

    private GearTooltipBuilder(
            GearInfo gearInfo,
            GearInstance gearInstance,
            List<Component> header,
            List<Component> footer,
            boolean hideUnidentified) {
        this.gearInfo = gearInfo;
        this.gearInstance = gearInstance;
        this.hideUnidentified = hideUnidentified;

        this.header = header;
        this.footer = footer;
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

        boolean hideUnidentified = itemStack instanceof GuideItemStack;
        return new GearTooltipBuilder(gearInfo, gearInstance, splittedLore.a(), splittedLore.b(), hideUnidentified);
    }

    public List<Component> getTooltipLines(GearTooltipStyle style) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(getHoverName());

        // Header and footer are always constant
        tooltip.addAll(header);

        // Between header and footer we have the list of identifications, which is different
        // depending on which decorations are requested
        List<Component> identificationsTooltip = identificationsCache.get(style);
        if (identificationsTooltip == null) {
            identificationsTooltip = GearTooltipIdentifications.buildTooltip(gearInfo, gearInstance, style);
            identificationsCache.put(style, identificationsTooltip);
        }

        tooltip.addAll(identificationsTooltip);

        tooltip.addAll(footer);

        // FIXME: Can we get rid of this?
        return ComponentUtils.stripDuplicateBlank(tooltip);
    }

    public List<Component> getTooltipLines() {
        return getTooltipLines(DEFAULT_TOOLTIP_STYLE);
    }

    private static Pair<List<Component>, List<Component>> splitLore(List<Component> lore, GearInfo gearInfo) {
        List<Component> header = new ArrayList<>();
        List<Component> footer = new ArrayList<>();

        List<Component> baseTooltip = header;

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

            Matcher rerollMatcher = REROLL_PATTERN.matcher(unformattedLoreLine);
            if (rerollMatcher.find()) {
                baseTooltip.add(loreLine);
                continue;
            }

            if (!isIdLine(loreLine, gearInfo, unformattedLoreLine)) {
                baseTooltip.add(loreLine);
                continue;
            }

            // if we've reached this point, we have an id. It should not be stored anywhere
            if (baseTooltip == header) {
                // switch to footer
                baseTooltip = footer;
            }
        }

        return Pair.of(header, footer);
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
        String prefix = gearInstance == null && !hideUnidentified ? UNIDENTIFIED_PREFIX : "";

        return Component.literal(prefix + gearInfo.name())
                .withStyle(gearInfo.tier().getChatFormatting());
    }
}
