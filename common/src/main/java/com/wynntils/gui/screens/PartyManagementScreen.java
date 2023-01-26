package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class PartyManagementScreen extends Screen implements TextboxScreen {
    private static final Pattern INVITE_REPLACER = Pattern.compile("[^\\w,; ]+");
    private static final Pattern COMMA_REPLACER = Pattern.compile("[,; ]+");
    private TextInputBoxWidget focusedTextInput;

    private TextInputBoxWidget inviteInput;

    private Button inviteButton;
    private Button kickOfflineButton;
    private Button createPartyButton;
    private Button leavePartyButton;

    private int totalWidth = 344;
    private int xStart = totalWidth / 2;

    private final Set<String> partyMembers = new HashSet<>();
    private final Set<String> offlineMembers = new HashSet<>();
    private final List<String> suggestedPlayers = new ArrayList<>();
    private Pair<HashSet<String>, String> unsortedPartyMembers = new Pair<>(new HashSet<>(), "");
    private boolean partyLeaveSent = false;

    private PartyManagementScreen() {
        super(Component.literal("Party Management Screen"));
    }

    public static Screen create() {
        return new PartyManagementScreen();
    }

    @Override
    protected void init() {
        // region Invite input and button
        this.addRenderableWidget(
                inviteInput = new TextInputBoxWidget(this.width / 2 - xStart, this.height / 2 - 200, 300, 20, null, this, inviteInput));

        this.addRenderableWidget(
                inviteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.invite"),
                (button) -> inviteFromField())
                        .pos(this.width / 2 - (xStart - totalWidth + 40), this.height / 2 - 200)
                        .size(40, 20)
                        .build());

        inviteButton.active = false;
        // endregion

        // region Management button row
        this.addRenderableWidget(
                new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.refreshButton").withStyle(ChatFormatting.GREEN),
                (button) -> refreshParty())
                        .pos(this.width / 2 - xStart, this.height / 2 - 176)
                        .size(83, 20)
                        .build());
        this.addRenderableWidget(
                kickOfflineButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.kickOfflineButton").withStyle(ChatFormatting.RED),
                (button) -> kickOffline())
                        .pos(this.width / 2 - (xStart - 87), this.height / 2 - 176)
                        .size(83, 20)
                        .build());
        this.addRenderableWidget(
                createPartyButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.createPartyButton"),
                (button) -> createParty())
                        .pos(this.width / 2 - (xStart - 174), this.height / 2 - 176)
                        .size(83, 20)
                        .build());
        this.addRenderableWidget(
                leavePartyButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.leavePartyButton").withStyle(ChatFormatting.RED),
                (button) -> leaveParty())
                        .pos(this.width / 2 - (xStart - 261), this.height / 2 - 176)
                        .size(83, 20)
                        .build());
        // endregion

        if (Models.PlayerRelations.isPartying()) {
            createPartyButton.active = false;
            leavePartyButton.active = true;
            if (offlineMembers.isEmpty()) {
                kickOfflineButton.active = false;
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        updateSuggestionsList();
        FontRenderer fr = FontRenderer.getInstance();

        // region Party list headers
        RenderUtils.drawRect(poseStack, CommonColors.WHITE, this.width / 2 - xStart, this.height / 2 - 140, 0, totalWidth, 1);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.head"),
                this.width / 2 - xStart,
                this.height / 2 - 144,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.name"),
                this.width / 2 - xStart + 50,
                this.height / 2 - 144,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.promote"),
                this.width / 2 - xStart + 260,
                this.height / 2 - 144,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.kick"),
                this.width / 2 - xStart + 322, // starts at 300-ish?, center 322, ends at 344-ish?
                this.height / 2 - 144,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        // endregion

        // region Suggestion list headers
        RenderUtils.drawRect(poseStack, CommonColors.WHITE, this.width / 2 + 200, this.height / 2 - 140, 0, totalWidth / 2, 1);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.head"),
                this.width / 2 + 200,
                this.height / 2 - 144,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.suggestions"),
                this.width / 2 + 280,
                this.height / 2 - 144,
                CommonColors.WHITE,
                HorizontalAlignment.Center,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.invite"),
                this.width / 2 + 350,
                this.height / 2 - 144,
                CommonColors.WHITE,
                HorizontalAlignment.Center,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        // endregion

        // region Legend
        RenderUtils.drawRect(poseStack, CommonColors.WHITE, this.width / 2 - 400, this.height / 2 - 140, 0, totalWidth / 3, 1);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.legend"),
                this.width / 2 - 400,
                this.height / 2 - 144,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.self"),
                this.width / 2 - 400,
                this.height / 2 - 132,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
    }

    private void inviteFromField() {
        // Remove all except commas, semicolons, whitespaces, and characters possible in name
        String fieldText = INVITE_REPLACER.matcher(inviteInput.getTextBoxInput()).replaceAll("");
        fieldText = COMMA_REPLACER.matcher(fieldText).replaceAll(","); // semicolons and spaces to comma
        if (fieldText.isBlank()) return;

        if (!Models.PlayerRelations.isPartying()) {
            McUtils.sendCommand("party create");
        }

        Set<String> toInvite = new HashSet<>(List.of(fieldText.split(",")));
        toInvite.removeAll(Models.PlayerRelations.getPartyMembers());
        toInvite.forEach(member -> McUtils.sendCommand("party invite " + member));

        inviteInput.setTextBoxInput("");
    }

    private void refreshParty() {
        Models.PlayerRelations.updateWorldPlayers();

        Models.PlayerRelations.requestPartyListUpdate();
        if (!Models.PlayerRelations.isPartying()) {
            partyMembers.clear();
        } else {
            partyMembers.addAll(Models.PlayerRelations.getPartyMembers());
        }
    }

    private void kickOffline() {
        Models.PlayerRelations.updateWorldPlayers();
        offlineMembers.addAll(Models.PlayerRelations.getPartyMembers());
        offlineMembers.removeAll(Models.PlayerRelations.getWorldPlayers());

    }

    private void createParty() {
        McUtils.sendCommand("party create");
    }

    private void leaveParty() {
        McUtils.sendCommand("party leave");
    }

    private void updateSuggestionsList() {
        suggestedPlayers.clear();
        // Add friends that are online in the same world as user

        Scoreboard scoreboard = McUtils.mc().level.getScoreboard();
        Set<String> onlineUsers = new HashSet<>(scoreboard.getTeamNames());
        onlineUsers.retainAll(Models.PlayerRelations.getFriends());

        suggestedPlayers.addAll(onlineUsers);
        suggestedPlayers.removeAll(partyMembers); // No need to suggest party members
        suggestedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        inviteButton.active = (!inviteInput.getTextBoxInput().isEmpty());
        return (focusedTextInput != null && focusedTextInput.charTyped(codePoint, modifiers)) || super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return (focusedTextInput != null && focusedTextInput.keyPressed(keyCode, scanCode, modifiers)) || super.keyPressed(keyCode, scanCode, modifiers);
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
