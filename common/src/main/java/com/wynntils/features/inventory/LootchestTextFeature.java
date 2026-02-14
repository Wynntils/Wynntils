/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.models.containers.containers.reward.LootChestContainer;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Matrix3x2fStack;

@ConfigCategory(Category.INVENTORY)
public class LootchestTextFeature extends Feature {
    @Persisted
    private final Config<String> titleTextTemplate = new Config<>("§8Total: {chest_opened}");

    @Persisted
    private final Config<String> inventoryTextTemplate = new Config<>("§8Dry: {dry_streak}");

    public LootchestTextFeature() {
        super(ProfileDefault.onlyDefault());
    }

    @SubscribeEvent
    public void onRenderLootChest(ContainerRenderEvent event) {
        if (!(Models.Container.getCurrentContainer() instanceof LootChestContainer)) return;

        int startX = event.getScreen().leftPos;
        int startY = event.getScreen().topPos;
        int width = event.getScreen().imageWidth;
        int titleLabelX = event.getScreen().titleLabelX;
        int titleLabelY = event.getScreen().titleLabelY;
        int inventoryLabelX = event.getScreen().inventoryLabelX;
        int inventoryLabelY = event.getScreen().inventoryLabelY;

        Matrix3x2fStack matrixStack = event.getGuiGraphics().pose();

        matrixStack.pushMatrix();
        matrixStack.translate(startX, startY);

        renderTitleTemplate(event.getGuiGraphics(), width - titleLabelX, titleLabelY);
        renderInventoryTemplate(event.getGuiGraphics(), width - inventoryLabelX, inventoryLabelY);

        matrixStack.popMatrix();
    }

    private void renderTitleTemplate(GuiGraphics guiGraphics, int x, int y) {
        String titleTemplateResult = Arrays.stream(Managers.Function.doFormatLines(titleTextTemplate.get()))
                .map(StyledText::getString)
                .collect(Collectors.joining(" "));

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(titleTemplateResult),
                        x,
                        y,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.RIGHT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }

    private void renderInventoryTemplate(GuiGraphics guiGraphics, int x, int y) {
        String inventoryTemplateResult = Arrays.stream(Managers.Function.doFormatLines(inventoryTextTemplate.get()))
                .map(StyledText::getString)
                .collect(Collectors.joining(" "));

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(inventoryTemplateResult),
                        x,
                        y,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.RIGHT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }
}
