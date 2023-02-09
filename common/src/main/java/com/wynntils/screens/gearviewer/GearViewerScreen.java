/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.gearviewer;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.screens.base.WynntilsContainerScreen;
import com.wynntils.screens.gearviewer.widgets.ViewPlayerStatsButton;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.wynn.WynnItemMatchers;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class GearViewerScreen extends WynntilsContainerScreen<GearViewerMenu> {
    private final Player player;
    private ViewPlayerStatsButton viewPlayerStatsButton;

    private GearViewerScreen(Player player, GearViewerMenu menu) {
        super(menu, player.getInventory(), Component.empty());

        this.player = player;
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

        String gearName = WynnUtils.normalizeBadString(ComponentUtils.getUnformatted(itemStack.getHoverName()));
        MutableComponent description = WynnItemMatchers.getNonGearDescription(itemStack, gearName);
        if (description != null) {
            itemStack.setHoverName(description);
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

        viewPlayerStatsButton = new ViewPlayerStatsButton(
                leftPos - 20,
                topPos + (Texture.GEAR_VIEWER_BACKGROUND.height() / 4),
                18,
                20,
                ComponentUtils.getUnformatted(player.getName()));
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.doRender(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);

        renderPlayerModel();

        viewPlayerStatsButton.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderPlayerModel() {
        float posX = this.width / 2f;
        float posY = this.height / 2f;

        InventoryScreen.renderEntityInInventory((int) posX, (int) posY + 32, 30, 0, 0, player);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderUtils.drawTexturedRect(poseStack, Texture.GEAR_VIEWER_BACKGROUND, this.leftPos, this.topPos);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // we don't want to draw any labels
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return viewPlayerStatsButton.mouseClicked(mouseX, mouseY, button);
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

    public Player getPlayer() {
        return player;
    }
}
