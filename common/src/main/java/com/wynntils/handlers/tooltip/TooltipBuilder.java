/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.tooltip.gear.GearTooltipFooter;
import com.wynntils.handlers.tooltip.gear.GearTooltipHeader;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class TooltipBuilder {
    private static final TooltipStyle DEFAULT_TOOLTIP_STYLE =
            new TooltipStyle(StatListOrdering.WYNNCRAFT, false, false, true);
    private final IdentifiableItemProperty itemInfo;
    private final List<Component> header;
    private final List<Component> footer;

    // The identificationsCache is only valid if the cached dependencies matchs
    private ClassType cachedCurrentClass;
    private TooltipStyle cachedStyle;
    private TooltipIdentificationDecorator cachedDecorator;
    private List<Component> identificationsCache;

    private TooltipBuilder(IdentifiableItemProperty itemInfo, List<Component> header, List<Component> footer) {
        this.itemInfo = itemInfo;
        this.header = header;
        this.footer = footer;
    }

    /**
     * Creates a tooltip builder that provides a synthetic header and footer
     */
    public static TooltipBuilder buildNewGear(GearItem gearItem, boolean hideUnidentified) {
        GearInfo gearInfo = gearItem.getGearInfo();
        GearInstance gearInstance = gearItem.getGearInstance().orElse(null);

        List<Component> header = GearTooltipHeader.buildTooltip(gearInfo, gearInstance, hideUnidentified);
        List<Component> footer = GearTooltipFooter.buildTooltip(gearInfo, gearInstance);
        return new TooltipBuilder(gearItem, header, footer);
    }

    /**
     * Creates a tooltip builder that parses the header and footer from an existing tooltip
     */
    public static TooltipBuilder fromParsedItemStack(ItemStack itemStack, IdentifiableItemProperty itemInfo) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);

        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltips);
        List<Component> header = splitLore.a();
        List<Component> footer = splitLore.b();

        return new TooltipBuilder(itemInfo, header, footer);
    }

    public List<Component> getTooltipLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator decorator) {
        List<Component> tooltip = new ArrayList<>();

        // Header and footer are always constant
        tooltip.addAll(header);

        List<Component> identifications;

        // Identification lines are rendered differently depending on current class, requested
        // style and provided decorator. If all match, use cache.
        if (currentClass != cachedCurrentClass || cachedStyle != style || cachedDecorator != decorator) {
            identifications = TooltipIdentifications.buildTooltip(itemInfo, currentClass, decorator, style);
            identificationsCache = identifications;
            cachedCurrentClass = currentClass;
            cachedStyle = style;
            cachedDecorator = decorator;
        } else {
            identifications = identificationsCache;
        }

        tooltip.addAll(identifications);

        tooltip.addAll(footer);

        return tooltip;
    }

    public List<Component> getTooltipLines(ClassType currentClass) {
        return getTooltipLines(currentClass, DEFAULT_TOOLTIP_STYLE, null);
    }

    private static Pair<List<Component>, List<Component>> extractHeaderAndFooter(List<Component> lore) {
        List<Component> header = new ArrayList<>();
        List<Component> footer = new ArrayList<>();

        boolean headerEnded = false;
        boolean footerStarted = false;
        for (Component loreLine : lore) {
            StyledText codedLine = StyledText.fromComponent(loreLine).getNormalized();

            if (!footerStarted) {
                if (codedLine.matches(WynnItemParser.SET_BONUS_PATTEN)) {
                    headerEnded = true;
                    footerStarted = true;
                } else {
                    Matcher matcher = codedLine.getMatcher(WynnItemParser.IDENTIFICATION_STAT_PATTERN);
                    if (matcher.matches()) {
                        String statName = matcher.group(6);

                        if (Skill.isSkill(statName)) {
                            // Skill points counts to the header since they are fixed (but look like
                            // identified stats), so ignore those, and fall through
                        } else {
                            headerEnded = true;
                            // Don't keep identifications lines at all
                            continue;
                        }
                    }
                }
            }

            // We want to keep this line, so figure out where to put it
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
}
