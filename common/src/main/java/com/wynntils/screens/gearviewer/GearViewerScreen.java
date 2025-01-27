/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.gearviewer;

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
import com.wynntils.screens.gearviewer.widgets.FriendButton;
import com.wynntils.screens.gearviewer.widgets.PartyButton;
import com.wynntils.screens.gearviewer.widgets.PlayerInteractionButton;
import com.wynntils.screens.gearviewer.widgets.SimplePlayerInteractionButton;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.wynn.ItemUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team.Visibility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class GearViewerScreen extends WynntilsContainerScreen<GearViewerMenu> {
    private static final String TEAM_NAME = "GearViewerTeam";

    private final Player player;
    private final Scoreboard scoreboard;
    private final PlayerTeam gearViewerTeam;
    private final PlayerTeam oldTeam;
    private List<PlayerInteractionButton> interactionButtons = new ArrayList<>();
    private FriendButton friendButton;
    private PartyButton partyButton;

    private GearViewerScreen(Player player, GearViewerMenu menu) {
        super(menu, player.getInventory(), Component.empty());

        this.player = player;
        this.scoreboard = player.level().getScoreboard();

        if (scoreboard.getTeamNames().contains(TEAM_NAME)) {
            gearViewerTeam = scoreboard.getPlayerTeam(TEAM_NAME);
        } else {
            gearViewerTeam = scoreboard.addPlayerTeam(TEAM_NAME);
            gearViewerTeam.setNameTagVisibility(Visibility.NEVER);
        }

        // this is done to prevent the player's nametag from rendering in the GUI
        oldTeam = scoreboard.getPlayersTeam(player.getScoreboardName());
        scoreboard.addPlayerToTeam(player.getScoreboardName(), gearViewerTeam);
    }

    public static Screen create(Player player) {
        ItemStack heldItem = createDecoratedItemStack(player.getMainHandItem(), player.getName());

        List<ItemStack> armorItems = new ArrayList<>();
        for (ItemStack armorSlot : player.getArmorSlots()) {
            armorItems.add(createDecoratedItemStack(armorSlot, player.getName()));
        }
        Collections.reverse(armorItems);

        return new GearViewerScreen(player, GearViewerMenu.create(heldItem, armorItems));
    }

    private static ItemStack createDecoratedItemStack(ItemStack itemStack, Component playerName) {
        if (itemStack.getItem() == Items.AIR) {
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
        this.leftPos = (this.width - Texture.GEAR_VIEWER_BACKGROUND.width()) / 2;
        this.topPos = (this.height - Texture.GEAR_VIEWER_BACKGROUND.height()) / 2;

        String playerName = StyledText.fromComponent(player.getName()).getStringWithoutFormatting();

        // left
        // view player stats button
        interactionButtons.add(new SimplePlayerInteractionButton(
                leftPos - 21,
                topPos + (Texture.GEAR_VIEWER_BACKGROUND.height() / 5 - 2),
                Component.translatable("screens.wynntils.gearViewer.viewStats"), Component.literal("↵"),
                () -> Managers.Net.openLink(UrlId.LINK_WYNNCRAFT_PLAYER_STATS, Map.of("username", playerName))));

        // add friend button
        friendButton = new FriendButton(
                leftPos - 21,
                topPos + (Texture.GEAR_VIEWER_BACKGROUND.height() / 5) + 18,
                playerName);
        interactionButtons.add(friendButton);

        // invite party button
        partyButton = new PartyButton(
                leftPos - 21,
                topPos + (Texture.GEAR_VIEWER_BACKGROUND.height() / 5) + 38,
                playerName);
        interactionButtons.add(partyButton);

        // right
        // duel button
        interactionButtons.add(new SimplePlayerInteractionButton(
                leftPos + Texture.GEAR_VIEWER_BACKGROUND.width() + 1,
                topPos + (Texture.GEAR_VIEWER_BACKGROUND.height() / 5) - 2,
                Component.translatable("screens.wynntils.gearViewer.duel"),
                Component.literal("D"),
                () -> Handlers.Command.queueCommand("duel " + playerName)));

        // trade button
        interactionButtons.add(new SimplePlayerInteractionButton(
                leftPos + Texture.GEAR_VIEWER_BACKGROUND.width() + 1,
                topPos + (Texture.GEAR_VIEWER_BACKGROUND.height() / 5) + 18,
                Component.translatable("screens.wynntils.gearViewer.trade"),
                Component.literal("T"),
                () -> Handlers.Command.queueCommand("trade " + playerName)));
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        renderPlayerModel(guiGraphics, mouseX, mouseY);

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        interactionButtons.forEach(button -> button.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    private void renderPlayerModel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int renderX = (this.width - Texture.GEAR_VIEWER_BACKGROUND.width()) / 2;
        int renderY = (this.height - Texture.GEAR_VIEWER_BACKGROUND.height()) / 2;

        int renderWidth = Texture.GEAR_VIEWER_BACKGROUND.width();
        int renderHeight = Texture.GEAR_VIEWER_BACKGROUND.height();

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
                guiGraphics.bufferSource(),
                Texture.GEAR_VIEWER_BACKGROUND,
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
        scoreboard.removePlayerFromTeam(player.getScoreboardName(), gearViewerTeam);
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
