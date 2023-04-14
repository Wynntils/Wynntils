/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.abilities.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.screens.abilities.CustomAbilityTreeScreen;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AbilityNodeWidget extends AbstractWidget {
    public static final int SIZE = 20;

    private final CustomAbilityTreeScreen customAbilityTreeScreen;
    private final AbilityTreeSkillNode node;

    public AbilityNodeWidget(
            int x,
            int y,
            int width,
            int height,
            CustomAbilityTreeScreen customAbilityTreeScreen,
            AbilityTreeSkillNode node) {
        super(x, y, width, height, Component.literal(node.name()));
        this.node = node;
        this.customAbilityTreeScreen = customAbilityTreeScreen;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ItemStack itemStack = new ItemStack(Item.byId(node.itemInformation().itemId()));

        int damage =
                switch (customAbilityTreeScreen.getNodeState(node)) {
                    case LOCKED -> node.itemInformation().getLockedDamage();
                    case UNLOCKABLE -> node.itemInformation().getUnlockableDamage();
                    case UNLOCKED -> node.itemInformation().getUnlockedDamage();
                    case BLOCKED -> node.itemInformation().getBlockedDamage();
                };

        itemStack.setDamageValue(damage);
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);

        RenderUtils.renderItem(poseStack, itemStack, this.getX(), this.getY(), 1f);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
