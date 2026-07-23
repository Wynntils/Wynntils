/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.SavableTome;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.services.loadout.type.LoadoutType;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.ItemStack;

public class LoadoutMenuScrollListWidget extends ScrollListWidget implements ItemTooltipProvider {
    private static final int MAX_WIDGETS_PER_PAGE = 4;
    private static final int WIDTH = 271 - 10;
    private static final int HEIGHT = 136;
    private static final int WIDGET_HEIGHT = 30;
    private static final int WIDGET_HEIGHT_PADDING = 2;
    private static final int WIDGET_HEIGHT_EDGE_PADDING = 5;
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;
    private List<AbstractWidget> itemWidgets = new ArrayList<>();

    public LoadoutMenuScrollListWidget(int x, int y, BuildLoadoutsScreen parent) {
        super(
                x,
                y,
                WIDTH,
                HEIGHT,
                WIDGET_HEIGHT,
                WIDGET_HEIGHT_PADDING,
                WIDGET_HEIGHT_EDGE_PADDING,
                MAX_WIDGETS_PER_PAGE);
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND, x - 5, y - 5, this.width + 10, this.height + 10);

        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    protected List<AbstractWidget> getWidgets() {
        return itemWidgets;
    }

    @Override
    public void renderHoveredItemTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (AbstractWidget widget : getWidgets()) {
            if (widget instanceof ItemTooltipProvider itemTooltipProvider
                    && widget.isMouseOver(mouseX, mouseY + scrollOffset)) {
                itemTooltipProvider.renderHoveredItemTooltip(guiGraphics, mouseX, mouseY);
                return;
            }
        }
    }

    public void populateWidgets() {
        itemWidgets = new ArrayList<>();
        scrollOffset = 0;

        Loadout selectedLoadout = parent.getSelectedLoadout();
        if (selectedLoadout == null) return;

        if (selectedLoadout.type() == LoadoutType.ABILITY_TREE && selectedLoadout.hasAbilityTree()) {
            for (String abilityName : selectedLoadout.abilityTree().abilities()) {
                itemWidgets.add(new LoadoutMenuScrollListAbilityWidget(
                        StyledText.fromString(abilityName),
                        this.x + WIDGET_HEIGHT_EDGE_PADDING,
                        this.y
                                + WIDGET_HEIGHT_EDGE_PADDING
                                + itemWidgets.size() * (WIDGET_HEIGHT + WIDGET_HEIGHT_PADDING),
                        this.width - 20,
                        WIDGET_HEIGHT,
                        parent));
            }
        }

        if (selectedLoadout.type() == LoadoutType.ASPECT && selectedLoadout.hasAspects()) {
            for (String abilityName : selectedLoadout.aspects().aspectNames()) {
                itemWidgets.add(new LoadoutMenuScrollListAspectWidget(
                        StyledText.fromString(abilityName),
                        this.x + WIDGET_HEIGHT_EDGE_PADDING,
                        this.y
                                + WIDGET_HEIGHT_EDGE_PADDING
                                + itemWidgets.size() * (WIDGET_HEIGHT + WIDGET_HEIGHT_PADDING),
                        this.width - 20,
                        WIDGET_HEIGHT,
                        parent));
            }
        }

        if (selectedLoadout.type() == LoadoutType.SKILL_POINT && selectedLoadout.hasTomes()) {
            for (SavableTome tome : selectedLoadout.tomes().getAllTomes()) {
                ItemStack tomeStack = decodeTomeItemStack(tome).orElse(ItemStack.EMPTY);
                itemWidgets.add(new LoadoutMenuScrollListTomeWidget(
                        StyledText.fromString(tome.name()),
                        tomeStack,
                        this.x + WIDGET_HEIGHT_EDGE_PADDING,
                        this.y
                                + WIDGET_HEIGHT_EDGE_PADDING
                                + itemWidgets.size() * (WIDGET_HEIGHT + WIDGET_HEIGHT_PADDING),
                        this.width - 20,
                        WIDGET_HEIGHT,
                        parent));
            }
        }
    }

    private Optional<ItemStack> decodeTomeItemStack(SavableTome tome) {
        if (tome == null || tome.encoded() == null || tome.encoded().isEmpty()) return Optional.empty();

        Matcher matcher = Models.ItemEncoding.getEncodedDataPattern().matcher(tome.encoded());
        if (matcher.matches()) {
            EncodedByteBuffer encodedByteBuffer = EncodedByteBuffer.fromUtf16String(matcher.group("data"));
            String itemName = matcher.group("name");

            ErrorOr<WynnItem> errorOrItem = Models.ItemEncoding.decodeItem(encodedByteBuffer, itemName);
            if (errorOrItem.hasError()) {
                return Optional.empty();
            }

            if (errorOrItem.getValue() instanceof TomeItem tomeItem) {
                return Optional.of(new FakeItemStack(tomeItem, "From loadout"));
            }
        }

        return Optional.empty();
    }
}
