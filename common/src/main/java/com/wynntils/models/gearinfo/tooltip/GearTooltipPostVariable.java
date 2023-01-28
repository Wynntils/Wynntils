/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.tooltip;

import com.wynntils.models.concepts.Powder;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.gearinfo.type.GearMajorId;
import com.wynntils.models.gearinfo.type.GearRestrictions;
import com.wynntils.models.gearinfo.type.GearTier;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class GearTooltipPostVariable {
    public static List<Component> buildTooltip(GearInfo gearInfo, GearInstance gearInstance) {
        List<Component> baseTooltip = new ArrayList<>();

        // major ids
        // FIXME: Missing "<+Entropy: >Meteor falls..." which should be in AQUA.
        if (!gearInfo.fixedStats().majorIds().isEmpty()) {
            for (GearMajorId majorId : gearInfo.fixedStats().majorIds()) {
                Stream.of(RenderedStringUtils.wrapTextBySize(majorId.lore(), 150))
                        .forEach(c -> baseTooltip.add(Component.literal(c).withStyle(ChatFormatting.DARK_AQUA)));
            }
            baseTooltip.add(Component.literal(""));
        }

        // powder slots
        if (gearInfo.powderSlots() > 0) {
            if (gearInstance == null) {
                baseTooltip.add(Component.literal("[" + gearInfo.powderSlots() + " Powder Slots]")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                MutableComponent powderLine = Component.literal("["
                                + gearInstance.getPowders().size() + "/" + gearInfo.powderSlots() + "] Powder Slots ")
                        .withStyle(ChatFormatting.GRAY);
                if (!gearInstance.getPowders().isEmpty()) {
                    MutableComponent powderList = Component.literal("[");
                    for (Powder p : gearInstance.getPowders()) {
                        String symbol = p.getColoredSymbol();
                        if (!powderList.getSiblings().isEmpty()) symbol = " " + symbol;
                        powderList.append(Component.literal(symbol));
                    }
                    powderList.append(Component.literal("]"));
                    powderLine.append(powderList);
                }
                baseTooltip.add(powderLine);
            }
        }

        // tier & rerolls
        GearTier gearTier = gearInfo.tier();
        MutableComponent tier = Component.literal(gearTier.getName() + " Item").withStyle(gearTier.getChatFormatting());
        if (gearInstance != null && gearInstance.getRerolls() > 1) {
            tier.append(" [" + gearInstance.getRerolls() + "]");
        }
        baseTooltip.add(tier);

        // untradable
        if (gearInfo.metaInfo().restrictions() != GearRestrictions.NONE) {
            baseTooltip.add(Component.literal(StringUtils.capitalizeFirst(
                            gearInfo.metaInfo().restrictions().getDescription() + " Item"))
                    .withStyle(ChatFormatting.RED));
        }

        Optional<String> lore = gearInfo.metaInfo().lore();
        if (lore.isPresent()) {
            Stream.of(RenderedStringUtils.wrapTextBySize(lore.get(), 150))
                    .forEach(c -> baseTooltip.add(Component.literal(c).withStyle(ChatFormatting.DARK_GRAY)));
        }

        return baseTooltip;
    }
}
