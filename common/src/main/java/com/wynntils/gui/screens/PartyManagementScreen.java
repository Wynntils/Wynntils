package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PartyManagementScreen extends Screen implements TextboxScreen {
    private TextInputBoxWidget focusedTextInput;

    private TextInputBoxWidget inviteInput;

    private Button inviteButton;
    private Button refreshButton;
    private Button kickOfflineButton;
    private Button createPartyButton;
    private Button leavePartyButton;

    private PartyManagementScreen() {
        super(Component.literal("Party Management Screen"));
    }

    public static Screen create() {
        return new PartyManagementScreen();
    }

    @Override
    protected void init() {
        // region Invite input and button
        int inviteInputWidth = 300;
        int inviteButtonWidth = 40;
        int inviteGap = 4;
        int inviteSum = inviteInputWidth + inviteButtonWidth + inviteGap;
        int xStart = inviteSum / 2;
        this.addRenderableWidget(
                inviteInput = new TextInputBoxWidget(this.width / 2 - xStart, this.height / 2 - 200, inviteInputWidth, 20, null, this, inviteInput));

        this.addRenderableWidget(
                inviteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.partyManagementGui.inviteButton"),
                (button) -> inviteFromField())
                        .pos(this.width / 2 - (xStart - inviteSum + inviteButtonWidth), this.height / 2 - 200)
                        .size(inviteButtonWidth, 20)
                        .build());

        inviteButton.active = false;
        // endregion

        // region Management button row
        this.addRenderableWidget(
                refreshButton = new Button.Builder(
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

        // region Party list headers
        RenderUtils.drawRect(poseStack, CustomColor.fromHexString("0xFFFFFF"), this.width / 2, this.height / 2, 0, 20, 20);

        // something about a fontrenderer
    }

    private void inviteFromField() {

        inviteInput.setTextBoxInput("");
    }

    private void refreshParty() {

    }

    private void kickOffline() {

    }

    private void createParty() {

    }

    private void leaveParty() {

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
