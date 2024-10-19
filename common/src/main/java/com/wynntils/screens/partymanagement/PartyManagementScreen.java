/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.partymanagement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.WynntilsGridLayoutScreen;
import com.wynntils.screens.base.widgets.InfoButton;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.partymanagement.widgets.CreateLeaveButton;
import com.wynntils.screens.partymanagement.widgets.PartyMemberWidget;
import com.wynntils.screens.partymanagement.widgets.SuggestionPlayerWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class PartyManagementScreen extends WynntilsGridLayoutScreen {
    private static final Pattern INVITE_REPLACER = Pattern.compile("[^\\w, ]+");
    private static final Pattern COMMA_REPLACER = Pattern.compile("[,; ]+");

    private static final int START_HEIGHT = 8;
    private static final int PARTY_LIST_DIV_HEIGHT = 14;
    private static final int SUGGESTION_LIST_DIV_HEIGHT = 22;
    private static final int MGMT_ROW_DIV_HEIGHT = 14;
    private int mgmtButtonWidth;
    private TextInputBoxWidget inviteInput;
    private Button inviteButton;
    private Button kickOfflineButton;
    private CreateLeaveButton createLeaveButton;
    private List<SuggestionPlayerWidget> suggestedPlayersWidgets = new ArrayList<>();
    private List<PartyMemberWidget> partyMembersWidgets = new ArrayList<>();

    private PartyManagementScreen() {
        super(Component.literal("Party Management Screen"));
    }

    public static Screen create() {
        return new PartyManagementScreen();
    }

    @Override
    public void doInit() {
        super.doInit();
        mgmtButtonWidth = (int) (dividedWidth * 8) - 2; // -2 for spacing
        refreshAll();

        // region Invite input and button
        inviteInput = new TextInputBoxWidget(
                (int) (dividedWidth * 36),
                (int) (dividedHeight * START_HEIGHT) + 1,
                (int) ((dividedWidth * 57) - (dividedWidth * 36)) - 1,
                BUTTON_SIZE,
                null,
                this,
                inviteInput);
        this.addRenderableWidget(inviteInput);

        inviteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.invite"),
                        (button) -> inviteFromField())
                .pos((int) (dividedWidth * 57) + 1, (int) (dividedHeight * START_HEIGHT) + 1)
                .size((int) (dividedWidth * 3) - 1, BUTTON_SIZE)
                .build();
        this.addRenderableWidget(inviteButton);
        // endregion

        // region Management button row (except create/leave)
        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.refreshButton")
                                .withStyle(ChatFormatting.GREEN),
                        (button) -> refreshAll())
                .pos((int) (dividedWidth * 36) + 1, (int) (dividedHeight * MGMT_ROW_DIV_HEIGHT))
                .size(mgmtButtonWidth, BUTTON_SIZE)
                .build());

        kickOfflineButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.kickOfflineButton")
                                .withStyle(ChatFormatting.RED),
                        (button) -> Models.Party.partyKickOffline())
                .pos((int) (dividedWidth * 44) + 1, (int) (dividedHeight * MGMT_ROW_DIV_HEIGHT))
                .size(mgmtButtonWidth, BUTTON_SIZE)
                .build();
        this.addRenderableWidget(kickOfflineButton);
        // endregion

        // region info button
        this.addRenderableWidget(new InfoButton(
                (int) (dividedWidth * 2),
                (int) (dividedHeight * 58),
                Component.literal("")
                        .append(Component.translatable("screens.wynntils.partyManagementGui.legend")
                                .withStyle(ChatFormatting.UNDERLINE))
                        .append(Component.literal("\n"))
                        .append(Component.translatable("screens.wynntils.partyManagementGui.self")
                                .withStyle(ChatFormatting.BOLD))
                        .append(Component.literal("\n"))
                        .append(Component.translatable("screens.wynntils.partyManagementGui.leader")
                                .withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal("\n"))
                        .append(Component.translatable("screens.wynntils.partyManagementGui.offline")
                                .withStyle(ChatFormatting.STRIKETHROUGH))
                        .append(Component.literal("\n"))
                        .append(Component.translatable("screens.wynntils.partyManagementGui.friend")
                                .withStyle(ChatFormatting.GREEN))));
        // endregion
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        PoseStack poseStack = guiGraphics.pose();

        boolean inParty = Models.Party.isInParty();

        // Update button states before rendering them
        createLeaveButton.render(guiGraphics, mouseX, mouseY, partialTick);
        kickOfflineButton.active = inParty
                && !Models.Party.getOfflineMembers().isEmpty()
                && Models.Party.isPartyLeader(McUtils.playerName());
        inviteButton.active = !inviteInput
                .getTextBoxInput()
                .isBlank(); // inParty check not required as button automatically makes new party if not in one

        // region Invite field header
        String inviteFieldHeader = I18n.get("screens.wynntils.partyManagementGui.inviteFieldHeader1")
                + ChatFormatting.GRAY
                + I18n.get("screens.wynntils.partyManagementGui.inviteFieldHeader2");

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(inviteFieldHeader),
                        dividedWidth * 36,
                        dividedWidth * 60,
                        dividedHeight * START_HEIGHT
                                - FontRenderer.getInstance()
                                        .calculateRenderHeight(
                                                StyledText.fromString(inviteFieldHeader), dividedWidth * 24),
                        dividedHeight * START_HEIGHT, // should be lined up with the party member count header
                        dividedWidth * 24,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
        // endregion

        // region Party list
        if (inParty) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get(
                                    "screens.wynntils.partyManagementGui.members",
                                    Models.Party.getPartyMembers().size(),
                                    Models.Party.MAX_PARTY_MEMBER_COUNT)),
                            dividedWidth * 4,
                            dividedHeight * START_HEIGHT,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NORMAL,
                            2);
            RenderUtils.drawRect(
                    poseStack,
                    CommonColors.WHITE,
                    dividedWidth * 4,
                    dividedHeight * PARTY_LIST_DIV_HEIGHT,
                    0,
                    dividedWidth * 30 - dividedWidth * 2,
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
                            dividedWidth * 30,
                            dividedHeight * PARTY_LIST_DIV_HEIGHT,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);

            partyMembersWidgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
        } else {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.partyManagementGui.notInParty")),
                            dividedWidth * 4,
                            dividedWidth * 30,
                            dividedHeight * PARTY_LIST_DIV_HEIGHT,
                            height - (dividedHeight * PARTY_LIST_DIV_HEIGHT),
                            dividedWidth * 30 - dividedWidth * 4,
                            CustomColor.NONE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            2);
        }
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

        suggestedPlayersWidgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
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

    public void reloadCreateLeaveButton() {
        createLeaveButton = new CreateLeaveButton(
                (int) (dividedWidth * 52) + 1,
                (int) (dividedHeight * MGMT_ROW_DIV_HEIGHT),
                mgmtButtonWidth,
                BUTTON_SIZE);
    }

    /**
     * Reloads the list of party members widgets
     * <p>
     * This should be called when the party list is updated or when the refresh button is pressed
     */
    public void reloadMembersWidgets() {
        partyMembersWidgets = new ArrayList<>();
        List<String> partyMembers = new ArrayList<>(Models.Party.getPartyMembers());
        for (int i = 0; i < partyMembers.size(); i++) {
            String playerName = partyMembers.get(i);
            if (playerName == null) continue;

            partyMembersWidgets.add(new PartyMemberWidget(
                    dividedWidth * 4,
                    dividedHeight * (PARTY_LIST_DIV_HEIGHT + 2) + i * BUTTON_SIZE,
                    (int) (dividedWidth * 28) - (int) (dividedWidth * 2),
                    BUTTON_SIZE,
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
        List<String> suggestedPlayers =
                new ArrayList<>(Models.Friends.getOnlineFriends().keySet());
        suggestedPlayers.removeAll(Models.Party.getPartyMembers()); // No need to suggest party members
        suggestedPlayers.sort(String.CASE_INSENSITIVE_ORDER);

        suggestedPlayersWidgets = new ArrayList<>();
        for (int i = 0; i < suggestedPlayers.size(); i++) {
            String playerName = suggestedPlayers.get(i);
            boolean isOffline = !Models.Friends.getOnlineFriends().containsKey(playerName);
            if (playerName == null) continue;

            suggestedPlayersWidgets.add(new SuggestionPlayerWidget(
                    dividedWidth * 36,
                    dividedHeight * (23 + i * 3),
                    (int) ((dividedWidth * 60) - (dividedWidth * 36)),
                    BUTTON_SIZE,
                    playerName,
                    isOffline,
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
}
