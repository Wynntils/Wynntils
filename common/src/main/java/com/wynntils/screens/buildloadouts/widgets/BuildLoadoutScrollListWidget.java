package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.SavableTome;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.ScrollListCategory;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.ErrorOr;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

public class BuildLoadoutScrollListWidget extends ScrollListWidget implements ItemTooltipProvider {
    private static final int  MAX_WIDGETS_PER_PAGE = 3;
    private static final int WIDTH = 271 - 10;
    private static final int HEIGHT = 131 - 16;
    private static final int WIDGET_HEIGHT = 33;
    private static final int WIDGET_HEIGHT_PADDING = 2;
    private static final int WIDGET_HEIGHT_EDGE_PADDING = 5;
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;
    private final BuildLoadoutScrollListSelectorWidget abilityTreeButton;
    private final BuildLoadoutScrollListSelectorWidget aspectsButton;
    private final BuildLoadoutScrollListSelectorWidget tomesButton;
    private ScrollListCategory selectedScrollListCategory = ScrollListCategory.ABILITY_TREE;
    private List<AbstractWidget> itemWidgets = new ArrayList<>();

    public BuildLoadoutScrollListWidget(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, WIDTH, HEIGHT, WIDGET_HEIGHT, WIDGET_HEIGHT_PADDING, WIDGET_HEIGHT_EDGE_PADDING,  MAX_WIDGETS_PER_PAGE);
        this.x = x;
        this.y = y;
        this.parent = parent;

        int buttonWidth = (this.width - 8) / 3;
        abilityTreeButton = new BuildLoadoutScrollListSelectorWidget(
                StyledText.fromComponent(Component.translatable("screens.wynntils.buildLoadouts.buildLoadoutScrollList.selectButton.abilityTree")),
                ScrollListCategory.ABILITY_TREE,
                x + 4,
                y - 2 - 15,
                buttonWidth + 1,
                18,
                parent
        );

        aspectsButton = new BuildLoadoutScrollListSelectorWidget(
                StyledText.fromComponent(Component.translatable("screens.wynntils.buildLoadouts.buildLoadoutScrollList.selectButton.aspects")),
                ScrollListCategory.ASPECTS,
                x + 4 + buttonWidth,
                y - 2 - 15,
                buttonWidth + 1,
                18,
                parent
        );

        tomesButton = new BuildLoadoutScrollListSelectorWidget(
                StyledText.fromComponent(Component.translatable("screens.wynntils.buildLoadouts.buildLoadoutScrollList.selectButton.tomes")),
                ScrollListCategory.TOMES,
                x + 4 + buttonWidth * 2,
                y - 2 - 15,
                buttonWidth + 1,
                18,
                parent
        );
    }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND,
                x - 5,
                y - 5 - 15,
                this.width + 10,
                this.height + 10 + 1 + 15);

        abilityTreeButton.render(guiGraphics, mouseX, mouseY, partialTick);
        aspectsButton.render(guiGraphics, mouseX, mouseY, partialTick);
        tomesButton.render(guiGraphics, mouseX, mouseY, partialTick);



        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (abilityTreeButton.isMouseOver(event.x(), event.y())) {
            return abilityTreeButton.mouseClicked(event, isDoubleClick);
        }
        if (aspectsButton.isMouseOver(event.x(), event.y())) {
            return aspectsButton.mouseClicked(event, isDoubleClick);
        }
        if (tomesButton.isMouseOver(event.x(), event.y())) {
            return tomesButton.mouseClicked(event, isDoubleClick);
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY)
                || abilityTreeButton.isMouseOver(mouseX, mouseY)
                || aspectsButton.isMouseOver(mouseX, mouseY)
                || tomesButton.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void renderHoveredItemTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (AbstractWidget widget : getWidgets()) {
            if (widget instanceof ItemTooltipProvider itemTooltipProvider && widget.isMouseOver(mouseX, mouseY + scrollOffset)) {
                itemTooltipProvider.renderHoveredItemTooltip(guiGraphics, mouseX, mouseY);
                return;
            }
        }
    }

    public void setSelectedScrollListCategory(ScrollListCategory category) {
        selectedScrollListCategory = category;
        populateWidgets();
    }

    public ScrollListCategory getSelectedScrollListCategory() {
        return selectedScrollListCategory;
    }

    @Override
    protected List<AbstractWidget> getWidgets() {
        return itemWidgets;
    }

    public void populateWidgets() {
        itemWidgets = new ArrayList<>();
        scrollOffset = 0;

        Loadout selectedLoadout = parent.getSelectedLoadout();
        if (selectedLoadout == null) return;

        if (selectedScrollListCategory == ScrollListCategory.ABILITY_TREE && selectedLoadout.hasAbilityTree()) {
            for (String abilityName : selectedLoadout.abilityTree().abilities()) {
                itemWidgets.add(new LoadoutMenuScrollListAbilityWidget(
                        StyledText.fromString(abilityName),
                        this.x + WIDGET_HEIGHT_EDGE_PADDING,
                        this.y + WIDGET_HEIGHT_EDGE_PADDING + itemWidgets.size() * (WIDGET_HEIGHT + WIDGET_HEIGHT_PADDING),
                        this.width - 20,
                        WIDGET_HEIGHT,
                        parent));
            }
        }

        if (selectedScrollListCategory == ScrollListCategory.ASPECTS && selectedLoadout.hasAspects()) {
            for (String abilityName : selectedLoadout.aspects().aspectNames()) {
                itemWidgets.add(new LoadoutMenuScrollListAspectWidget(
                        StyledText.fromString(abilityName),
                        this.x + WIDGET_HEIGHT_EDGE_PADDING,
                        this.y + WIDGET_HEIGHT_EDGE_PADDING + itemWidgets.size() * (WIDGET_HEIGHT + WIDGET_HEIGHT_PADDING),
                        this.width - 20,
                        WIDGET_HEIGHT,
                        parent));
            }
        }

        if (selectedScrollListCategory == ScrollListCategory.TOMES && selectedLoadout.hasTomes()) {
            for (SavableTome tome : selectedLoadout.tomes().getAllTomes()) {
                ItemStack tomeStack = decodeTomeItemStack(tome).orElse(ItemStack.EMPTY);
                itemWidgets.add(new LoadoutMenuScrollListTomeWidget(
                        StyledText.fromString(tome.name()),
                        tomeStack,
                        this.x + WIDGET_HEIGHT_EDGE_PADDING,
                        this.y + WIDGET_HEIGHT_EDGE_PADDING + itemWidgets.size() * (WIDGET_HEIGHT + WIDGET_HEIGHT_PADDING),
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
