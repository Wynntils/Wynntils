/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.guides.aspect.AspectGuideContainerWidget;
import com.wynntils.screens.guides.augment.AugmentGuideContainerWidget;
import com.wynntils.screens.guides.charm.CharmGuideContainerWidget;
import com.wynntils.screens.guides.dungeonkey.DungeonKeyGuideContainerWidget;
import com.wynntils.screens.guides.emerald.EmeraldGuideContainerWidget;
import com.wynntils.screens.guides.gatheringtool.GatheringToolGuideContainerWidget;
import com.wynntils.screens.guides.gear.GearGuideContainerWidget;
import com.wynntils.screens.guides.ingredient.IngredientGuideContainerWidget;
import com.wynntils.screens.guides.material.MaterialGuideContainerWidget;
import com.wynntils.screens.guides.powder.PowderGuideContainerWidget;
import com.wynntils.screens.guides.rune.RuneGuideContainerWidget;
import com.wynntils.screens.guides.sets.SetsGuideContainerWidget;
import com.wynntils.screens.guides.tome.TomeGuideContainerWidget;
import com.wynntils.screens.guides.ward.WardGuideContainerWidget;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.GuideTypeButton;
import com.wynntils.screens.guides.widgets.GuideTypeScrollButton;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

public class WynntilsGuideScreen extends WynntilsScreen {
    private static final int BUTTON_SPACING = 22;

    private final Screen previousScreen;

    private GuideContainerWidget<?> guideContainerWidget;
    private List<WynntilsButton> guideTypeButtons;

    private GuideType selectedGuideType = GuideType.GEAR;
    private int guideTypeScrollOffset = 0;

    private WynntilsGuideScreen(Screen previousScreen) {
        super(Component.literal("Wynntils Guide Screen"));

        this.previousScreen = previousScreen;
    }

    public static Screen create(Screen previousScreen) {
        return new WynntilsGuideScreen(previousScreen);
    }

