/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.chattabs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.TextWidget;
import com.wynntils.services.chat.ChatTab;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.glfw.GLFW;

public final class ChatTabEditingScreen extends WynntilsScreen implements TextboxScreen {
    private static final float GRID_DIVISIONS = 64.0f;
    private static final int FIRST_ROW_Y = 16;
    private static final int SECOND_ROW_Y = 23;
    private static final int THIRD_ROW_Y = 35;
    private static final int FOURTH_ROW_Y = 39;
    private static final int FIFTH_ROW_Y = 49;

    private float dividedHeight;
    private float dividedWidth;

    private TextInputBoxWidget focusedTextInput;

    private TextInputBoxWidget nameInput;
    private TextInputBoxWidget autoCommandInput;
    private TextInputBoxWidget orderInput;
    private final List<Checkbox> recipientTypeBoxes = new ArrayList<>();
    private TextInputBoxWidget filterRegexInput;
    private TextWidget regexErrorMsg;
    private Checkbox consumingCheckbox;

    private Button saveButton;
    private Button deleteButton;

    private boolean firstSetup;
    private final ChatTab edited;

    private ChatTabEditingScreen() {
        this(null);
    }

    private ChatTabEditingScreen(ChatTab tab) {
        super(Component.literal("Chat Tab Editing Screen"));

        this.edited = tab;
        this.firstSetup = true;
    }

    public static Screen create() {
        return new ChatTabEditingScreen();
    }

    public static Screen create(ChatTab chatTab) {
        return new ChatTabEditingScreen(chatTab);
    }

