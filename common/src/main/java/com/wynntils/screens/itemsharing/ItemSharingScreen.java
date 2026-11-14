/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemsharing;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.TooltipUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

public final class ItemSharingScreen extends WynntilsScreen {
    private ItemStack itemStack;
    private final WynnItem wynnItem;
    private final Screen previousScreen;

    private boolean savedItem = false;
    private Button saveButton;
    private EncodedByteBuffer encodedItem;
    private int backgroundX;
    private int backgroundY;
    private int tooltipX;
    private int tooltipWidth;
    private boolean previewWidthMeasured;
    private Component parsingMessage;
    private ItemStack previewItemStack;
    private List<AbstractWidget> options = new ArrayList<>();

    private ItemSharingScreen(WynnItem wynnItem, ItemStack itemStack) {
        super(Component.literal("Item Sharing Screen"));

        this.wynnItem = wynnItem;
        this.itemStack = itemStack;
        this.previousScreen = McUtils.screen();
    }

    private ItemSharingScreen(WynnItem wynnItem, ItemStack itemStack, boolean savedItem) {
        super(Component.literal("Item Sharing Screen"));

        this.wynnItem = wynnItem;
        this.itemStack = itemStack;
        this.savedItem = savedItem;
        this.previousScreen = McUtils.screen();
    }

    // Creating screen from an item
    public static Screen create(WynnItem wynnItem, ItemStack itemStack) {
        return new ItemSharingScreen(wynnItem, itemStack);
    }

    // Creating screen from item record
    public static Screen create(WynnItem wynnItem, ItemStack itemStack, boolean savedItem) {
        return new ItemSharingScreen(wynnItem, itemStack, savedItem);
    }

    // Creating screen from an item that needs to have its pages parsed
    public static ItemSharingScreen createParsing(WynnItem wynnItem, ItemStack itemStack, int page) {
        ItemSharingScreen screen = new ItemSharingScreen(wynnItem, itemStack);
        screen.setParsingPage(page);
        return screen;
    }

