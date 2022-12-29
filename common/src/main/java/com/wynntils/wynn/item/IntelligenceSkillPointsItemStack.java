/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item;

import com.wynntils.core.components.Managers;
import com.wynntils.wynn.item.properties.ItemProperty;
import com.wynntils.wynn.item.properties.SkillPointProperty;
import com.wynntils.wynn.objects.SpellType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class IntelligenceSkillPointsItemStack extends WynnItemStack {
    private List<Component> tooltip;

    public IntelligenceSkillPointsItemStack(ItemStack stack) {
        super(stack);
    }

    @Override
    public void init() {
        List<Component> newTooltip = new ArrayList<>(getOriginalTooltip());
        newTooltip.addAll(getTooltipExtension());
        tooltip = newTooltip;
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag isAdvanced) {
        if (tooltip == null) {
            return getOriginalTooltip();
        }

        return tooltip;
    }

    private List<Component> getTooltipExtension() {
        List<Component> tooltipExtension = new ArrayList<>();
        tooltipExtension.add(Component.literal(""));
        tooltipExtension.addAll(getManaTables());
        return tooltipExtension;
    }

    private List<Component> getManaTables() {
        SkillPointProperty property = getProperty(ItemProperty.SKILL_POINT);
        int intelligencePoints = property.getSkillPoints();

        int closestUpgradeLevel = Integer.MAX_VALUE;
        int level = Managers.Character.getXpLevel();

        LinkedList<Component> newLore = new LinkedList<>();

        for (int j = 0; j < 4; j++) {
            SpellType spell = SpellType.forClass(Managers.Character.getClassType(), j + 1);

            if (spell.getUnlockLevel(1) <= level) {
                // The spell has been unlocked
                int nextUpgrade = spell.getNextManaReduction(level, intelligencePoints);
                if (nextUpgrade < closestUpgradeLevel) {
                    closestUpgradeLevel = nextUpgrade;
                }
                int manaCost = spell.getManaCost(level, intelligencePoints);
                String spellName = spell.getName();
                String spellInfo = ChatFormatting.LIGHT_PURPLE + spellName + " Spell: " + ChatFormatting.AQUA + "-"
                        + manaCost + " ✺";
                if (nextUpgrade < Integer.MAX_VALUE) {
                    spellInfo += ChatFormatting.GRAY + " (-" + (manaCost - 1) + " ✺ in "
                            + remainingLevelsDescription(nextUpgrade - intelligencePoints) + ")";
                }
                newLore.add(Component.literal(spellInfo));
            }
        }

        if (closestUpgradeLevel < Integer.MAX_VALUE) {
            newLore.addFirst(Component.literal(ChatFormatting.GRAY + "Next upgrade: At " + ChatFormatting.WHITE
                    + closestUpgradeLevel + ChatFormatting.GRAY + " points (in "
                    + remainingLevelsDescription(closestUpgradeLevel - intelligencePoints) + ")"));
        }

        return newLore;
    }

    private String remainingLevelsDescription(int remainingLevels) {
        return "" + ChatFormatting.GOLD + remainingLevels + ChatFormatting.GRAY + " point"
                + (remainingLevels == 1 ? "" : "s");
    }
}
