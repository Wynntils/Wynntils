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
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.WynntilsContainerScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.itemsharing.widgets.SavedCategoryButton;
import com.wynntils.screens.itemsharing.widgets.SavedItemsButton;
import com.wynntils.screens.itemsharing.widgets.SavedItemsHelpButton;
import com.wynntils.services.itemvault.type.SavedItem;
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
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
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
    private boolean editingCategory = false;
    private int itemScrollOffset = 0;
    private List<Integer> selectedSlots = new ArrayList<>();
    private List<SavedItem> encodedItems = new ArrayList<>();
    private List<Pair<Pair<String, Boolean>, String>> selectedItems = new ArrayList<>();
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
                    if (b == 0) {
                        if (!addingCategory) {
                            addingCategory = true;
                            addCategoryInput();
                        } else {
                            addCategory();
                            addingCategory = false;
                        }
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
                    if (b == 0) {
                        moveSelectedItems();
                    } else if (b == 1) {
                        selectedItems = new ArrayList<>();
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
                        Component.translatable("screens.wynntils.savedItems.help3"),
                        Component.translatable("screens.wynntils.savedItems.help4"),
                        Component.translatable("screens.wynntils.savedItems.help5"))));
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
        // Right+Shift will also deletes it from the current category when moved
        if (mouseButton == 0) {
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
        } else if (mouseButton == 1) {
            Pair<Pair<String, Boolean>, String> selectedItem = new Pair<>(
                    new Pair<>(currentCategory, !KeyboardUtils.isShiftDown()),
                    encodedItems.get(slot.index).base64());

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

        this.renderTooltip(guiGraphics, mouseX, mouseY);

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
            } else {
                addCategory();
                editingCategory = false;
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
            addCategory();

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

    private void addCategory() {
        String newCategory = categoryInput.getTextBoxInput();

        // Remove the input widget
        this.removeWidget(categoryInput);
        categoryInput = null;

        if (newCategory.isEmpty()) return;

        if (addingCategory) {
            // Save new category
            Services.ItemVault.categories.get().add(newCategory);
            Services.ItemVault.categories.touched();

            if (!selectedItems.isEmpty()) {
                currentCategory = newCategory;
                moveSelectedItems();
            }
        } else if (editingCategory) {
            // Add renamed category and remove previous name
            Services.ItemVault.categories.get().add(newCategory);
            Services.ItemVault.categories.get().remove(currentCategory);
            Services.ItemVault.categories.touched();

            for (SavedItem savedItem : Services.ItemVault.savedItems.get()) {
                // If an item is in the current category, add it to the renamed and remove previous name
                if (savedItem.categories().contains(currentCategory)) {
                    savedItem.categories().add(newCategory);
                    savedItem.categories().remove(currentCategory);
                }
            }

            Services.ItemVault.savedItems.touched();

            // Change to new category
            currentCategory = newCategory;

            populateItems();
        }
    }

    private void deleteCategory() {
        Set<SavedItem> savedItems = Services.ItemVault.savedItems.get();

        if (KeyboardUtils.isShiftDown()) {
            Set<SavedItem> newSavedItems = new TreeSet<>();

            // Remove category from all items
            for (SavedItem savedItem : savedItems) {
                savedItem.categories().remove(currentCategory);

                // If the item is no longer in any categories then it should be deleted
                if (!savedItem.categories().isEmpty()) {
                    newSavedItems.add(savedItem);
                }
            }

            Services.ItemVault.savedItems.store(newSavedItems);
            Services.ItemVault.savedItems.touched();
        } else if (!currentCategory.equals(Services.ItemVault.getDefaultCategory())) {
            // Remove category from all items and add default
            for (SavedItem savedItem : savedItems) {
                savedItem.categories().remove(currentCategory);
                savedItem.categories().add(Services.ItemVault.getDefaultCategory());
            }

            Services.ItemVault.savedItems.store(savedItems);
            Services.ItemVault.savedItems.touched();
        }

        // If current category is not the default, delete it
        if (!currentCategory.equals(Services.ItemVault.getDefaultCategory())) {
            Services.ItemVault.categories.get().remove(currentCategory);
            Services.ItemVault.categories.touched();
        }
        // Reset to default category
        currentCategory = Services.ItemVault.getDefaultCategory();

        populateItems();
    }

    private void moveSelectedItems() {
        for (Pair<Pair<String, Boolean>, String> selectedItem : selectedItems) {
            SavedItem savedItem = Services.ItemVault.getItem(selectedItem.b());

            if (selectedItem != null) {
                moveItemCategory(
                        savedItem, selectedItem.a().a(), selectedItem.a().b());
            }
        }

        selectedItems = new ArrayList<>();
    }

    private void moveItemCategory(SavedItem savedItem, String originalCategory, boolean keepOriginal) {
        Set<SavedItem> savedItems = Services.ItemVault.savedItems.get();
        savedItems.remove(savedItem);

        savedItem.categories().add(currentCategory);

        if (!keepOriginal) {
            savedItem.categories().remove(originalCategory);
        }

        savedItems.add(savedItem);
        Services.ItemVault.savedItems.store(savedItems);
        Services.ItemVault.savedItems.touched();

        selectedItems = new ArrayList<>();

        populateItems();
    }

    private void deleteItem(String base64) {
        Set<SavedItem> savedItems = new TreeSet<>(Services.ItemVault.savedItems.get());

        for (SavedItem savedItem : savedItems) {
            if (savedItem.base64().equals(base64)) {
                Services.ItemVault.savedItems.get().remove(savedItem);
                Services.ItemVault.savedItems.touched();
                break;
            }
        }

        // Scroll up if there is now an empty row
        if (((encodedItems.size() - 1) % ITEMS_PER_ROW) == 0) {
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
        for (int i = 0; i < MAX_ITEMS; i++) {
            this.menu.setItem(i, 0, ItemStack.EMPTY);
        }

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

            // Create the ItemStack from the store tag values
            ItemStack itemStack;
            itemStack = new ItemStack(Item.byId(savedItem.itemID()), 1);
            CompoundTag compoundTag = new CompoundTag();
            CompoundTag displayTag = new CompoundTag();
            compoundTag.putInt("Damage", savedItem.damage());
            compoundTag.putInt("HideFlags", savedItem.hideFlags());
            compoundTag.putBoolean("Unbreakable", savedItem.unbreakable());

            // If there is a color value that isn't the default, then add that too
            if (savedItem.color() != -1) {
                displayTag.putInt("color", savedItem.color());
            }

            compoundTag.put("display", displayTag);
            itemStack.setTag(compoundTag);

            ErrorOr<WynnItem> errorOrWynnItem =
                    Models.ItemEncoding.decodeItem(EncodedByteBuffer.fromBase64String(savedItem.base64()));
            if (errorOrWynnItem.hasError()) {
                WynntilsMod.error("Failed to encode item: " + errorOrWynnItem.getError());
                McUtils.sendErrorToClient(
                        I18n.get("feature.wynntils.chatItem.chatItemErrorEncode", errorOrWynnItem.getError()));
                continue;
            } else {
                itemStack = new FakeItemStack(
                        errorOrWynnItem.getValue(), itemStack, "From " + McUtils.playerName() + "'s vault");
            }

            for (Pair<Pair<String, Boolean>, String> selectedItem : selectedItems) {
                if (selectedItem.b().equals(savedItem.base64())) {
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
