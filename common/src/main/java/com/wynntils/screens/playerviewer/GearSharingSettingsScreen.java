/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.playerviewer;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.models.inventory.type.InventoryAccessory;
import com.wynntils.models.inventory.type.InventoryArmor;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

public class GearSharingSettingsScreen extends WynntilsScreen {
    private final Screen previousScreen;

    private int offsetX;
    private int offsetY;

    private GearSharingSettingsScreen(Screen previousScreen) {
        super(Component.empty());

        this.previousScreen = previousScreen;
    }

    public static GearSharingSettingsScreen create(Screen previousScreen) {
        return new GearSharingSettingsScreen(previousScreen);
    }

    @Override
    public void doInit() {
        offsetX = (this.width - Texture.PLAYER_VIEWER_BACKGROUND.width()) / 2;
        offsetY = (this.height - Texture.PLAYER_VIEWER_BACKGROUND.height()) / 2;

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.gearSharingSettings.back"), (button -> onClose()))
                .pos(offsetX + 1, offsetY - 21)
                .size(Texture.PLAYER_VIEWER_BACKGROUND.width() - 2, 20)
                .build());

        this.addRenderableWidget(new WynntilsCheckbox(
                offsetX + 100,
                offsetY + 63,
                16,
                Component.empty(),
                Services.Hades.getGearShareOptions().shouldShareHeldItem(),
                0,
                (checkbox, bl) -> {
                    Services.Hades.getGearShareOptions().setShareHeldItem(bl);
                },
                List.of(Component.translatable("screens.wynntils.gearSharingSettings.shareHeldItem"))));

        int x = offsetX + 28;
        int y = offsetY + 9;

        for (InventoryArmor armor : InventoryArmor.values()) {
            this.addRenderableWidget(new WynntilsCheckbox(
                    x,
                    y,
                    16,
                    Component.empty(),
                    Services.Hades.getGearShareOptions().shouldShareArmor(armor),
                    0,
                    (checkbox, bl) -> {
                        Services.Hades.getGearShareOptions().setShareArmor(armor, bl);
                    },
                    List.of(Component.translatable(
                            "screens.wynntils.gearSharingSettings.shareTemplate", EnumUtils.toNiceString(armor)))));

            y += 18;
        }

        x = offsetX + 10;
        y = offsetY + 9;

        for (InventoryAccessory accessory : InventoryAccessory.values()) {
            this.addRenderableWidget(new WynntilsCheckbox(
                    x,
                    y,
                    16,
                    Component.empty(),
                    Services.Hades.getGearShareOptions().shouldShareAccessory(accessory),
                    0,
                    (checkbox, bl) -> {
                        Services.Hades.getGearShareOptions().setShareAccessory(accessory, bl);
                    },
                    List.of(Component.translatable(
                            "screens.wynntils.gearSharingSettings.shareTemplate", EnumUtils.toNiceString(accessory)))));

            y += 18;
        }

        this.addRenderableWidget(new WynntilsCheckbox(
                offsetX + Texture.PLAYER_VIEWER_BACKGROUND.width() + 1,
                offsetY + 9,
                16,
                Component.translatable("screens.wynntils.gearSharingSettings.shareCraftedGear"),
                Services.Hades.getGearShareOptions().shareCraftedItems(),
                150,
                (checkbox, bl) -> {
                    Services.Hades.getGearShareOptions().setShareCraftedItems(bl);
                },
                List.of(Component.translatable("screens.wynntils.gearSharingSettings.shareCraftedGearTooltip"))));

        this.addRenderableWidget(new WynntilsCheckbox(
                offsetX + Texture.PLAYER_VIEWER_BACKGROUND.width() + 1,
                offsetY + Texture.PLAYER_VIEWER_BACKGROUND.height() - 25,
                16,
                Component.translatable("screens.wynntils.gearSharingSettings.shareItemNames"),
                Services.Hades.getGearShareOptions().shareCraftedNames(),
                150,
                (checkbox, bl) -> {
                    Services.Hades.getGearShareOptions().setShareCraftedNames(bl);
                },
                List.of(Component.translatable("screens.wynntils.gearSharingSettings.shareItemNamesTooltip"))));
    }

    @Override
    public void onClose() {
        Services.Hades.saveGearShareOptions();
        McUtils.setScreen(previousScreen);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.PLAYER_VIEWER_BACKGROUND, offsetX, offsetY);

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics,
                offsetX + 13,
                offsetY - 4,
                offsetX + 13 + Texture.PLAYER_VIEWER_BACKGROUND.width(),
                offsetY - 4 + Texture.PLAYER_VIEWER_BACKGROUND.height(),
                30,
                0.2f,
                mouseX,
                mouseY,
                McUtils.player());
    }
}
