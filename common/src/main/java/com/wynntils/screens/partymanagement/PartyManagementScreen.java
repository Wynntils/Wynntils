package com.wynntils.screens.partymanagement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.partymanagement.widgets.SuggestionPlayer;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class PartyManagementScreen extends Screen implements TextboxScreen {
    private static final Pattern INVITE_REPLACER = Pattern.compile("[^\\w, ]+");
    private static final Pattern COMMA_REPLACER = Pattern.compile("[,; ]+");

    private TextInputBoxWidget focusedTextInput;

    private TextInputBoxWidget inviteInput;

    private Button inviteButton;
    private Button kickOfflineButton;
    private Button createPartyButton;
    private Button leavePartyButton;
    private final List<Button> promoteButtons = new ArrayList<>();
    private final List<Button> kickButtons = new ArrayList<>();
    private final List<Button> inviteButtons = new ArrayList<>();

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

        // region Invite field header
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.inviteFieldHeader1"),
                this.width / 2 - xStart,
                this.height / 2 - 206,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.inviteFieldHeader2"),
                this.width / 2 - xStart + 77,
                this.height / 2 - 206,
                CommonColors.LIGHT_GRAY,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        // endregion

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
                this.width / 2 - xStart + 40,
                this.height / 2 - 144,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.promote"),
                this.width / 2 - xStart + 249,
                this.height / 2 - 144,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
                poseStack,
                I18n.get("screens.wynntils.partyManagementGui.kick"),
                this.width / 2 - xStart + 312,
                this.height / 2 - 144,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        // endregion
        // region Party list
        this.renderables.removeAll(promoteButtons);
        this.renderables.removeAll(kickButtons);
        this.children.removeAll(promoteButtons);
        this.children.removeAll(kickButtons);
        promoteButtons.clear();
        kickButtons.clear();
        List<String> partyMembers = new ArrayList<>(Models.Party.getPartyMembers());
        for (int i = 0; i < partyMembers.size(); i++) {
            String playerName = partyMembers.get(i);
            if (playerName == null) continue;

            ResourceLocation skin = McUtils.mc().getConnection().getPlayerInfo(playerName).getSkinLocation();
            // head rendering
            RenderUtils.drawTexturedRect(
                    poseStack,
                    skin,
                    this.width / 2 - xStart + 4,
                    this.height / 2 - 125 + i * 20 - 10,
                    8,
                    16,
                    16,
                    8,
                    8,
                    8,
                    8,
                    64,
                    64);
            // hat rendering
            RenderUtils.drawTexturedRect(
                    poseStack,
                    skin,
                    this.width / 2 - xStart + 4,
                    this.height / 2 - 125 + i * 20 - 10,
                    1,
                    16,
                    16,
                    40,
                    8,
                    8,
                    8,
                    64,
                    64);

            // name rendering
            CustomColor color = playerName.equals(Models.Party.getPartyLeader()) ? CommonColors.YELLOW : (Models.Friends.getFriends().contains(playerName) ? CommonColors.GREEN : CommonColors.WHITE);
            String prefix = offlineMembers.contains(playerName) ? "§m" : (playerName.equals(McUtils.player().getName().getString()) ? "§l" : "");

            String formattedPlayerName = prefix + playerName;

            fr.renderText(
                    poseStack,
                    formattedPlayerName,
                    this.width / 2 - xStart + 40,
                    this.height / 2 - 125 + i * 20,
                    color,
                    HorizontalAlignment.Left,
                    VerticalAlignment.Middle,
                    TextShadow.NORMAL);

            if (!McUtils.player().getName().getString().equals(Models.Party.getPartyLeader())) continue; // only leader can promote/kick

            if (playerName.equals(Models.Party.getPartyLeader())) {
                kickButtons.add(new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.disband"),
                        (button) -> disbandParty())
                        .pos(this.width / 2 - xStart + 296, this.height / 2 - 125 + i * 20 - 10)
                        .size(50, 20)
                        .build());
                continue;
            }

            // Promote button
            promoteButtons.add(new Button.Builder(
                    Component.translatable("screens.wynntils.partyManagementGui.promote"),
                    (button) -> promoteToLeader(playerName))
                    .pos(this.width / 2 - xStart + 244, this.height / 2 - 125 + i * 20 - 10)
                    .size(50, 20)
                    .build());

            // Kick button
            kickButtons.add(new Button.Builder(
                    Component.translatable("screens.wynntils.partyManagementGui.kick"),
                    (button) -> kickFromParty(playerName))
                    .pos(this.width / 2 - xStart + 296, this.height / 2 - 125 + i * 20 - 10)
                    .size(50, 20)
                    .build());
        }
        this.renderables.addAll(promoteButtons);
        this.renderables.addAll(kickButtons);
        this.children.addAll(promoteButtons);
        this.children.addAll(kickButtons);
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
                this.width / 2 + 240,
                this.height / 2 - 144,
                CommonColors.WHITE,
                HorizontalAlignment.Left,
                VerticalAlignment.Middle,
                TextShadow.NORMAL);
        fr.renderText(
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
        this.renderables.removeAll(inviteButtons);
        this.children.removeAll(inviteButtons);
        inviteButtons.clear();
        for (int i = 0; i < suggestedPlayers.size(); i++) {
            String playerName = suggestedPlayers.get(i);
            if (playerName == null) continue;

            this.addRenderableWidget(new SuggestionPlayer(this.width / 2 + 204, this.height / 2 - 125 + i * 20 - 10, 200, 20, playerName, i));

            ResourceLocation skin = McUtils.mc().getConnection().getPlayerInfo(playerName).getSkinLocation();
            // head rendering
            RenderUtils.drawTexturedRect(
                    poseStack,
                    skin,
                    this.width / 2 + 204,
                    this.height / 2 - 125 + i * 20 - 10,
                    8,
                    16,
                    16,
                    8,
                    8,
                    8,
                    8,
                    64,
                    64);
            RenderUtils.drawTexturedRect(
                    poseStack,
                    skin,
                    this.width / 2 + 204,
                    this.height / 2 - 125 + i * 20 - 10,
                    8,
                    16,
                    16,
                    40,
                    8,
                    8,
                    8,
                    64,
                    64);

            // name rendering
            fr.renderText(
                    poseStack,
                    playerName,
                    this.width / 2 + 240,
                    this.height / 2 - 125 + i * 20,
                    CommonColors.GREEN,
                    HorizontalAlignment.Left,
                    VerticalAlignment.Middle,
                    TextShadow.NORMAL);

            // Invite button
            inviteButtons.add(new Button.Builder(
                    Component.translatable("screens.wynntils.partyManagementGui.invite"),
                    (button) -> inviteToParty(playerName))
                    .pos(this.width / 2 + 334, this.height / 2 - 125 + i * 20 - 10)
                    .size(40, 20)
                    .build());
        }
        this.renderables.addAll(inviteButtons);
        this.children.addAll(inviteButtons);
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
        toInvite.forEach(this::inviteToParty);

        inviteInput.setTextBoxInput("");
    }

    private void refreshParty() {
        Models.Party.requestPartyListUpdate();
    }

    private void kickOffline() {
        refreshParty();
        offlineMembers.addAll(Models.Party.getPartyMembers());
        offlineMembers.removeAll(McUtils.mc().level.getScoreboard().getTeamNames());
        offlineMembers.forEach(this::kickFromParty);
    }

    private void createParty() {
        McUtils.sendCommand("party create");
    }

    private void leaveParty() {
        McUtils.sendCommand("party leave");
    }

    private void disbandParty() {
        McUtils.sendCommand("party disband");
    }

    private void promoteToLeader(String playerName) {
        McUtils.sendCommand("party promote " + playerName);
    }

    private void kickFromParty(String playerName) {
        McUtils.sendCommand("party kick " + playerName);
    }

    private void inviteToParty(String playerName) {
        McUtils.sendCommand("party invite " + playerName);
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
