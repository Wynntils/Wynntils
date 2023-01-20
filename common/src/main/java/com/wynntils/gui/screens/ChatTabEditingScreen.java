/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.chat.tabs.ChatTab;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.features.user.ChatTabsFeature;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.TextShadow;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.widgets.TextInputBoxWidget;
import com.wynntils.handlers.chat.RecipientType;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.CommonColors;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class ChatTabEditingScreen extends Screen implements TextboxScreen {
    private TextInputBoxWidget focusedTextInput;

    private TextInputBoxWidget nameInput;
    private TextInputBoxWidget autoCommandInput;
    private TextInputBoxWidget orderInput;
    private final List<Checkbox> recipientTypeBoxes = new ArrayList<>();
    private TextInputBoxWidget filterRegexInput;
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
    protected void init() {
        // region Name
        this.addRenderableWidget(
                nameInput = new TextInputBoxWidget(
                        this.width / 2 - 160,
                        this.height / 2 - 70,
                        110,
                        20,
                        (s) -> updateSaveStatus(),
                        this,
                        nameInput));

        if (firstSetup) {
            if (edited != null) {
                nameInput.setTextBoxInput(edited.getName());
            }
            setFocusedTextInput(nameInput);
        }
        // endregion

        // region Auto Command
        this.addRenderableWidget(
                autoCommandInput = new TextInputBoxWidget(
                        this.width / 2 - 30, this.height / 2 - 70, 110, 20, null, this, autoCommandInput));
        if (firstSetup && edited != null && edited.getAutoCommand() != null) {
            autoCommandInput.setTextBoxInput(edited.getAutoCommand());
        }
        // endregion

        // region Order
        this.addRenderableWidget(
                orderInput = new TextInputBoxWidget(
                        this.width / 2 + 100,
                        this.height / 2 - 70,
                        20,
                        20,
                        (s) -> updateSaveStatus(),
                        this,
                        orderInput));
        if (firstSetup && edited != null) {
            orderInput.setTextBoxInput(Integer.toString(ChatTabsFeature.INSTANCE.chatTabs.indexOf(edited)));
        }
        // endregion

        // region Recipient Types

        // Display all recipient types in two rows of 4 checkboxes each
        List<Checkbox> oldBoxes = new ArrayList<>(recipientTypeBoxes);
        recipientTypeBoxes.clear();

        int x = this.width / 2 - 160;
        int y = this.height / 2 - 20;
        for (int i = 0; i < RecipientType.values().length; i++) {
            if (i == 4) {
                y += 20;
                x = this.width / 2 - 160;
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

            x += 80;
        }

        // endregion

        // region Filter Regex
        this.addRenderableWidget(
                filterRegexInput = new TextInputBoxWidget(
                        this.width / 2 - 160, this.height / 2 + 45, 300, 20, null, this, filterRegexInput));
        if (firstSetup && edited != null && edited.getCustomRegexString() != null) {
            filterRegexInput.setTextBoxInput(edited.getCustomRegexString());
        }
        // endregion

        // region Consuming
        this.addRenderableWidget(
                consumingCheckbox = new Checkbox(
                        this.width / 2 - 160,
                        this.height / 2 + 75,
                        20,
                        20,
                        Component.translatable("screens.wynntils.chatTabsGui.consuming"),
                        consumingCheckbox != null && consumingCheckbox.selected(),
                        true));
        if (firstSetup && edited != null) {
            consumingCheckbox.selected = edited.isConsuming();
        }
        // endregion

        // region Screen Interactions
        this.addRenderableWidget(
                saveButton = new Button.Builder(
                                Component.translatable("screens.wynntils.chatTabsGui.save")
                                        .withStyle(ChatFormatting.DARK_GREEN),
                                (button) -> {
                                    saveChatTab();
                                    this.onClose();
                                })
                        .pos(this.width / 2 - 200, this.height - 40)
                        .size(100, 20)
                        .build());

        this.addRenderableWidget(
                deleteButton = new Button.Builder(
                                Component.translatable("screens.wynntils.chatTabsGui.delete")
                                        .withStyle(ChatFormatting.DARK_RED),
                                (button) -> {
                                    deleteChatTab();
                                    this.onClose();
                                })
                        .pos(this.width / 2 - 50, this.height - 40)
                        .size(100, 20)
                        .build());

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.chatTabsGui.cancel"), (button) -> this.onClose())
                .pos(this.width / 2 + 100, this.height - 40)
                .size(100, 20)
                .build());
        // endregion

        firstSetup = false;
        deleteButton.active = edited != null;
        updateSaveStatus();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        // Name
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.chatTabsGui.name") + ChatFormatting.DARK_RED + " *",
                        this.width / 2f - 160,
                        this.height / 2f - 85,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);

        // Auto Command
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.chatTabsGui.autoCommand"),
                        this.width / 2f - 30,
                        this.height / 2f - 85,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);

        // Order
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.chatTabsGui.order"),
                        this.width / 2f + 100,
                        this.height / 2f - 85,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);

        // Recipient Types
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.chatTabsGui.types") + ChatFormatting.DARK_RED + " *",
                        this.width / 2f - 160,
                        this.height / 2f - 40,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);

        // Filter Pattern
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.chatTabsGui.filter"),
                        this.width / 2f - 160,
                        this.height / 2f + 30,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

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
            ChatTabsFeature.INSTANCE.chatTabs.remove(edited);
        }

        int insertIndex = orderInput.getTextBoxInput().isEmpty()
                ? ChatTabsFeature.INSTANCE.chatTabs.size()
                : Math.min(ChatTabsFeature.INSTANCE.chatTabs.size(), Integer.parseInt(orderInput.getTextBoxInput()));

        ChatTabsFeature.INSTANCE.chatTabs.add(
                insertIndex,
                new ChatTab(
                        nameInput.getTextBoxInput(),
                        consumingCheckbox.selected(),
                        autoCommandInput.getTextBoxInput(),
                        recipientTypeBoxes.stream()
                                .filter(Checkbox::selected)
                                .map(box ->
                                        RecipientType.fromName(box.getMessage().getString()))
                                .collect(Collectors.toSet()),
                        filterRegexInput.getTextBoxInput().isBlank() ? null : filterRegexInput.getTextBoxInput()));

        Managers.Config.saveConfig();
    }

    private void deleteChatTab() {
        ChatTabsFeature.INSTANCE.chatTabs.remove(edited);
        if (Objects.equals(Models.ChatTab.getFocusedTab(), edited)) {
            if (!ChatTabsFeature.INSTANCE.chatTabs.isEmpty()) {
                Models.ChatTab.setFocusedTab(ChatTabsFeature.INSTANCE.chatTabs.get(0));
            } else {
                Models.ChatTab.setFocusedTab(null);
            }
        }

        Managers.Config.saveConfig();
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
                && recipientTypeBoxes.stream().anyMatch(Checkbox::selected);
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
