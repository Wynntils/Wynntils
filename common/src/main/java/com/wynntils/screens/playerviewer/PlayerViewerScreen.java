/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.playerviewer;

import com.google.gson.JsonObject;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.screens.base.WynntilsContainerScreen;
import com.wynntils.screens.playerviewer.widgets.FriendButton;
import com.wynntils.screens.playerviewer.widgets.PartyButton;
import com.wynntils.screens.playerviewer.widgets.PlayerInteractionButton;
import com.wynntils.screens.playerviewer.widgets.SimplePlayerInteractionButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.wynn.ItemUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team.Visibility;

public final class PlayerViewerScreen extends WynntilsContainerScreen<PlayerViewerMenu> {
    private static final String TEAM_NAME = "PlayerViewerTeam";

    private final Player player;
    private final Scoreboard scoreboard;
    private final PlayerTeam playerViewerTeam;
    private final PlayerTeam oldTeam;
    private final List<PlayerInteractionButton> interactionButtons = new ArrayList<>();

    private FriendButton friendButton;
    private PartyButton partyButton;

    private PlayerViewerScreen(Player player, PlayerViewerMenu menu) {
        super(menu, player.getInventory(), Component.empty());

        this.player = player;
        this.scoreboard = player.level().getScoreboard();

        if (scoreboard.getTeamNames().contains(TEAM_NAME)) {
            playerViewerTeam = scoreboard.getPlayerTeam(TEAM_NAME);
        } else {
            playerViewerTeam = scoreboard.addPlayerTeam(TEAM_NAME);
            playerViewerTeam.setNameTagVisibility(Visibility.NEVER);
        }

        // this is done to prevent the player's nametag from rendering in the GUI
        oldTeam = scoreboard.getPlayersTeam(player.getScoreboardName());
        scoreboard.addPlayerToTeam(player.getScoreboardName(), playerViewerTeam);
    }

    public static Screen create(Player player) {
        ItemStack heldItem = createDecoratedItemStack(player.getMainHandItem(), player.getName());

        List<ItemStack> armorItems = new ArrayList<>();
        for (ItemStack armorSlot : player.getArmorSlots()) {
            armorItems.add(createDecoratedItemStack(armorSlot, player.getName()));
        }
        Collections.reverse(armorItems);

        return new PlayerViewerScreen(player, PlayerViewerMenu.create(heldItem, armorItems));
    }

    private static ItemStack createDecoratedItemStack(ItemStack itemStack, Component playerName) {
        if (itemStack.isEmpty()) {
            return itemStack;
        }

        // This must specifically NOT be normalized; the ֎ is significant
        String gearName = StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting();
        MutableComponent description = ItemUtils.getNonGearDescription(itemStack, gearName);
        if (description != null) {
            itemStack.set(DataComponents.CUSTOM_NAME, description);
            return itemStack;
        }

        GearInfo gearInfo = Models.Gear.getGearInfoFromApiName(gearName);
        if (gearInfo == null) {
            return itemStack;
        }

        JsonObject itemData = LoreUtils.getJsonFromIngameLore(itemStack);
        GearInstance gearInstance = Models.Gear.parseInstance(gearInfo, itemData);
        return new FakeItemStack(new GearItem(gearInfo, gearInstance), "From " + playerName.getString());
    }

