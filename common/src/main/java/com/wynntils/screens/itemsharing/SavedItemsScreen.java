/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemsharing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.WynnItem;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.WynntilsContainerScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.itemsharing.widgets.SavedCategoryButton;
import com.wynntils.screens.itemsharing.widgets.SavedItemsButton;
import com.wynntils.services.itemvault.type.SavedItem;
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
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class SavedItemsScreen extends WynntilsContainerScreen<SavedItemsMenu> {
    private static final int ITEMS_PER_ROW = 7;
    private static final int MAX_ITEMS = 49;
    private static final int SCROLL_AREA_HEIGHT = 128;

    private boolean addingCategory = false;
    private boolean draggingScroll = false;
    private boolean editingCategory = false;
    private int itemScrollOffset = 0;
    private List<Integer> selectedSlots = new ArrayList<>();
    private List<SavedItem> encodedItems = new ArrayList<>();
    private List<Pair<String, String>> selectedItems = new ArrayList<>();
    private String currentCategory = Services.ItemVault.getDefaultCategory();
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
                    if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        if (!addingCategory) {
                            addingCategory = true;
                            addCategoryInput();
                        } else {
                            addCategory(KeyboardUtils.isShiftDown());
                            addingCategory = false;
                        }
                    }
                },
                List.of(
                        Component.translatable("screens.wynntils.savedItems.addTooltip1"),
                        Component.translatable("screens.wynntils.savedItems.addTooltip2"),
                        Component.translatable("screens.wynntils.savedItems.addTooltip3"),
                        Component.translatable("screens.wynntils.savedItems.addTooltip4")),
                Texture.VAULT_ADD));

        this.addRenderableWidget(new SavedItemsButton(
                this.leftPos + 8,
                this.topPos + 36,
                (b) -> {
                    selectedItems = new ArrayList<>();

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
                    if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        moveSelectedItems();
                    } else if (b == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        selectedItems = new ArrayList<>();
                        selectedSlots = new ArrayList<>();
                    }
                },
                List.of(
                        Component.translatable("screens.wynntils.savedItems.changeCategoryTooltip1"),
                        Component.translatable("screens.wynntils.savedItems.changeCategoryTooltip2"),
                        Component.translatable("screens.wynntils.savedItems.changeCategoryTooltip3")),
                Texture.VAULT_CONFIRM));

        this.addRenderableWidget(new SavedItemsButton(
                this.leftPos + 8,
                this.topPos + 140,
                null,
                List.of(
                        Component.translatable("screens.wynntils.savedItems.help")
                                .withStyle(ChatFormatting.UNDERLINE),
                        Component.translatable("screens.wynntils.savedItems.help1"),
                        Component.translatable("screens.wynntils.savedItems.help2"),
                        Component.translatable("screens.wynntils.savedItems.help3"),
                        Component.translatable("screens.wynntils.savedItems.help4")),
                Texture.VAULT_HELP));
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
        // Right click toggles the selection of the item
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (KeyboardUtils.isShiftDown()) {
                deleteItem(encodedItems.get(slot.index).base64());
            } else {
                Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(slot.getItem());

                if (wynnItemOpt.isPresent()) {
                    McUtils.mc().setScreen(ItemSharingScreen.create(wynnItemOpt.get(), slot.getItem(), true));
                } else {
                    McUtils.sendMessageToClient(Component.translatable("screens.wynntils.savedItems.unableToShare"));
                }
            }
        } else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Pair<String, String> selectedItem =
                    new Pair<>(currentCategory, encodedItems.get(slot.index).base64());

            if (selectedItems.contains(selectedItem)) {
                selectedItems.remove(selectedItem);
                selectedSlots.remove((Integer) slot.index);
            } else {
                selectedItems.add(selectedItem);
                selectedSlots.add(slot.index);
            }
        }
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        PoseStack poseStack = guiGraphics.pose();

        if (!addingCategory && !editingCategory) {
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

        for (int selectedSlot : selectedSlots) {
            RenderUtils.drawRectBorders(
                    poseStack,
                    CommonColors.WHITE,
                    this.leftPos + this.getMenu().getSlot(selectedSlot).x,
                    this.topPos + this.getMenu().getSlot(selectedSlot).y,
                    this.leftPos + this.getMenu().getSlot(selectedSlot).x + 16,
                    this.topPos + this.getMenu().getSlot(selectedSlot).y + 16,
                    0,
                    1);
        }

        renderScrollButton(poseStack);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
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

        float categoryRenderX = this.leftPos + 36;
        float categoryRenderY = this.topPos + 10;

        // Click the area where the category title is rendered to edit it
        if (!currentCategory.equals(Services.ItemVault.getDefaultCategory())
                && !editingCategory
                && !addingCategory
                && MathUtils.isInside(
                        (int) mouseX,
                        (int) mouseY,
                        (int) categoryRenderX,
                        (int) (categoryRenderX + 99),
                        (int) categoryRenderY,
                        (int) (categoryRenderY + 8))) {
            if (!editingCategory) {
                editingCategory = true;
                addCategoryInput();
            }

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
        if ((addingCategory || editingCategory) && keyCode == GLFW.GLFW_KEY_ENTER) {
            addCategory(KeyboardUtils.isShiftDown());

            if (addingCategory) {
                addingCategory = false;
            } else if (editingCategory) {
                editingCategory = false;
            }
            return false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void scrollCategories(int scrollDirection) {
        List<String> categories = new ArrayList<>(Services.ItemVault.categories.get());

        addingCategory = false;
        editingCategory = false;
        this.removeWidget(categoryInput);
        categoryInput = null;

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

        // Get the current category name and add if we are editing
        if (editingCategory) {
            categoryInput.setTextBoxInput(currentCategory);
        }

        this.addRenderableWidget(categoryInput);
    }

    private void addCategory(boolean keepOriginals) {
        String newCategory = categoryInput.getTextBoxInput();

        // Remove the input widget
        this.removeWidget(categoryInput);
        categoryInput = null;

        if (newCategory.isEmpty()) return;

        if (addingCategory) {
            Services.ItemVault.addCategory(newCategory, selectedItems, keepOriginals);

            if (!selectedItems.isEmpty()) {
                currentCategory = newCategory;
                selectedItems = new ArrayList<>();
                populateItems();
            }
        } else if (editingCategory) {
            Services.ItemVault.renameCategory(currentCategory, newCategory);

            // Change to new category
            currentCategory = newCategory;

            populateItems();
        }
    }

    private void deleteCategory() {
        Services.ItemVault.deleteCategory(currentCategory);

        // Reset to default category
        currentCategory = Services.ItemVault.getDefaultCategory();

        populateItems();
    }

    private void moveSelectedItems() {
        Services.ItemVault.moveSelectedItems(selectedItems, currentCategory, KeyboardUtils.isShiftDown());

        selectedItems = new ArrayList<>();

        populateItems();
    }

    private void deleteItem(String base64) {
        Services.ItemVault.deleteItem(base64);

        // Scroll up if there is now an empty row
        if (((encodedItems.size() - 1) % ITEMS_PER_ROW) == 0 && itemScrollOffset > 0) {
            itemScrollOffset--;
        }

        populateItems();
    }

    private void populateItems() {
        List<ItemStack> items = new ArrayList<>();
        List<ItemStack> selected = new ArrayList<>();
        encodedItems = new ArrayList<>();
        selectedSlots = new ArrayList<>();

        // Clear current items
        this.menu.clear();

        List<SavedItem> savedItems = Services.ItemVault.savedItems.get().stream()
                .filter(savedItem -> savedItem.categories().contains(currentCategory))
                .toList();

        // No items in current category
        if (savedItems.isEmpty()) return;

        int rowOffset = ITEMS_PER_ROW * itemScrollOffset;

        for (int i = rowOffset; i < (MAX_ITEMS + rowOffset); i++) {
            if (i >= savedItems.size()) break;

            SavedItem savedItem = savedItems.get(i);
            encodedItems.add(savedItem);

            ItemStack itemStack = savedItem.itemStack();

            itemStack = new FakeItemStack(savedItem.wynnItem(), itemStack, "From " + McUtils.playerName() + "'s vault");

            for (Pair<String, String> selectedItem : selectedItems) {
                if (selectedItem.a().equals(currentCategory) && selectedItem.b().equals(savedItem.base64())) {
                    selected.add(itemStack);
                    break;
                }
            }

            items.add(itemStack);
        }

        // Update items in menu
        for (int i = 0; i < MAX_ITEMS; i++) {
            if (i > items.size() - 1) break;

            this.menu.setItem(i, 0, items.get(i));

            if (selected.contains(items.get(i))) {
                selectedSlots.add(i);
            }
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
                Services.ItemVault.savedItems.get().stream()
                                .filter(savedItem -> savedItem.categories().contains(currentCategory))
                                .toList()
                                .size()
                        - MAX_ITEMS);
        return maxItemOffset / ITEMS_PER_ROW + (maxItemOffset % ITEMS_PER_ROW > 0 ? 1 : 0);
    }
}
