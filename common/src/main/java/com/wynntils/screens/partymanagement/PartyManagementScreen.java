/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.partymanagement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.partymanagement.widgets.CreateLeaveButton;
import com.wynntils.screens.partymanagement.widgets.LegendButton;
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

public final class PartyManagementScreen extends WynntilsScreen implements TextboxScreen {
    private static final Pattern INVITE_REPLACER = Pattern.compile("[^\\w, ]+");
    private static final Pattern COMMA_REPLACER = Pattern.compile("[,; ]+");

    private static final float GRID_DIVISIONS = 64.0f;
    private static final int PARTY_LIST_DIV_HEIGHT = 8;
    private static final int SUGGESTION_LIST_DIV_HEIGHT = 22;
    private static final int MGMT_ROW_DIV_HEIGHT = 14;

    private float dividedHeight;
    private float dividedWidth;
    private int mgmtButtonWidth;

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

    /*
    Some notes on element alignment and sizing:
    All elements should be positioned dynamically relative to window size. That is; you must use this.height and
    this.width, plus any multiplier and optionally, +/- 1 to make small tweaks. For example, these are valid:
    (int) (dividedWidth * 36)
    (int) (dividedWidth * 57) - 1
    (this.width / 2)
    (end of another element) + x
    And these are invalid:
    100
    (int) (dividedWidth * 36) + 4
    (int) (this.width / 2 + 199)
    The reason for all of this is gui scale. Height and width will be different depending on the gui scale, and
    the above method will ensure that the gui is always aligned somewhat correctly.

    Also note that button height (and related elements), must be 20 or the texture will break.
    Element sizing isn't nearly as picky as alignment, but it'll still be better to keep static modifiers (eg. +2) to a
    minimum.
     */

    @Override
    public void doInit() {
        dividedWidth = this.width / GRID_DIVISIONS;
        dividedHeight = this.height / GRID_DIVISIONS;
        mgmtButtonWidth = (int) (dividedWidth * 8) - 2; // -2 for spacing
        refreshAll();
        // region Invite input and button
        inviteInput = new TextInputBoxWidget(
                (int) (dividedWidth * 36),
                (int) (dividedHeight * PARTY_LIST_DIV_HEIGHT) + 1,
                (int) ((dividedWidth * 57) - (dividedWidth * 36)) - 1,
                20, // height is static 20 to match the button below
                null,
                this,
                inviteInput);
        this.addRenderableWidget(inviteInput);

        inviteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.invite"),
                        (button) -> inviteFromField())
                .pos((int) (dividedWidth * 57) + 1, (int) (dividedHeight * PARTY_LIST_DIV_HEIGHT) + 1)
                .size((int) (dividedWidth * 3) - 1, 20) // height must be static 20 or the texture will break
                .build();
        this.addRenderableWidget(inviteButton);
        // endregion

