/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.gearviewer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.screens.base.WynntilsScreen;
import com.wynntils.screens.gearviewer.widgets.GearItemButton;
import com.wynntils.screens.gearviewer.widgets.ViewPlayerStatsButton;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.wynn.WynnItemMatchers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class GearViewerScreen extends WynntilsScreen {
    private static final List<Component> VIEW_STATS_TOOLTIP =
            List.of(Component.translatable("screens.wynntils.gearViewer.viewStats"));

    private final Player player;
    private final ItemStack heldItem;
    private final List<ItemStack> armorItems;

    private GearViewerScreen(Player player) {
        super(Component.empty());
        this.player = player;
        this.heldItem = createDecoratedItemStack(player.getMainHandItem(), player.getName());

        this.armorItems = new ArrayList<>();
        for (ItemStack armorSlot : player.getArmorSlots()) {
            armorItems.add(createDecoratedItemStack(armorSlot, player.getName()));
        }

        Collections.reverse(armorItems);
    }

    public static Screen create(Player player) {
        return new GearViewerScreen(player);
    }

    private ItemStack createDecoratedItemStack(ItemStack itemStack, Component playerName) {
        if (itemStack.getItem() == Items.AIR) {
            return itemStack;
        }

        String gearName = ComponentUtils.getUnformatted(itemStack.getHoverName());
        MutableComponent description = WynnItemMatchers.getNonGearDescription(itemStack, gearName);
        if (description != null) {
            itemStack.setHoverName(description);
            return itemStack;
        }

        GearInfo gearInfo = Models.GearInfo.getGearInfoFromInternalName(gearName);
        if (gearInfo == null) {
            return itemStack;
        }

        GearItem gearItem = Models.GearInfo.fromJsonLore(itemStack, gearInfo);
        return new FakeItemStack(gearItem, "From " + playerName.getString());
    }

    @Override
    protected void doInit() {
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
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
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
        for (Renderable renderable : renderables) {
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