    @Override
    protected void doInit() {
        dividedWidth = this.width / GRID_DIVISIONS;
        dividedHeight = this.height / GRID_DIVISIONS;

        // region Name
        nameInput = new TextInputBoxWidget(
                (int) (dividedWidth * 35),
                (int) ((dividedHeight * FIRST_ROW_Y)),
                (int) (dividedWidth * 10),
                20,
                (s) -> updateSaveStatus(),
                this,
                nameInput);
        this.addRenderableWidget(nameInput);

        if (firstSetup) {
            if (edited != null) {
                nameInput.setTextBoxInput(edited.getName());
            }
            setFocusedTextInput(nameInput);
        }
        // endregion

        // region Auto Command
        autoCommandInput = new TextInputBoxWidget(
                (int) (dividedWidth * 47),
                (int) (dividedHeight * FIRST_ROW_Y),
                (int) (dividedWidth * 10),
                20,
                null,
                this,
                autoCommandInput);
        this.addRenderableWidget(autoCommandInput);
        if (firstSetup && edited != null && edited.getAutoCommand() != null) {
            autoCommandInput.setTextBoxInput(edited.getAutoCommand());
        }
        // endregion

        // region Order
        orderInput = new TextInputBoxWidget(
                (int) (dividedWidth * 59),
                (int) (dividedHeight * FIRST_ROW_Y),
                (int) (dividedWidth * 2),
                20,
                (s) -> updateSaveStatus(),
                this,
                orderInput);
        this.addRenderableWidget(orderInput);
        if (firstSetup && edited != null) {
            orderInput.setTextBoxInput(Integer.toString(Services.ChatTab.getTabIndex(edited)));
        }
        // endregion

        // region Recipient Types

        // Display all recipient types in two rows of 4 checkboxes each
        List<Checkbox> oldBoxes = new ArrayList<>(recipientTypeBoxes);
        recipientTypeBoxes.clear();

        int x = (int) (dividedWidth * 35);
        int y = (int) (dividedHeight * SECOND_ROW_Y);
        for (int i = 0; i < RecipientType.values().length; i++) {
            if (i == 4 || i == 8) {
                y += (int) (dividedHeight * 3);
                x = (int) (dividedWidth * 35);
            }

            RecipientType type = RecipientType.values()[i];
            Checkbox oldCheckbox = oldBoxes.stream()
                    .filter(checkbox -> checkbox.getMessage().getString().equals(type.getName()))
                    .findFirst()
                    .orElse(null);

            boolean oldCheckboxSelected = oldCheckbox != null && oldCheckbox.selected();
            boolean editedFirstSetupSelected = firstSetup
                    && edited != null
                    && (edited.getFilteredTypes() == null
                            || edited.getFilteredTypes().contains(type));
            boolean ticked = oldCheckboxSelected || editedFirstSetupSelected;

            Checkbox newBox = new Checkbox(x, y, 20, 20, Component.literal(type.getName()), ticked, true);
            this.addRenderableWidget(newBox);
            recipientTypeBoxes.add(newBox);

            x += (int) (dividedWidth * 6);
        }

        // endregion

        // region Filter Regex
        filterRegexInput = new TextInputBoxWidget(
                (int) (dividedWidth * 35),
                (int) (dividedHeight * THIRD_ROW_Y),
                (int) (dividedWidth * 26),
                20,
                (s) -> updateSaveStatus(),
                this,
                filterRegexInput);
        this.addRenderableWidget(filterRegexInput);
        if (firstSetup && edited != null && edited.getCustomRegexString() != null) {
            filterRegexInput.setTextBoxInput(edited.getCustomRegexString());
        }

        regexErrorMsg =
                new TextWidget(this.width / 2 - 160 + 100, this.height / 2 + 75 + 7, 200, 20, Component.empty());
        this.addRenderableWidget(regexErrorMsg);
        // endregion

        // region Consuming
        consumingCheckbox = new Checkbox(
                (int) (dividedWidth * 35),
                (int) (dividedHeight * FOURTH_ROW_Y),
                20,
                20,
                Component.translatable("screens.wynntils.chatTabsGui.consuming"),
                consumingCheckbox != null && consumingCheckbox.selected(),
                true);
        this.addRenderableWidget(consumingCheckbox);
        if (firstSetup && edited != null) {
            consumingCheckbox.selected = edited.isConsuming();
        }
        // endregion

        // region Screen Interactions
        saveButton = new Button.Builder(
                Component.translatable("screens.wynntils.chatTabsGui.save")
                        .withStyle(ChatFormatting.DARK_GREEN),
                (button) -> {
                    saveChatTab();
                    this.onClose();
                })
                .pos((int) (dividedWidth * 35), (int) (dividedHeight * FIFTH_ROW_Y))
                .size((int) (dividedWidth * 8), 20)
                .build();
        this.addRenderableWidget(saveButton);

        deleteButton = new Button.Builder(
                Component.translatable("screens.wynntils.chatTabsGui.delete")
                        .withStyle(ChatFormatting.DARK_RED),
                (button) -> {
                    deleteChatTab();
                    this.onClose();
                })
                .pos((int) (dividedWidth * 44), (int) (dividedHeight * FIFTH_ROW_Y))
                .size((int) (dividedWidth * 8), 20)
                .build();
        this.addRenderableWidget(deleteButton);

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.chatTabsGui.cancel"), (button) -> this.onClose())
                .pos((int) (dividedWidth * 53), (int) (dividedHeight * FIFTH_ROW_Y))
                .size((int) (dividedWidth * 8), 20)
                .build());
        // endregion

        firstSetup = false;
        deleteButton.active = edited != null;
        updateSaveStatus();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        PoseStack poseStack = guiGraphics.pose();

        dividedWidth = this.width / GRID_DIVISIONS;
        dividedHeight = this.height / GRID_DIVISIONS;

        // Dev/Debug: Uncomment when editing GUI elements for debug grid
        // RenderUtils.renderDebugGrid(poseStack, GRID_DIVISIONS, dividedWidth, dividedHeight);

