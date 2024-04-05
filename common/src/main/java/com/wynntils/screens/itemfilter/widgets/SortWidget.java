/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class SortWidget extends AbstractWidget {
    private final float translationX;
    private final float translationY;
    private final ItemFilterScreen filterScreen;
    private final ItemStatProvider<?> provider;
    private final List<Button> buttons = new ArrayList<>();

    private boolean descending;

    public SortWidget(
            int x,
            int y,
            ItemStatProvider<?> provider,
            boolean descending,
            ItemFilterScreen filterScreen,
            float translationX,
            float translationY) {
        super(x, y, 150, 20, Component.literal("Sort Widget"));

        this.filterScreen = filterScreen;
        this.provider = provider;
        this.descending = descending;
        this.translationX = translationX;
        this.translationY = translationY;

        Button sortButton = new Button.Builder(
                        Component.literal(descending ? "v" : "ʌ"), (button) -> toggleSortDirection())
                .pos(x + width - 70, y)
                .size(50, 20)
                .build();

        Button upButton = new Button.Builder(
                        Component.literal("🠝"), (button) -> filterScreen.reorderSort(provider, -1))
                .pos(x + width - 20, y)
                .size(10, 20)
                .build();

        Button downButton = new Button.Builder(
                        Component.literal("🠟"), (button) -> filterScreen.reorderSort(provider, 1))
                .pos(x + width - 10, y)
                .size(10, 20)
                .build();

        List<Pair<ItemStatProvider<?>, String>> sorts = filterScreen.getSorts();

        // Disable the up/down button if the current provider is at the start/end of the sort
        for (int i = 0; i < sorts.size(); i++) {
            if (sorts.get(i).a() == provider) {
                upButton.active = i != 0;
                downButton.active = i != sorts.size() - 1;
                break;
            }
        }

        buttons.add(sortButton);
        buttons.add(upButton);
        buttons.add(downButton);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawRectBorders(
                poseStack,
                descending ? CommonColors.WHITE : CommonColors.BLACK,
                getX(),
                getY(),
                getX() + width,
                getY() + height,
                1,
                1);

        FontRenderer.getInstance()
                .renderScrollingString(
                        poseStack,
                        StyledText.fromString(provider.getDisplayName()),
                        getX() + 2,
                        getY() + (height / 2f),
                        width - 70,
                        translationX,
                        translationY,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1.0f);

        for (Button button : buttons) {
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener listener : buttons) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return listener.mouseClicked(mouseX, mouseY, button);
            }
        }

        return false;
    }

    private void toggleSortDirection() {
        descending = !descending;

        filterScreen.changeSortOrder(provider, descending);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
