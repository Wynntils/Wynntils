/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.widgets.GearItemButton;
import com.wynntils.gui.widgets.ViewPlayerStatsButton;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.utils.WynnItemUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GearViewerScreen extends Screen {
    private static final List<Component> VIEW_STATS_TOOLTIP =
            List.of(new TranslatableComponent("screens.wynntils.gearViewer.viewStats"));

    private final Player player;
    private final ItemStack heldItem;
    private final List<ItemStack> armorItems;

    private GearViewerScreen(Player player) {
        super(TextComponent.EMPTY);
        this.player = player;
        this.heldItem = WynnItemUtils.getParsedItemStack(player.getMainHandItem());

        this.armorItems = new ArrayList<>();
        for (ItemStack armorSlot : player.getArmorSlots()) {
            armorItems.add(WynnItemUtils.getParsedItemStack(armorSlot));
        }

        Collections.reverse(armorItems);
    }

    public static Screen create(Player player) {
        return WynntilsScreenWrapper.create(new GearViewerScreen(player));
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new ViewPlayerStatsButton(
                -20,
                Texture.GEAR_VIEWER_BACKGROUND.height() / 3,
                18,
                20,
                ComponentUtils.getUnformatted(player.getName())));

        this.addRenderableWidget(new GearItemButton(
                Texture.GEAR_VIEWER_BACKGROUND.width() - 22,
                Texture.GEAR_VIEWER_BACKGROUND.height() - 25,
                18,
                18,
                this,
                heldItem));

        for (int i = 0; i < armorItems.size(); i++) {
            ItemStack armorItem = armorItems.get(i);
            this.addRenderableWidget(new GearItemButton(11, 11 + i * 18, 18, 18, this, armorItem));
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        float translationX = getTranslationX();
        float translationY = getTranslationY();

        poseStack.translate(translationX, translationY, 0f);

        renderBg(poseStack);

        renderPlayerModel();

        renderButtons(poseStack, mouseX, mouseY, partialTick);

        renderHoveredTooltip(poseStack, mouseX, mouseY);
    }

    private void renderHoveredTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        mouseX -= getTranslationX();
        mouseY -= getTranslationY();

        GuiEventListener hovered = null;
        for (GuiEventListener child : children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                hovered = child;
                break;
            }
        }

        if (hovered == null) return;

        if (hovered instanceof ViewPlayerStatsButton) {
            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY,
                    0,
                    VIEW_STATS_TOOLTIP,
                    FontRenderer.getInstance().getFont(),
                    true);
            return;
        }

        if (hovered instanceof GearItemButton gearItemButton && gearItemButton.getItemStack() != null) {
            this.renderTooltip(poseStack, gearItemButton.getItemStack(), mouseX, mouseY);
        }
    }

    private void renderButtons(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        for (Widget renderable : renderables) {
            renderable.render(
                    poseStack, (int) (mouseX - getTranslationX()), (int) (mouseY - getTranslationY()), partialTick);
        }
    }

    private void renderPlayerModel() {
        float posX = this.width / 2f;
        float posY = this.height / 2f;

        InventoryScreen.renderEntityInInventory((int) posX, (int) posY + 32, 30, 0, 0, player);
    }

    private static void renderBg(PoseStack poseStack) {
        RenderUtils.drawTexturedRect(poseStack, Texture.GEAR_VIEWER_BACKGROUND, 0, 0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX -= getTranslationX();
        mouseY -= getTranslationY();

        for (GuiEventListener child : children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                child.mouseClicked(mouseX, mouseY, button);
            }
        }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == McUtils.options().keyInventory.key.getValue()) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public float getTranslationX() {
        return (this.width - Texture.GEAR_VIEWER_BACKGROUND.width()) / 2f;
    }

    public float getTranslationY() {
        return (this.height - Texture.GEAR_VIEWER_BACKGROUND.height()) / 2f;
    }

    public Player getPlayer() {
        return player;
    }
}