    public void setParsingPage(int page) {
        parsingMessage = Component.translatable("screens.wynntils.itemSharing.parsingPage", page + 1, 3)
                .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT));
    }

    public void parsingFailed() {
        parsingMessage = Component.translatable("screens.wynntils.itemSharing.parsingFailed")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.RED));
    }

    public void completeParsing(ItemStack itemStack) {
        this.itemStack = itemStack;
        parsingMessage = null;
        refreshPreview();
    }

    @Override
    protected void doInit() {
        super.doInit();
        backgroundY = (this.height - Texture.ITEM_SHARING_BACKGROUND.height()) / 2;
        refreshPreview();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.itemSharing.sharingOptions")
                                .withStyle(ChatFormatting.BOLD)),
                        backgroundX + 10,
                        backgroundY + 10,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);

        renderPreview(guiGraphics);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtils.drawTexturedRect(guiGraphics, Texture.ITEM_SHARING_BACKGROUND, backgroundX, backgroundY);
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previousScreen);
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 0
                && !isInsideOptionsPanel(event.x(), event.y())
                && previewItemStack instanceof FakeItemStack fakeItemStack
                && fakeItemStack.cyclePage()) {
            return true;
        }
        return super.doMouseClicked(event, isDoubleClick);
    }

    private boolean isInsideOptionsPanel(double x, double y) {
        return x >= backgroundX
                && x < backgroundX + Texture.ITEM_SHARING_BACKGROUND.width()
                && y >= backgroundY
                && y < backgroundY + Texture.ITEM_SHARING_BACKGROUND.height();
    }

    private void renderPreview(GuiGraphics guiGraphics) {
        int centerY = backgroundY + Texture.ITEM_SHARING_BACKGROUND.height() / 2;
        if (parsingMessage != null) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(parsingMessage),
                            tooltipX + tooltipWidth / 2f,
                            centerY,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            2f);
            return;
        }
        if (previewItemStack == null) return;

        guiGraphics.renderTooltip(
                McUtils.mc().font,
                TooltipUtils.getClientTooltipComponent(LoreUtils.getTooltipLines(previewItemStack)),
                tooltipX,
                centerY,
                (screenWidth, screenHeight, x, y, actualWidth, actualHeight) -> new Vector2i(
                        Math.max(4, Math.min(x, screenWidth - actualWidth - 4)),
                        Math.max(4, Math.min(y - actualHeight / 2, screenHeight - actualHeight - 4))),
                previewItemStack.get(DataComponents.TOOLTIP_STYLE));
    }

    private void refreshPreview() {
        if (parsingMessage != null) {
            previewItemStack = null;
            tooltipWidth = 200;
            updateLayout();
            return;
        }
        // Encode the item with the selected settings
        EncodingSettings encodingSettings = new EncodingSettings(
                Models.ItemEncoding.extendedIdentificationEncoding.get(), Models.ItemEncoding.shareItemName.get());
        ErrorOr<EncodedByteBuffer> errorOrEncodedByteBuffer =
                Models.ItemEncoding.encodeItem(wynnItem, encodingSettings);
        if (errorOrEncodedByteBuffer.hasError()) {
            WynntilsMod.error("Failed to encode item: " + errorOrEncodedByteBuffer.getError());
            previewItemStack = null;
            backgroundX = (width - Texture.ITEM_SHARING_BACKGROUND.width()) / 2;
            clearWidgets();
            addError(errorOrEncodedByteBuffer.getError());
            return;
        }

        encodedItem = errorOrEncodedByteBuffer.getValue();

        // Decode the item to be displayed
        String itemName = wynnItem instanceof NamedItemProperty namedItem ? namedItem.getName() : null;
        ErrorOr<WynnItem> errorOrDecodedByteBuffer = Models.ItemEncoding.decodeItem(encodedItem, itemName);
        if (errorOrDecodedByteBuffer.hasError()) {
            WynntilsMod.error("Failed to decode item: " + errorOrDecodedByteBuffer.getError());
            previewItemStack = null;
            backgroundX = (width - Texture.ITEM_SHARING_BACKGROUND.width()) / 2;
            clearWidgets();
            addError(errorOrDecodedByteBuffer.getError());
            return;
        }

        WynnItem renderedItem = errorOrDecodedByteBuffer.getValue();

        previewItemStack = new FakeItemStack(renderedItem, itemStack, ItemSourceType.ENCODED, "From chat");

        updatePreviewLayout();
    }

    private void updatePreviewLayout() {
        // The preview starts on page zero. Keep its width for the lifetime of this screen
        // so narrower pages don't move the options panel.
        if (!previewWidthMeasured) {
            List<Component> tooltipLines = LoreUtils.getTooltipLines(previewItemStack);
            tooltipWidth = tooltipLines.stream()
                    .map(component -> McUtils.mc().font.width(component))
                    .max(Integer::compareTo)
                    .orElse(0);
            previewWidthMeasured = true;
        }

        updateLayout();
    }

    private void updateLayout() {
        // Total area the tooltip + sharing options panel + gap should cover
        int totalRenderWidth = tooltipWidth + Texture.ITEM_SHARING_BACKGROUND.width() + 20;

        // Blank space to the side of each element
        backgroundX = Math.max(0, (this.width - totalRenderWidth) / 2);
        tooltipX = backgroundX + Texture.ITEM_SHARING_BACKGROUND.width() + 20;

        // Widgets need to be remade in case backgroundX has changed and the options will be misaligned
        for (AbstractWidget widget : options) {
            this.removeWidget(widget);
        }

        options = new ArrayList<>();

        addSharingOptions();
        for (AbstractWidget option : options) {
            option.active = parsingMessage == null;
        }
    }

    private void shareItem(String target) {
        if (parsingMessage != null || encodedItem == null) return;
        switch (target) {
            case "guild" ->
                Handlers.Chat.queueChatCommand("g " + Models.ItemEncoding.makeItemString(wynnItem, encodedItem));
            case "party" ->
                Handlers.Chat.queueChatCommand("p " + Models.ItemEncoding.makeItemString(wynnItem, encodedItem));
            case "save" -> {
                ItemStack itemStackToSave = itemStack;

                // Gear items can have their item changed by cosmetics so we need to get their original item
                // FIXME: Does not work for crafted gear
                if (wynnItem instanceof GearItem gearItem) {
                    itemStackToSave = new FakeItemStack(
                            gearItem, ItemSourceType.ENCODED, "From " + McUtils.playerName() + "'s Item Record");
                }

                // Item name is passed in since it is lost in the instanceof check above and looks nicer
                // saying "Saved Gale's Force to your item record" than "Saved Bow to your item record"
                savedItem = Services.ItemRecord.saveItem(wynnItem, itemStackToSave, itemStack.getHoverName());

                if (savedItem) {
                    saveButton.setMessage(Component.translatable("screens.wynntils.itemSharing.openRecord"));
                }
            }
            default -> {
                McUtils.mc().keyboardHandler.setClipboard(Models.ItemEncoding.makeItemString(wynnItem, encodedItem));

                McUtils.sendWynntilsPrefixMessage(Component.translatable("screens.wynntils.itemSharing.copied")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
    }

    private void addSharingOptions() {
        // region Checkbox options
        if (wynnItem instanceof IdentifiableItemProperty<?, ?>) {
            options.add(this.addRenderableWidget(new WynntilsCheckbox(
                    backgroundX + 15,
                    backgroundY + 25,
                    10,
                    Component.translatable("screens.wynntils.itemSharing.extended.name"),
                    Models.ItemEncoding.extendedIdentificationEncoding.get(),
                    Texture.ITEM_SHARING_BACKGROUND.width() - 30,
                    (c, b) -> {
                        Models.ItemEncoding.extendedIdentificationEncoding.store(b);
                        refreshPreview();
                    },
                    ComponentUtils.wrapTooltips(
                            List.of(
                                    Component.translatable("screens.wynntils.itemSharing.extended.description1"),
                                    Component.translatable("screens.wynntils.itemSharing.extended.description2")),
                            150))));
        } else if (wynnItem instanceof CraftedItemProperty || wynnItem instanceof MountItem) {
            options.add(this.addRenderableWidget(new WynntilsCheckbox(
                    backgroundX + 15,
                    backgroundY + 25,
                    10,
                    Component.translatable("screens.wynntils.itemSharing.itemName.name"),
                    Models.ItemEncoding.shareItemName.get(),
                    Texture.ITEM_SHARING_BACKGROUND.width() - 30,
                    (c, b) -> {
                        Models.ItemEncoding.shareItemName.store(b);
                        refreshPreview();
                    },
                    ComponentUtils.wrapTooltips(
                            List.of(Component.translatable("screens.wynntils.itemSharing.itemName.description")),
                            150))));
        }
        // endregion

        // region Share buttons
        int shareButtonWidth = (Texture.ITEM_SHARING_BACKGROUND.width() - 20) / 2 - 5;

        options.add(this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.itemSharing.shareParty"), (b) -> shareItem("party"))
                .pos(backgroundX + 10, backgroundY + 45)
                .size(shareButtonWidth, 20)
                .build()));

        options.add(this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.itemSharing.shareGuild"), (b) -> shareItem("guild"))
                .pos(backgroundX + 20 + shareButtonWidth, backgroundY + 45)
                .size(shareButtonWidth, 20)
                .build()));

        // If an item has already been saved then this button will act as easy access to their saved items
        Component saveButtonMessage = savedItem
                ? Component.translatable("screens.wynntils.itemSharing.openRecord")
                : Component.translatable("screens.wynntils.itemSharing.save");

        saveButton = new Button.Builder(saveButtonMessage, (b) -> {
                    if (!savedItem) {
                        shareItem("save");
                    } else {
                        McUtils.setScreen(SavedItemsScreen.create());
                    }
                })
                .pos(backgroundX + 10, backgroundY + Texture.ITEM_SHARING_BACKGROUND.height() - 30)
                .size(shareButtonWidth, 20)
                .build();

        this.addRenderableWidget(saveButton);

        options.add(saveButton);

        options.add(this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.itemSharing.copy"), (b) -> shareItem("clipboard"))
                .pos(backgroundX + 20 + shareButtonWidth, backgroundY + Texture.ITEM_SHARING_BACKGROUND.height() - 30)
                .size(shareButtonWidth, 20)
                .build()));
        // endregion
    }

    private void addError(String error) {
        this.addRenderableWidget(
                new Button.Builder(Component.translatable("screens.wynntils.itemSharing.error"), (b) -> {})
                        .pos(backgroundX + 10, backgroundY + Texture.ITEM_SHARING_BACKGROUND.height() - 30)
                        .size(Texture.ITEM_SHARING_BACKGROUND.width() - 20, 20)
                        .tooltip(Tooltip.create(Component.literal(error)))
                        .build());
    }
}
