/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemsharing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.type.SavedItemStack;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.WynntilsContainerScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.itemsharing.widgets.SavedCategoryButton;
import com.wynntils.screens.itemsharing.widgets.SavedItemsButton;
import com.wynntils.screens.itemsharing.widgets.SavedItemsHelpButton;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.ErrorOr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class SavedItemsScreen extends WynntilsContainerScreen<SavedItemsMenu> {
    private static final int ITEMS_PER_ROW = 7;
    private static final int MAX_ITEMS = 49;
    private static final int SCROLL_AREA_HEIGHT = 128;

    private boolean addingCategory = false;
    private boolean draggingScroll = false;
    private int itemScrollOffset = 0;
    private List<String> encodedItems = new ArrayList<>();
    private Slot selectedItemSlot;
    private String currentCategory = Services.ItemVault.getDefaultCategory();
    private String originalCategory;
    private String selectedItem;
    private TextInputBoxWidget categoryInput;

    private SavedItemsScreen(SavedItemsMenu abstractContainerMenu) {
        super(abstractContainerMenu, McUtils.inventory(), Component.literal("Saved Items Screen"));
    }

    public static Screen create() {
        return new SavedItemsScreen(SavedItemsMenu.create());
    }

    @Override
    protected void doInit() {
        super.doInit();
        this.leftPos = (this.width - Texture.VAULT.width()) / 2;
        this.topPos = (this.height - Texture.VAULT.height()) / 2;

        // region Side buttons
        this.addRenderableWidget(new SavedItemsButton(
                this.leftPos + 8,
                this.topPos + 22,
                (b) -> {
                    if (b == 0) {
                        // Deselect an item if any was selected
                        originalCategory = "";
                        selectedItem = "";
                        selectedItemSlot = null;

                        addCategory();
                    }
                },
                List.of(
                        Component.translatable("screens.wynntils.savedItems.addTooltip1"),
                        Component.translatable("screens.wynntils.savedItems.addTooltip2")),
                Texture.VAULT_ADD));

        this.addRenderableWidget(new SavedItemsButton(
                this.leftPos + 8,
                this.topPos + 36,
                (b) -> {
                    // Deselect an item if any was selected
                    originalCategory = "";
                    selectedItem = "";
                    selectedItemSlot = null;

                    deleteCategory();
                },
                List.of(
                        Component.translatable("screens.wynntils.savedItems.deleteTooltip1"),
                        Component.translatable("screens.wynntils.savedItems.deleteTooltip2")),
                Texture.VAULT_DELETE));

        this.addRenderableWidget(new SavedItemsButton(
                this.leftPos + 8,
                this.topPos + 50,
                (b) -> {
                    if (selectedItemSlot == null) return;

                    if (b == 0) {
                        moveItemCategory();
                    } else if (b == 1) {
                        // Deselect an item if any was selected
                        originalCategory = "";
                        selectedItem = "";
                        selectedItemSlot = null;
                    }
                },
                List.of(
                        Component.translatable("screens.wynntils.savedItems.changeCategoryTooltip1"),
                        Component.translatable("screens.wynntils.savedItems.changeCategoryTooltip2")),
                Texture.VAULT_CONFIRM));

        this.addRenderableWidget(new SavedItemsHelpButton(
                this.leftPos + 8,
                this.topPos + 140,
                List.of(
                        Component.translatable("screens.wynntils.savedItems.help")
                                .withStyle(ChatFormatting.UNDERLINE),
                        Component.translatable("screens.wynntils.savedItems.help1"),
                        Component.translatable("screens.wynntils.savedItems.help2"),
                        Component.translatable("screens.wynntils.savedItems.help3"))));
        // endregion

        // region Category navigation buttons
        this.addRenderableWidget(new SavedCategoryButton(this.leftPos + 19, this.topPos + 9, this, false));

        this.addRenderableWidget(new SavedCategoryButton(this.leftPos + 139, this.topPos + 9, this, true));
        // endregion

        // Re add input box if screen resized during input
        if (addingCategory) {
            addCategoryInput();
        }

        populateItems();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawTexturedRect(poseStack, Texture.VAULT, this.leftPos, this.topPos);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // We don't want a label
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slot == null) return;
        if (slot.getItem() == ItemStack.EMPTY) return;

        // Left click goes to sharing menu
        // Left+Shift deletes the item from storage
        // Right click selects it for category change
        if (mouseButton == 0) {
            if (KeyboardUtils.isShiftDown()) {
                deleteItem(encodedItems.get(slot.index));
            } else {
                Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(slot.getItem());

                if (wynnItemOpt.isPresent()) {
                    McUtils.mc().setScreen(ItemSharingScreen.create(wynnItemOpt.get(), slot.getItem(), true));
                } else {
                    McUtils.sendMessageToClient(Component.translatable("screens.wynntils.savedItems.unableToShare"));
                }
            }
        } else if (mouseButton == 1) {
            originalCategory = currentCategory;
            selectedItem = encodedItems.get(slot.index);
            selectedItemSlot = slot;
        }
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        PoseStack poseStack = guiGraphics.pose();

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        if (!addingCategory) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromString(currentCategory),
                            this.leftPos + 36,
                            this.leftPos + 36 + 99,
                            this.topPos + 10,
                            this.topPos + 10 + 8,
                            99,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NONE);
        }

        // Draw a box around the selected item to let the user know which item they are moving
        if (selectedItemSlot != null && currentCategory.equals(originalCategory)) {
            RenderUtils.drawRectBorders(
                    poseStack,
                    CommonColors.WHITE,
                    this.leftPos + selectedItemSlot.x,
                    this.topPos + selectedItemSlot.y,
                    this.leftPos + selectedItemSlot.x + 16,
                    this.topPos + selectedItemSlot.y + 16,
                    0,
                    1);
        }

        renderScrollButton(poseStack);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float scrollButtonRenderX = this.leftPos + 152;
        float scrollButtonRenderY =
                this.topPos + 18 + MathUtils.map(itemScrollOffset, 0, getMaxScrollOffset(), 0, SCROLL_AREA_HEIGHT);

        if (MathUtils.isInside(
                (int) mouseX,
                (int) mouseY,
                (int) scrollButtonRenderX,
                (int) (scrollButtonRenderX + Texture.VAULT_SCROLL.width()),
                (int) scrollButtonRenderY,
                (int) (scrollButtonRenderY + Texture.VAULT_SCROLL.height()))) {
            draggingScroll = true;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingScroll) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        int renderY = this.topPos + 18;
        int newValue = Math.round(
                MathUtils.map((float) mouseY, renderY, renderY + SCROLL_AREA_HEIGHT, 0, getMaxScrollOffset()));

        scrollItems(newValue - itemScrollOffset);

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        double scrollValue = -Math.signum(deltaY);

        if (mouseY < this.topPos + 18) {
            scrollCategories((int) scrollValue);
        } else {
            scrollItems((int) scrollValue);
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Enter can also be used to submit name for new category title
        if (addingCategory && keyCode == GLFW.GLFW_KEY_ENTER) {
            addCategory();
            return false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void scrollCategories(int scrollDirection) {
        List<String> categories =
                new ArrayList<>(Services.ItemVault.savedItems.get().keySet());

        int currentIndex = categories.indexOf(currentCategory);
        int newIndex = currentIndex + scrollDirection;

        // If scrolling past one end of the set, go to the other end
        if (newIndex >= categories.size()) {
            currentCategory = categories.get(0);
        } else if (newIndex < 0) {
            currentCategory = categories.get(categories.size() - 1);
        } else {
            currentCategory = categories.get(newIndex);
        }

        itemScrollOffset = 0;
        populateItems();
    }

    private void renderScrollButton(PoseStack poseStack) {
        float renderX = this.leftPos + 153;
        float renderY =
                this.topPos + 18 + MathUtils.map(itemScrollOffset, 0, getMaxScrollOffset(), 0, SCROLL_AREA_HEIGHT);

        RenderUtils.drawTexturedRect(poseStack, Texture.VAULT_SCROLL, renderX, renderY);
    }

    private void addCategoryInput() {
        // Remove previous widget if it exists
        if (categoryInput != null) {
            this.removeWidget(categoryInput);
        }

        categoryInput = new TextInputBoxWidget(
                this.leftPos + 36, this.topPos + 5, 99, 16, null, (TextboxScreen) McUtils.mc().screen, categoryInput);

        this.addRenderableWidget(categoryInput);
    }

    private void addCategory() {
        if (addingCategory) {
            if (!categoryInput.getTextBoxInput().isEmpty()) {
                // Save new category
                Services.ItemVault.savedItems.get().put(categoryInput.getTextBoxInput(), new TreeMap<>());
                Services.ItemVault.savedItems.touched();
            }

            // Remove the input widget
            this.removeWidget(categoryInput);
            categoryInput = null;
        } else {
            addCategoryInput();
        }

        // Toggle state
        addingCategory = !addingCategory;
    }

    private void deleteCategory() {
        if (KeyboardUtils.isShiftDown()) {
            // Delete all items from category
            Services.ItemVault.savedItems.get().remove(currentCategory);
            Services.ItemVault.savedItems.touched();
        } else if (!currentCategory.equals(Services.ItemVault.getDefaultCategory())) {
            // Move all items to "Uncategorized" category, then delete category
            Map<String, SavedItemStack> uncategorisedItems =
                    Services.ItemVault.savedItems.get().get(Services.ItemVault.getDefaultCategory());
            Map<String, SavedItemStack> currentCategoryItems =
                    Services.ItemVault.savedItems.get().getOrDefault(currentCategory, new HashMap<>());

            uncategorisedItems.putAll(currentCategoryItems);

            Services.ItemVault.savedItems.get().put(Services.ItemVault.getDefaultCategory(), uncategorisedItems);
            Services.ItemVault.savedItems.get().remove(currentCategory);
            Services.ItemVault.savedItems.touched();
        }

        // Reset to default category
        currentCategory = Services.ItemVault.getDefaultCategory();

        populateItems();
    }

    private void moveItemCategory() {
        Map<String, Map<String, SavedItemStack>> savedItems = Services.ItemVault.savedItems.get();
        Map<String, SavedItemStack> originalCategoryItems = savedItems.get(originalCategory);
        Map<String, SavedItemStack> newCategoryItems = savedItems.get(currentCategory);

        // Get the item to move
        SavedItemStack savedItemStack = originalCategoryItems.get(selectedItem);

        // Remove from original and put in new
        originalCategoryItems.remove(selectedItem);
        newCategoryItems.put(selectedItem, savedItemStack);

        // Update categories
        savedItems.put(originalCategory, originalCategoryItems);
        savedItems.put(currentCategory, newCategoryItems);

        Services.ItemVault.savedItems.store(savedItems);
        Services.ItemVault.savedItems.touched();

        // Deselect item
        originalCategory = "";
        selectedItem = "";
        selectedItemSlot = null;

        populateItems();
    }

    private void deleteItem(String base64) {
        Map<String, Map<String, SavedItemStack>> savedItems = Services.ItemVault.savedItems.get();
        Map<String, SavedItemStack> categoryItems = savedItems.get(currentCategory);

        // Remove item from category
        categoryItems.remove(base64);
        savedItems.put(currentCategory, categoryItems);

        Services.ItemVault.savedItems.store(savedItems);
        Services.ItemVault.savedItems.touched();

        // Scroll up if there is now an empty row
        if ((categoryItems.size() % ITEMS_PER_ROW) == 0) {
            itemScrollOffset--;
        }

        populateItems();
    }

    private void populateItems() {
        encodedItems = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();

        // Clear current items
        for (int i = 0; i < MAX_ITEMS; i++) {
            this.menu.setItem(i, 0, ItemStack.EMPTY);
        }

        Map<String, SavedItemStack> savedItems =
                Services.ItemVault.savedItems.get().getOrDefault(currentCategory, new HashMap<>());

        // No items in current category
        if (savedItems.isEmpty()) return;

        int itemIndex = 0;
        // So that we don't need to re-encode items when they are clicked to be shared
        // we can keep a list of them and use the slot index to get their encoded string.
        // Since we're looping through a map, we need to skip to ensure that the item that
        // will be in index 0 of the list, will be index 0 in the menu etc.
        int itemsToSkip = ITEMS_PER_ROW * itemScrollOffset;

        for (Map.Entry<String, SavedItemStack> entry : savedItems.entrySet()) {
            if (itemIndex < itemsToSkip) {
                itemIndex++;
                continue;
            }

            String base64Item = entry.getKey();
            encodedItems.add(base64Item);

            // Create the ItemStack from the store tag values
            ItemStack itemStack;
            itemStack = new ItemStack(Item.byId(entry.getValue().itemID()), 1);
            CompoundTag compoundTag = new CompoundTag();
            CompoundTag displayTag = new CompoundTag();
            compoundTag.putInt("Damage", entry.getValue().damage());
            compoundTag.putInt("HideFlags", entry.getValue().hideFlags());
            compoundTag.putBoolean("Unbreakable", entry.getValue().unbreakable());

            // If there is a color value that isn't the default, then add that too
            if (entry.getValue().color() != -1) {
                displayTag.putInt("color", entry.getValue().color());
            }

            compoundTag.put("display", displayTag);
            itemStack.setTag(compoundTag);

            ErrorOr<WynnItem> errorOrWynnItem =
                    Models.ItemEncoding.decodeItem(EncodedByteBuffer.fromBase64String(base64Item));
            if (errorOrWynnItem.hasError()) {
                WynntilsMod.error("Failed to encode item: " + errorOrWynnItem.getError());
                McUtils.sendErrorToClient(
                        I18n.get("feature.wynntils.chatItem.chatItemErrorEncode", errorOrWynnItem.getError()));
                continue;
            } else {
                itemStack = new FakeItemStack(
                        errorOrWynnItem.getValue(), itemStack, "From " + McUtils.playerName() + "'s vault");
            }

            items.add(itemStack);

            itemIndex++;
        }

        // Update items in menu
        for (int i = 0; i < MAX_ITEMS; i++) {
            if (i > items.size() - 1) break;

            this.menu.setItem(i, 0, items.get(i));
        }
    }

    private void scrollItems(int scollDirection) {
        int maxValue = getMaxScrollOffset();

        itemScrollOffset = MathUtils.clamp(itemScrollOffset + scollDirection, 0, maxValue);

        populateItems();
    }

    private int getMaxScrollOffset() {
        int maxItemOffset = Math.max(
                0,
                Services.ItemVault.savedItems
                                .get()
                                .getOrDefault(currentCategory, new HashMap<>())
                                .size()
                        - MAX_ITEMS);
        return maxItemOffset / ITEMS_PER_ROW + (maxItemOffset % ITEMS_PER_ROW > 0 ? 1 : 0);
    }
}
