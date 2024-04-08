/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.abilities.widgets;

import com.wynntils.models.abilitytree.type.AbilityTreeNodeState;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.abilitytree.type.ParsedAbilityTree;
import com.wynntils.screens.base.TooltipProvider;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AbilityNodeWidget extends AbstractWidget implements TooltipProvider {
    public static final int SIZE = 20;

    private final ParsedAbilityTree currentAbilityTree;
    private final AbilityTreeSkillNode node;

    public AbilityNodeWidget(
            int x, int y, int width, int height, ParsedAbilityTree currentAbilityTree, AbilityTreeSkillNode node) {
        super(x, y, width, height, Component.literal(node.name()));
        this.currentAbilityTree = currentAbilityTree;
        this.node = node;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ItemStack itemStack = new ItemStack(Item.byId(node.itemInformation().itemId()));

        AbilityTreeNodeState nodeState = currentAbilityTree.getNodeState(node);
        int damage =
                switch (nodeState) {
                    case UNREACHABLE, REQUIREMENT_NOT_MET -> node.itemInformation()
                            .getLockedDamage();
                    case UNLOCKABLE -> node.itemInformation().getUnlockableDamage();
                    case UNLOCKED -> node.itemInformation().getUnlockedDamage();
                    case BLOCKED -> node.itemInformation().getBlockedDamage();
                };

        itemStack.setDamageValue(damage);
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);

        guiGraphics.renderItem(itemStack, this.getX(), this.getY());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    public AbilityTreeSkillNode getNode() {
        return node;
    }

    @Override
    public List<Component> getTooltipLines() {
        return node.getDescription(currentAbilityTree.getNodeState(node), currentAbilityTree);
    }
}