        // Name
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(
                                I18n.get("screens.wynntils.chatTabsGui.name") + ChatFormatting.DARK_RED + " *"),
                        (int) (dividedWidth * 35),
                        (int) (dividedHeight * FIRST_ROW_Y),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        // Auto Command
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.chatTabsGui.autoCommand")),
                        (int) (dividedWidth * 47),
                        (int) (dividedHeight * FIRST_ROW_Y),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        // Order
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.chatTabsGui.order")),
                        (int) (dividedWidth * 59),
                        (int) (dividedHeight * FIRST_ROW_Y),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        // Recipient Types
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(
                                I18n.get("screens.wynntils.chatTabsGui.types") + ChatFormatting.DARK_RED + " *"),
                        (int) (dividedWidth * 35),
                        (int) (dividedHeight * (SECOND_ROW_Y - 1)),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        // Filter Pattern
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.chatTabsGui.filter")),
                        (int) (dividedWidth * 35),
                        (int) (dividedHeight * THIRD_ROW_Y),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        super.doMouseClicked(mouseX, mouseY, button);

        updateSaveStatus();

        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return (focusedTextInput != null && focusedTextInput.charTyped(codePoint, modifiers))
                || super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // When tab is pressed, focus the next text box
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            int index = focusedTextInput == null ? 0 : children().indexOf(focusedTextInput);
            int actualIndex = Math.max(index, 0) + 1;

            // Try to find next text input
            // From index - end
            for (int i = actualIndex; i < children().size(); i++) {
                if (children().get(i) instanceof TextInputBoxWidget textInputBoxWidget) {
                    setFocusedTextInput(textInputBoxWidget);
                    return true;
                }
            }

            // From 0 - index
            for (int i = 0; i < Math.min(actualIndex, children().size()); i++) {
                if (children().get(i) instanceof TextInputBoxWidget textInputBoxWidget) {
                    setFocusedTextInput(textInputBoxWidget);
                    return true;
                }
            }
        }

        return (focusedTextInput != null && focusedTextInput.keyPressed(keyCode, scanCode, modifiers))
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(new ChatScreen(""));
    }

    private void saveChatTab() {
        if (edited != null) {
            Services.ChatTab.removeTab(edited);
        }

        int insertIndex = orderInput.getTextBoxInput().isEmpty()
                ? Services.ChatTab.getTabCount()
                : Math.min(Services.ChatTab.getTabCount(), Integer.parseInt(orderInput.getTextBoxInput()));

        ChatTab chatTab = new ChatTab(
                nameInput.getTextBoxInput(),
                consumingCheckbox.selected(),
                autoCommandInput.getTextBoxInput(),
                recipientTypeBoxes.stream()
                        .filter(Checkbox::selected)
                        .map(box -> RecipientType.fromName(box.getMessage().getString()))
                        .collect(Collectors.toSet()),
                filterRegexInput.getTextBoxInput().isBlank() ? null : filterRegexInput.getTextBoxInput());
        Services.ChatTab.addTab(insertIndex, chatTab);
    }

    private void deleteChatTab() {
        Services.ChatTab.removeTab(edited);
        if (Objects.equals(Services.ChatTab.getFocusedTab(), edited)) {
            if (!Services.ChatTab.isTabListEmpty()) {
                Services.ChatTab.setFocusedTab(0);
            } else {
                Services.ChatTab.setFocusedTab(null);
            }
        }
    }

    private void updateSaveStatus() {
        if (orderInput != null && !orderInput.getTextBoxInput().isBlank()) {
            try {
                Integer.parseInt(orderInput.getTextBoxInput());
                orderInput.setRenderColor(CommonColors.GREEN);
            } catch (NumberFormatException ignored) {
                orderInput.setRenderColor(CommonColors.RED);
                saveButton.active = false;
            }
        }

        if (saveButton == null) return;

        saveButton.active = !nameInput.getTextBoxInput().isEmpty()
                && validatePattern()
                && recipientTypeBoxes.stream().anyMatch(Checkbox::selected);
    }

    private boolean validatePattern() {
        try {
            Pattern.compile(filterRegexInput.getTextBoxInput());
            regexErrorMsg.setMessage(Component.empty());
        } catch (PatternSyntaxException e) {
            MutableComponent errorMessage = Component.literal(e.getDescription())
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal(" (at pos " + e.getIndex() + ")").withStyle(ChatFormatting.DARK_RED));
            regexErrorMsg.setMessage(errorMessage);
            return false;
        }
        return true;
    }

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return focusedTextInput;
    }

    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {
        this.focusedTextInput = focusedTextInput;
    }
}
