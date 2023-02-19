/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.partymanagement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.partymanagement.widgets.CreateLeaveButton;
import com.wynntils.screens.partymanagement.widgets.PartyMemberWidget;
import com.wynntils.screens.partymanagement.widgets.SuggestionPlayerWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class PartyManagementScreen extends Screen implements TextboxScreen {
    private static final Pattern INVITE_REPLACER = Pattern.compile("[^\\w, ]+");
    private static final Pattern COMMA_REPLACER = Pattern.compile("[,; ]+");

    private static final int BUTTON_WIDTH = 80;
    private static final int GAP = 4;
    private static final int TOTAL_WIDTH = 3 * BUTTON_WIDTH + 2 * GAP;
    private static final int X_START = TOTAL_WIDTH / 2;

    private TextInputBoxWidget focusedTextInput;

    private TextInputBoxWidget inviteInput;

    private Button inviteButton;
    private Button kickOfflineButton;
    private CreateLeaveButton createLeaveButton;
    private final List<AbstractWidget> suggestedPlayersWidgets = new ArrayList<>();
    private final List<AbstractWidget> partyMembersWidgets = new ArrayList<>();

    private PartyManagementScreen() {
        super(Component.literal("Party Management Screen"));
    }

    public static Screen create() {
        return new PartyManagementScreen();
    }

    @Override
    public void init() {
        refreshAll();
        // region Invite input and button
        this.addRenderableWidget(
                inviteInput = new TextInputBoxWidget(
                        this.width / 2 - X_START, this.height / 2 - 200, (TOTAL_WIDTH - (BUTTON_WIDTH / 2) - GAP), 20, null, this, inviteInput));

        this.addRenderableWidget(
                inviteButton = new Button.Builder(
                                Component.translatable("screens.wynntils.partyManagementGui.invite"),
                                (button) -> inviteFromField())
                        .pos(this.width / 2 - (X_START - TOTAL_WIDTH + 40), this.height / 2 - 200)
                        .size(BUTTON_WIDTH / 2, 20)
                        .build());
        // endregion

        // region Management button row (except create/leave)
        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.refreshButton")
                                .withStyle(ChatFormatting.GREEN),
                        (button) -> refreshAll())
                .pos(this.width / 2 - X_START, this.height / 2 - 176)
                .size(BUTTON_WIDTH, 20)
                .build());
        this.addRenderableWidget(
                kickOfflineButton = new Button.Builder(
                                Component.translatable("screens.wynntils.partyManagementGui.kickOfflineButton")
                                        .withStyle(ChatFormatting.RED),
                                (button) -> Models.Party.partyKickOffline())
                        .pos(this.width / 2 - (X_START - (BUTTON_WIDTH + GAP)), this.height / 2 - 176)
                        .size(BUTTON_WIDTH, 20)
                        .build());
        // endregion
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);

        boolean inParty = Models.Party.isInParty();

        // Update button states before rendering them
        createLeaveButton.render(poseStack, mouseX, mouseY, partialTick);
        kickOfflineButton.active = inParty
                && !Models.Party.getOfflineMembers().isEmpty()
                && Models.Party.getPartyLeader()
                        .equals(McUtils.player().getName().getString());
        inviteButton.active = !inviteInput
                .getTextBoxInput()
                .isBlank(); // inParty check not required as button automatically makes new party if not in one

        super.render(poseStack, mouseX, mouseY, partialTick);

        // region Invite field header
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.partyManagementGui.inviteFieldHeader1"),
                        this.width / 2 - X_START,
                        this.height / 2 - 206,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.partyManagementGui.inviteFieldHeader2"),
                        this.width / 2 - X_START + 77,
                        this.height / 2 - 206,
                        CommonColors.LIGHT_GRAY,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        // endregion

        // region Party list headers
        RenderUtils.drawRect(
                poseStack, CommonColors.WHITE, this.width / 2 - X_START, this.height / 2 - 140, 0, TOTAL_WIDTH, 1);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.partyManagementGui.head"),
                        this.width / 2 - X_START,
                        this.height / 2 - 144,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.partyManagementGui.name"),
                        this.width / 2 - X_START + 40,
                        this.height / 2 - 144,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.partyManagementGui.promote"),
                        this.width / 2 - X_START + 153,
                        this.height / 2 - 144,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.partyManagementGui.kick"),
                        this.width / 2 - X_START + 216,
                        this.height / 2 - 144,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        // endregion

        // region Party list
        partyMembersWidgets.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));
        // endregion

        // region Suggestion list headers
        RenderUtils.drawRect(
                poseStack, CommonColors.WHITE, this.width / 2 + 200, this.height / 2 - 140, 0, 172, 1);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.partyManagementGui.head"),
                        this.width / 2 + 200,
                        this.height / 2 - 144,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.partyManagementGui.suggestions"),
                        this.width / 2 + 240,
                        this.height / 2 - 144,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.partyManagementGui.invite"),
                        this.width / 2 + 340,
                        this.height / 2 - 144,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        // endregion

        // region Suggestion list
        suggestedPlayersWidgets.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));
        // endregion

        // region Legend
        RenderUtils.drawRect(poseStack, CommonColors.WHITE, this.width / 2 - 300, this.height / 2 - 140, 0, 50, 1);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.partyManagementGui.legend"),
                        this.width / 2 - 300,
                        this.height / 2 - 144,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        ChatFormatting.BOLD + I18n.get("screens.wynntils.partyManagementGui.self"),
                        this.width / 2 - 300,
                        this.height / 2 - 132,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.partyManagementGui.leader"),
                        this.width / 2 - 300,
                        this.height / 2 - 120,
                        CommonColors.YELLOW,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        ChatFormatting.STRIKETHROUGH + I18n.get("screens.wynntils.partyManagementGui.offline"),
                        this.width / 2 - 300,
                        this.height / 2 - 108,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.partyManagementGui.friend"),
                        this.width / 2 - 300,
                        this.height / 2 - 96,
                        CommonColors.GREEN,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Middle,
                        TextShadow.NORMAL);
        // endregion
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (createLeaveButton.isMouseOver(mouseX, mouseY)) {
            return createLeaveButton.mouseClicked(mouseX, mouseY, button);
        }

        for (AbstractWidget widget : partyMembersWidgets) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                return widget.mouseClicked(mouseX, mouseY, button);
            }
        }

        for (AbstractWidget widget : suggestedPlayersWidgets) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                return widget.mouseClicked(mouseX, mouseY, button);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return (focusedTextInput != null && focusedTextInput.charTyped(codePoint, modifiers))
                || super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return (focusedTextInput != null && focusedTextInput.keyPressed(keyCode, scanCode, modifiers))
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return focusedTextInput;
    }

    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {
        this.focusedTextInput = focusedTextInput;
    }

    public void reloadCreateLeaveButton() {
        createLeaveButton = new CreateLeaveButton(this.width / 2 - (X_START - (2 * (BUTTON_WIDTH + GAP))), this.height / 2 - 176, BUTTON_WIDTH, 20);
    }

    /**
     * Reloads the suggested players and their widgets
     * <p>
     * This should be called when a friend joins/leaves the world or when the refresh button is pressed
     */
    public void reloadSuggestedPlayersWidgets() {
        // Add friends that are online in the same world as user
        List<String> onlineUsers =
                new ArrayList<>(McUtils.mc().level.getScoreboard().getTeamNames());
        // Remove non-friends as we do not want to suggest them
        onlineUsers.removeIf(user -> !Models.Friends.getFriends().stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toList()
                .contains(user.toLowerCase(Locale.ROOT)));

        List<String> suggestedPlayers = new ArrayList<>(onlineUsers);
        suggestedPlayers.removeAll(Models.Party.getPartyMembers()); // No need to suggest party members
        suggestedPlayers.sort(String.CASE_INSENSITIVE_ORDER);

        suggestedPlayersWidgets.clear();
        for (int i = 0; i < suggestedPlayers.size(); i++) {
            String playerName = suggestedPlayers.get(i);
            if (playerName == null) continue;

            suggestedPlayersWidgets.add(new SuggestionPlayerWidget(
                    this.width / 2 + 204, this.height / 2 - 125 + i * 20 - 10, 172, 20, playerName));
        }
    }

    /**
     * Reloads the list of party members widgets
     * <p>
     * This should be called when the party list is updated or when the refresh button is pressed
     */
    public void reloadMembersWidgets() {
        partyMembersWidgets.clear();
        List<String> partyMembers = new ArrayList<>(Models.Party.getPartyMembers());
        for (int i = 0; i < partyMembers.size(); i++) {
            String playerName = partyMembers.get(i);
            if (playerName == null) continue;

            partyMembersWidgets.add(new PartyMemberWidget(
                    this.width / 2 - X_START + 4,
                    this.height / 2 - 125 + i * 20 - 10,
                    TOTAL_WIDTH,
                    20,
                    playerName,
                    Models.Party.getOfflineMembers().contains(playerName)));
        }
    }

    private void inviteFromField() {
        // Remove all except commas, semicolons, whitespaces, and characters possible in name
        String fieldText =
                INVITE_REPLACER.matcher(inviteInput.getTextBoxInput()).replaceAll("");
        fieldText = COMMA_REPLACER.matcher(fieldText).replaceAll(","); // semicolons and spaces to comma
        if (fieldText.isBlank()) return;

        Set<String> toInvite = new HashSet<>(List.of(fieldText.split(",")));
        toInvite.removeAll(Models.Party.getPartyMembers());
        toInvite.forEach(Models.Party::partyInvite);

        inviteInput.setTextBoxInput("");
    }

    private void refreshAll() {
        Models.Party.requestData();
        Models.Friends.requestData();
        reloadCreateLeaveButton();
        reloadMembersWidgets();
        reloadSuggestedPlayersWidgets();
    }
}