    @Override
    protected void doInit() {
        interactionButtons.clear();
        this.leftPos = (this.width - Texture.PLAYER_VIEWER_BACKGROUND.width()) / 2;
        this.topPos = (this.height - Texture.PLAYER_VIEWER_BACKGROUND.height()) / 2;

        String playerName = StyledText.fromComponent(player.getName()).getStringWithoutFormatting();

        // left
        // view player stats button
        interactionButtons.add(new SimplePlayerInteractionButton(
                leftPos - 21,
                topPos + (Texture.PLAYER_VIEWER_BACKGROUND.height() / 5 - 2),
                Component.translatable("screens.wynntils.playerViewer.viewStats"),
                Texture.STATS_ICON,
                () -> Managers.Net.openLink(UrlId.LINK_WYNNCRAFT_PLAYER_STATS, Map.of("username", playerName))));

        // add friend button
        friendButton = new FriendButton(
                leftPos - 21, topPos + (Texture.PLAYER_VIEWER_BACKGROUND.height() / 5) + 18, playerName);
        interactionButtons.add(friendButton);

        // invite party button
        partyButton = new PartyButton(
                leftPos - 21, topPos + (Texture.PLAYER_VIEWER_BACKGROUND.height() / 5) + 38, playerName);
        interactionButtons.add(partyButton);

        // right
        // duel button
        interactionButtons.add(new SimplePlayerInteractionButton(
                leftPos + Texture.PLAYER_VIEWER_BACKGROUND.width() + 1,
                topPos + (Texture.PLAYER_VIEWER_BACKGROUND.height() / 5) - 2,
                Component.translatable("screens.wynntils.playerViewer.duel"),
                Texture.DUEL_ICON,
                () -> Handlers.Command.queueCommand("duel " + playerName)));

        // trade button
        interactionButtons.add(new SimplePlayerInteractionButton(
                leftPos + Texture.PLAYER_VIEWER_BACKGROUND.width() + 1,
                topPos + (Texture.PLAYER_VIEWER_BACKGROUND.height() / 5) + 18,
                Component.translatable("screens.wynntils.playerViewer.trade"),
                Texture.TRADE_ICON,
                () -> Handlers.Command.queueCommand("trade " + playerName)));

        // msg button
        interactionButtons.add(new SimplePlayerInteractionButton(
                leftPos + Texture.PLAYER_VIEWER_BACKGROUND.width() + 1,
                topPos + (Texture.PLAYER_VIEWER_BACKGROUND.height() / 5) + 38,
                Component.translatable("screens.wynntils.playerViewer.message"),
                Texture.MESSAGE_ICON,
                () -> {
                    this.onClose(); // Required so that nametags render properly
                    McUtils.mc().setScreen(new ChatScreen("/msg " + playerName + " "));
                }));
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        renderPlayerModel(guiGraphics, mouseX, mouseY);

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics.pose(),
                        StyledText.fromComponent(Component.translatable("screens.wynntils.playerViewer.warning")),
                        this.width / 2f - 200,
                        this.width / 2f + 200,
                        (this.height - Texture.PLAYER_VIEWER_BACKGROUND.height()) / 2f - 110,
                        (this.height - Texture.PLAYER_VIEWER_BACKGROUND.height()) / 2f - 10,
                        400,
                        CommonColors.RED,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.OUTLINE);

        interactionButtons.forEach(button -> button.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    private void renderPlayerModel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int renderX = (this.width - Texture.PLAYER_VIEWER_BACKGROUND.width()) / 2;
        int renderY = (this.height - Texture.PLAYER_VIEWER_BACKGROUND.height()) / 2;

        int renderWidth = Texture.PLAYER_VIEWER_BACKGROUND.width();
        int renderHeight = Texture.PLAYER_VIEWER_BACKGROUND.height();

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics,
                renderX,
                renderY,
                renderX + renderWidth,
                renderY + renderHeight,
                30,
                0.2f,
                mouseX,
                mouseY,
                player);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        BufferedRenderUtils.drawTexturedRect(
                guiGraphics.pose(),
                guiGraphics.bufferSource,
                Texture.PLAYER_VIEWER_BACKGROUND,
                this.leftPos,
                this.topPos);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // we don't want to draw any labels
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (PlayerInteractionButton interactionButton : interactionButtons) {
            if (interactionButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        // do nothing here, because we don't want the user interacting with slots at all
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == McUtils.options().keyInventory.key.getValue()) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        // restore previous scoreboard team setup
        scoreboard.removePlayerFromTeam(player.getScoreboardName(), playerViewerTeam);
        if (oldTeam != null) {
            scoreboard.addPlayerToTeam(player.getScoreboardName(), oldTeam);
        }

        super.onClose();
    }

    public Player getPlayer() {
        return player;
    }

    public void updateButtonIcons() {
        friendButton.updateIcon();
        partyButton.updateIcon();
    }
}
