/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.CharmInstance;
import com.wynntils.models.rewards.type.CharmRequirements;
import com.wynntils.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class CharmTooltipComponent extends IdentifiableTooltipComponent<CharmInfo, CharmInstance> {
    @Override
    public List<Component> buildHeaderTooltip(
            CharmInfo charmInfo, CharmInstance charmInstance, boolean hideUnidentified) {
        List<Component> header = new ArrayList<>();

        // name
        String prefix = charmInstance == null && !hideUnidentified ? "Unidentified " : "";
        header.add(Component.literal(prefix + charmInfo.name())
                .withStyle(charmInfo.tier().getChatFormatting()));

        // Keep in inventory to gain bonus
        header.add(Component.literal("Keep in inventory to gain bonus").withStyle(ChatFormatting.GRAY));
        header.add(Component.empty());

        // requirements
        CharmRequirements requirements = charmInfo.requirements();
        int level = requirements.level();
        if (level != 0) {
            boolean fulfilled = Models.CombatXp.getCombatLevel().current() >= level;
            header.add(buildRequirementLine("Combat Lv. Min: " + level, fulfilled));
            header.add(Component.empty());
        }

        return header;
    }

    @Override
    public List<Component> buildFooterTooltip(CharmInfo charmInfo, CharmInstance charmInstance, boolean showItemType) {
        List<Component> footer = new ArrayList<>();

        footer.add(Component.empty());

        // tier & rerolls
        GearTier gearTier = charmInfo.tier();
        MutableComponent itemTypeName = showItemType ? Component.literal("Charm") : Component.literal("Raid Reward");
        MutableComponent tier = Component.literal(gearTier.getName())
                .withStyle(gearTier.getChatFormatting())
                .append(" ")
                .append(itemTypeName);
        if (charmInstance != null && charmInstance.rerolls() > 1) {
            tier.append(" [" + charmInstance.rerolls() + "]");
        }
        footer.add(tier);

        // restrictions (untradable, quest item)
        if (charmInfo.metaInfo().restrictions() != GearRestrictions.NONE) {
            footer.add(Component.literal(StringUtils.capitalizeFirst(
                            charmInfo.metaInfo().restrictions().getDescription()))
                    .withStyle(ChatFormatting.RED));
        }

        return footer;
    }
}