        // region Management button row (except create/leave)
        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.refreshButton")
                                .withStyle(ChatFormatting.GREEN),
                        (button) -> refreshAll())
                .pos((int) (dividedWidth * 36) + 1, (int) (dividedHeight * MGMT_ROW_DIV_HEIGHT))
                .size(mgmtButtonWidth, 20)
                .build());

        kickOfflineButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.kickOfflineButton")
                                .withStyle(ChatFormatting.RED),
                        (button) -> Models.Party.partyKickOffline())
                .pos((int) (dividedWidth * 44) + 1, (int) (dividedHeight * MGMT_ROW_DIV_HEIGHT))
                .size(mgmtButtonWidth, 20)
                .build();
        this.addRenderableWidget(kickOfflineButton);
        // endregion

        this.addRenderableWidget(new LegendButton((int) (dividedWidth * 2), (int) (dividedHeight * 58)));
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        dividedWidth = this.width / GRID_DIVISIONS;
        dividedHeight = this.height / GRID_DIVISIONS;
        renderBackground(poseStack);

        boolean inParty = Models.Party.isInParty();

        // Update button states before rendering them
        createLeaveButton.render(poseStack, mouseX, mouseY, partialTick);
        kickOfflineButton.active = inParty
                && !Models.Party.getOfflineMembers().isEmpty()
                && Models.Party.isPartyLeader(McUtils.playerName());
        inviteButton.active = !inviteInput
                .getTextBoxInput()
                .isBlank(); // inParty check not required as button automatically makes new party if not in one

        super.doRender(poseStack, mouseX, mouseY, partialTick);

        // uncomment when changing gui elements
        // renderDebugGrid(poseStack);

        // region Invite field header
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        // Yes this is kind of abusive of the formatting system, and I should probably do
                        // another .renderText call, but this makes aligning these two texts significantly easier
                        // (especially with changing gui scales and resolutions)
                        StyledText.fromString(I18n.get("screens.wynntils.partyManagementGui.inviteFieldHeader1")
                                + ChatFormatting.GRAY
                                + I18n.get("screens.wynntils.partyManagementGui.inviteFieldHeader2")),
                        dividedWidth * 36,
                        dividedHeight * PARTY_LIST_DIV_HEIGHT, // should be lined up with the party list header
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        // endregion

        // region Party list
        // if you enable the debug dividers, we want to render this on the 4nd line from the left (this.width / 64 * 4)
        // and the 8th line from the top (this.height / 64 * 8)
        RenderUtils.drawRect(
                poseStack,
                CommonColors.WHITE,
                dividedWidth * 4,
                dividedHeight * PARTY_LIST_DIV_HEIGHT,
                0,
                dividedWidth * 30 - dividedWidth * 4,
                1);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.partyManagementGui.head")),
                        dividedWidth * 5,
                        dividedHeight * PARTY_LIST_DIV_HEIGHT,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.partyManagementGui.name")),
                        dividedWidth * 7,
                        dividedHeight * PARTY_LIST_DIV_HEIGHT,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.partyManagementGui.promote")),
                        dividedWidth * 22,
                        dividedHeight * PARTY_LIST_DIV_HEIGHT,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER, // (!) center as the button spans 2 columns
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.partyManagementGui.kick")),
                        dividedWidth * 26,
                        dividedHeight * PARTY_LIST_DIV_HEIGHT,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER, // (!) center as the button spans 2 columns
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.partyManagementGui.priority")),
                        dividedWidth * 28,
                        dividedHeight * PARTY_LIST_DIV_HEIGHT,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        partyMembersWidgets.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));
        // endregion

        // region Suggestions
        RenderUtils.drawRect(
                poseStack,
                CommonColors.WHITE,
                dividedWidth * 36,
                dividedHeight * SUGGESTION_LIST_DIV_HEIGHT,
                0,
                dividedWidth * 60 - dividedWidth * 36,
                1);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.partyManagementGui.head")),
                        dividedWidth * 37,
                        dividedHeight * SUGGESTION_LIST_DIV_HEIGHT,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.partyManagementGui.suggestions")),
                        dividedWidth * 39,
                        dividedHeight * SUGGESTION_LIST_DIV_HEIGHT,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.partyManagementGui.invite")),
                        dividedWidth * 58,
                        dividedHeight * SUGGESTION_LIST_DIV_HEIGHT,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER, // (!) center as the button spans 2 columns
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        suggestedPlayersWidgets.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));
        // endregion
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
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

        return super.doMouseClicked(mouseX, mouseY, button);
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
        createLeaveButton = new CreateLeaveButton(
                (int) (dividedWidth * 52) + 1, (int) (dividedHeight * MGMT_ROW_DIV_HEIGHT), mgmtButtonWidth, 20);
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
                    dividedWidth * 4,
                    dividedHeight * (9 + i * 3),
                    (int) (dividedWidth * 28) - (int) (dividedWidth * 2),
                    20,
                    playerName,
                    Models.Party.getOfflineMembers().contains(playerName),
                    28 - 2));
        }
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
                    dividedWidth * 36,
                    dividedHeight * (23 + i * 3),
                    (int) ((dividedWidth * 60) - (dividedWidth * 36)),
                    20,
                    playerName,
                    60 - 36));
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

    /**
     * To make positioning easier, we will split the screen into GRID_DIVISIONS parts horizontally and vertically
     * This renders the screen split properly and also renders the division numbers
     */
    private void renderDebugGrid(PoseStack poseStack) {
        for (int i = 1; i <= GRID_DIVISIONS - 1; i++) {
            double x = dividedWidth * i;
            double y = dividedHeight * i;
            RenderUtils.drawRect(poseStack, CommonColors.GRAY, (float) x, 0, 0, 1, this.height);
            RenderUtils.drawRect(poseStack, CommonColors.GRAY, 0, (float) y, 0, this.width, 1);
            if (i % 2 == 0) continue; // reduce clutter
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(String.valueOf(i)),
                            (float) x,
                            this.height / 2,
                            CommonColors.RED,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(String.valueOf(i)),
                            this.width / 2,
                            (float) y,
                            CommonColors.CYAN,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }
    }
}
