/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.abilities.widgets;

import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeInstance;
import com.wynntils.models.abilitytree.type.ArchetypeInfo;
import com.wynntils.screens.base.TooltipProvider;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AbilityArchetypeWidget extends AbstractWidget implements TooltipProvider {
    public static final int SIZE = 20;

    private final AbilityTreeInfo abilityTreeInfo;
    private final AbilityTreeInstance abilityTreeInstance;
    private final ArchetypeInfo archetypeInfo;

    public AbilityArchetypeWidget(
            int x,
            int y,
            int width,
            int height,
            Component message,
            AbilityTreeInfo abilityTreeInfo,
            AbilityTreeInstance abilityTreeInstance,
            ArchetypeInfo archetypeInfo) {
        super(x, y, width, height, message);
        this.abilityTreeInfo = abilityTreeInfo;
        this.abilityTreeInstance = abilityTreeInstance;
        this.archetypeInfo = archetypeInfo;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ItemStack itemStack =
                new ItemStack(Item.byId(archetypeInfo.itemInformation().itemId()));
        itemStack.setDamageValue(archetypeInfo.itemInformation().damage());

        guiGraphics.renderItem(itemStack, this.getX(), this.getY());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return archetypeInfo.getTooltip(abilityTreeInstance);
    }
}
