package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sun.source.tree.Tree;
import com.wynntils.core.components.Models;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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

    private final int totalWidth = 344;
    private final int xStart = totalWidth / 2;

    private final Set<String> offlineMembers = new HashSet<>();
    private final List<String> suggestedPlayers = new ArrayList<>();

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

    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        boolean partying = Models.Party.isPartying();

        createPartyButton.active = !partying;
        leavePartyButton.active = partying;
        kickOfflineButton.active = partying && !offlineMembers.isEmpty();
        inviteButton.active = !inviteInput.getTextBoxInput().isBlank(); // partying check not required as button automatically makes new party if not in one

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
        // region Party list
        List<String> partyMembers = new ArrayList<>(Models.Party.getPartyMembers());
        for (int i = 0; i < partyMembers.size(); i++) {
            String playerName = partyMembers.get(i);
            if (playerName == null) continue;

            CustomColor color;
            String prefix = "";

            if (playerName.equals(Models.Party.getPartyLeader())) {
                color = CommonColors.YELLOW;
            } else if (Models.Friends.getFriends().contains(playerName)) {
                color = CommonColors.GREEN;
            } else {
                color = CommonColors.WHITE;
            }

            if (offlineMembers.contains(playerName)) {
                prefix = "§m";
            } else if (playerName.equals(McUtils.player().getName().getString())) {
                prefix = "§l";
            }

            String formattedPlayerName = prefix + playerName;

            fr.renderText(
                    poseStack,
                    formattedPlayerName,
                    this.width / 2 - xStart + 50,
                    this.height / 2 - 120 + i * 20,
                    color,
                    HorizontalAlignment.Left,
                    VerticalAlignment.Middle,
                    TextShadow.NORMAL);
        }
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
        RenderUtils.drawRect(poseStack, CommonColors.WHITE, this.width / 2 - 300, this.height / 2 - 140, 0, 50, 1);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.legend"),
                this.width / 2 - 300,
                this.height / 2 - 144,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
                poseStack,
                "§l" + I18n.get("screens.wynntils.partyManagementGui.self"),
                this.width / 2 - 300,
                this.height / 2 - 132,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.leader"),
                this.width / 2 - 300,
                this.height / 2 - 120,
                CommonColors.YELLOW,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
                poseStack,
                "§m" + I18n.get("screens.wynntils.partyManagementGui.offline"),
                this.width / 2 - 300,
                this.height / 2 - 108,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
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

    private void inviteFromField() {
        // Remove all except commas, semicolons, whitespaces, and characters possible in name
        String fieldText = INVITE_REPLACER.matcher(inviteInput.getTextBoxInput()).replaceAll("");
        fieldText = COMMA_REPLACER.matcher(fieldText).replaceAll(","); // semicolons and spaces to comma
        if (fieldText.isBlank()) return;

        if (!Models.Party.isPartying()) {
            McUtils.sendCommand("party create");
        }

        Set<String> toInvite = new HashSet<>(List.of(fieldText.split(",")));
        toInvite.removeAll(Models.Party.getPartyMembers());
        toInvite.forEach(member -> McUtils.sendCommand("party invite " + member));

        inviteInput.setTextBoxInput("");
    }

    private void refreshParty() {
        Models.Party.requestPartyListUpdate();
    }

    private void kickOffline() {
        refreshParty();
        offlineMembers.addAll(Models.Party.getPartyMembers());
        offlineMembers.removeAll(McUtils.mc().level.getScoreboard().getTeamNames());
        offlineMembers.forEach(member -> McUtils.sendCommand("party kick " + member));
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
        onlineUsers.retainAll(Models.Friends.getFriends());

        suggestedPlayers.addAll(onlineUsers);
        suggestedPlayers.removeAll(Models.Party.getPartyMembers()); // No need to suggest party members
        suggestedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
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