    @Override
    protected void doInit() {
        createTypeButtons();

        buildGuideContainerWidget(true);
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previousScreen);
    }

    @Override
    public void doRender(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (WynntilsButton guideTypeButton : guideTypeButtons) {
            guideTypeButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        guideContainerWidget.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        for (WynntilsButton button : guideTypeButtons) {
            if (button.isMouseOver(event.x(), event.y())) {
                return button.mouseClicked(event, isDoubleClick);
            }
        }

        if (guideContainerWidget.isMouseOver(event.x(), event.y())) {
            return guideContainerWidget.mouseClicked(event, isDoubleClick);
        }

        return super.doMouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (guideContainerWidget.isMouseOver(event.x(), event.y())) {
            return guideContainerWidget.mouseDragged(event, dragX, dragY);
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        guideContainerWidget.mouseReleased(event);

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (guideContainerWidget.isMouseOver(mouseX, mouseY)) {
            return guideContainerWidget.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        }

        scrollTypes((int) -deltaY);

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (guideContainerWidget.keyPressed(event)) return true;

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (guideContainerWidget.charTyped(event)) return true;

        return super.charTyped(event);
    }

    public void setGuideType(GuideType guideType) {
        this.selectedGuideType = guideType;
        guideContainerWidget = null;

        buildGuideContainerWidget(false);
    }

    public GuideType getGuideType() {
        return selectedGuideType;
    }

    public void scrollTypes(int direction) {
        int size = GuideType.values().length;
        guideTypeScrollOffset = Math.floorMod(guideTypeScrollOffset + direction, size);

        createTypeButtons();
    }

    private void createTypeButtons() {
        guideTypeButtons = new ArrayList<>();

        int buttonArea = this.height - 20;
        int maxButtons = (buttonArea / BUTTON_SPACING) - 1;
        boolean scrollNeeded = GuideType.values().length > maxButtons;

        List<GuideType> guideTypes;
        GuideType[] values = GuideType.values();

        if (scrollNeeded) {
            guideTypes = new ArrayList<>();

            for (int i = 0; i < maxButtons - 2; i++) {
                guideTypes.add(values[(guideTypeScrollOffset + i) % values.length]);
            }
        } else {
            guideTypes = Arrays.asList(values);
        }

        int visibleButtons = guideTypes.size() + (scrollNeeded ? 2 : 0);
        int renderY = (this.height + 20 - (visibleButtons * BUTTON_SPACING)) / 2;

        if (scrollNeeded) {
            ItemStack upItem = new ItemStack(Items.POTION);
            upItem.set(
                    DataComponents.CUSTOM_MODEL_DATA,
                    new CustomModelData(
                            List.of(Services.CustomModel.getFloat("default_arrow_up")
                                    .orElse(-1f)),
                            List.of(),
                            List.of(),
                            List.of()));
            guideTypeButtons.add(new GuideTypeScrollButton(8, renderY, upItem, true, this));

            renderY += BUTTON_SPACING;
        }

        for (GuideType guideType : guideTypes) {
            ItemStack itemStack = new ItemStack(guideType.getItem());

            if (!guideType.getModelDataKey().isEmpty()) {
                itemStack.set(
                        DataComponents.CUSTOM_MODEL_DATA,
                        new CustomModelData(
                                List.of(Services.CustomModel.getFloat(guideType.getModelDataKey())
                                        .orElse(-1f)),
                                List.of(),
                                List.of(),
                                List.of()));
            }

            guideTypeButtons.add(new GuideTypeButton(8, renderY, itemStack, guideType, this));

            renderY += BUTTON_SPACING;
        }

        if (scrollNeeded) {
            ItemStack downItem = new ItemStack(Items.POTION);
            downItem.set(
                    DataComponents.CUSTOM_MODEL_DATA,
                    new CustomModelData(
                            List.of(Services.CustomModel.getFloat("default_arrow_down")
                                    .orElse(-1f)),
                            List.of(),
                            List.of(),
                            List.of()));
            guideTypeButtons.add(new GuideTypeScrollButton(8, renderY, downItem, false, this));
        }
    }

    private void buildGuideContainerWidget(boolean preserveScroll) {
        int previousScroll =
                preserveScroll && guideContainerWidget != null ? guideContainerWidget.getScrollOffset() : 0;
        boolean filtersOpen = guideContainerWidget == null || guideContainerWidget.isDisplayingFilters();

        guideContainerWidget = selectedGuideType.buildScreen(30, 2, width - 32, height, previousScroll, filtersOpen);
    }

    public enum GuideType {
        GEAR(Items.LEATHER_HELMET, "helmet.pale_iron", GearGuideContainerWidget::new),
        INGREDIENTS("ingredient_pouch_full", IngredientGuideContainerWidget::new),
        TOMES("tome.armour", TomeGuideContainerWidget::new),
        CHARMS("charm.stone", CharmGuideContainerWidget::new),
        ASPECTS("abilityTree.aspectArcher", AspectGuideContainerWidget::new),
        MATERIALS("profession.ingotDernic", MaterialGuideContainerWidget::new),
        POWDER(Items.GREEN_DYE, PowderGuideContainerWidget::new),
        TOOLS("gatheringTool.pickaxe7", GatheringToolGuideContainerWidget::new),
        AUGMENTS("corkian_amplifier", AugmentGuideContainerWidget::new),
        WARDS("ward_pink", WardGuideContainerWidget::new),
        RUNE("rune_az", RuneGuideContainerWidget::new),
        DUNGEON_KEY("dungeon_key", DungeonKeyGuideContainerWidget::new),
        EMERALD(Items.EMERALD, EmeraldGuideContainerWidget::new),
        SETS(Items.LEATHER_CHESTPLATE, "chestplate.pale_leather", SetsGuideContainerWidget::new);

        private final Item item;
        private final String modelDataKey;
        private final GuideContainerFactory factory;

        GuideType(Item item, String modelDataKey, GuideContainerFactory factory) {
            this.item = item;
            this.modelDataKey = modelDataKey;
            this.factory = factory;
        }

        GuideType(String modelDataKey, GuideContainerFactory factory) {
            this(Items.POTION, modelDataKey, factory);
        }

        GuideType(Item item, GuideContainerFactory factory) {
            this(item, "", factory);
        }

        public GuideContainerWidget<?> buildScreen(
                int x, int y, int width, int height, int scrollOffset, boolean filtersOpen) {
            return factory.create(x, y, width, height, scrollOffset, filtersOpen);
        }

        public Item getItem() {
            return item;
        }

        public String getModelDataKey() {
            return modelDataKey;
        }
    }

    @FunctionalInterface
    public interface GuideContainerFactory {
        GuideContainerWidget<?> create(int x, int y, int width, int height, int scrollOffset, boolean filtersOpen);
    }
}
