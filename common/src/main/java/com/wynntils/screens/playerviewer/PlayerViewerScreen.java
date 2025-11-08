/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.playerviewer;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.inventory.type.InventoryAccessory;
import com.wynntils.models.inventory.type.InventoryArmor;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.screens.base.WynntilsContainerScreen;
import com.wynntils.screens.base.widgets.InfoButton;
import com.wynntils.screens.playerviewer.widgets.FriendButton;
import com.wynntils.screens.playerviewer.widgets.PartyButton;
import com.wynntils.screens.playerviewer.widgets.PlayerInteractionButton;
import com.wynntils.screens.playerviewer.widgets.SimplePlayerInteractionButton;
import com.wynntils.services.hades.HadesUser;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
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

    private Button settingsButton;
    private FriendButton friendButton;
    private InfoButton infoButton;
    private PartyButton partyButton;

    private static boolean noGear = true;

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
        Optional<HadesUser> hadesUserOpt = Services.Hades.getHadesUser(player.getUUID());

        ItemStack heldItem = ItemStack.EMPTY;
        List<ItemStack> armorItems = new ArrayList<>();
        List<ItemStack> accessoryItems = new ArrayList<>();

        if (hadesUserOpt.isPresent()) {
            heldItem = createDecoratedItemStack(hadesUserOpt.get().getHeldItem(), player);

            noGear = heldItem.isEmpty();

            for (InventoryAccessory accessory : InventoryAccessory.values()) {
                WynnItem wynnItem = hadesUserOpt.get().getAccessories().get(accessory);

                if (wynnItem == null) {
                    accessoryItems.add(ItemStack.EMPTY);
                    continue;
                }

                ItemStack accessoryItemStack = createDecoratedItemStack(wynnItem, player);

                accessoryItems.add(accessoryItemStack);
                noGear = false;
            }

            for (InventoryArmor armor : InventoryArmor.values()) {
                WynnItem wynnItem = hadesUserOpt.get().getArmor().get(armor);

                if (wynnItem == null) {
                    armorItems.add(ItemStack.EMPTY);
                    continue;
                }

                ItemStack armorItemStack = createDecoratedItemStack(wynnItem, player);

                armorItems.add(armorItemStack);
                noGear = false;
            }
        }

        return new PlayerViewerScreen(player, PlayerViewerMenu.create(heldItem, armorItems, accessoryItems));
    }

    private static ItemStack createDecoratedItemStack(WynnItem wynnItem, Player player) {
        ItemStack itemStack = ItemStack.EMPTY;

        if (wynnItem instanceof GearItem gearItem) {
            itemStack = gearItem.getItemInfo().metaInfo().material().itemStack();
        } else if (wynnItem instanceof CraftedGearItem craftedGearItem) {
            // Armor and weapons are sent by Wynn so we can use those itemstacks, accessories we will have to use
            // a default texture
            if (craftedGearItem.getGearType().isArmor()) {
                itemStack = player.getInventory()
                        .armor
                        .get(InventoryArmor.fromString(
                                        craftedGearItem.getGearType().name())
                                .getArmorSlot());
            } else if (craftedGearItem.getGearType().isWeapon()) {
                itemStack = player.getMainHandItem();
            } else {
                itemStack = new ItemStack(Items.POTION);
                CustomModelData customModelData = new CustomModelData(
                        List.of(craftedGearItem.getGearType().getDefaultModel()), List.of(), List.of(), List.of());
                DataComponentMap.Builder componentsBuilder =
                        DataComponentMap.builder().set(DataComponents.CUSTOM_MODEL_DATA, customModelData);
                itemStack.applyComponents(componentsBuilder.build());
            }
        }

        return new FakeItemStack(wynnItem, itemStack, "From " + player.getScoreboardName());
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
                    McUtils.openChatScreen("/msg " + playerName + " ");
                }));

        settingsButton = new Button.Builder(
                        Component.translatable("screens.wynntils.playerViewer.sharingSettings"), (b) -> {
                            McUtils.setScreen(GearSharingSettingsScreen.create(this));
                        })
                .pos(leftPos + 1, topPos - 21)
                .size(Texture.PLAYER_VIEWER_BACKGROUND.width() - 23, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.playerViewer.sharingSettingsTooltip")))
                .build();

        if (noGear) {
            infoButton = new InfoButton(
                    leftPos + Texture.PLAYER_VIEWER_BACKGROUND.width() - 21,
                    topPos - 21,
                    Component.literal("")
                            .append(Component.translatable("screens.wynntils.playerViewer.helpTitle")
                                    .withStyle(ChatFormatting.UNDERLINE))
                            .append(Component.literal("\n"))
                            .append(Component.translatable("screens.wynntils.playerViewer.help")
                                    .withStyle(ChatFormatting.GRAY)));
        }
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        renderPlayerModel(guiGraphics, mouseX, mouseY);

        this.renderTooltip(guiGraphics, mouseX, mouseY);

        interactionButtons.forEach(button -> button.render(guiGraphics, mouseX, mouseY, partialTick));
        settingsButton.render(guiGraphics, mouseX, mouseY, partialTick);

        if (infoButton == null) return;

        infoButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderPlayerModel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int renderX = (this.width - Texture.PLAYER_VIEWER_BACKGROUND.width()) / 2 + 13;
        int renderY = (this.height - Texture.PLAYER_VIEWER_BACKGROUND.height()) / 2 - 4;

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

        return settingsButton.mouseClicked(mouseX, mouseY, button);
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
