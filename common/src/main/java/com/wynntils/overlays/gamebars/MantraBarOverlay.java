/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.models.abilities.bossbars.MantraBar;
import com.wynntils.models.abilities.type.ShamanMaskType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class MantraBarOverlay extends BaseBarOverlay {
    @Persisted
    private final Config<Boolean> showMaskNames = new Config<>(false);

    public MantraBarOverlay() {
        super(
                new OverlayPosition(
                        -70,
                        -150,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(81, 21),
                CommonColors.WHITE);
    }

    @Override
    protected BossBarProgress progress() {
        return Models.Ability.mantraBar.getBarProgress();
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return MantraBar.class;
    }

    @Override
    protected boolean isVisible() {
        return Models.Ability.mantraBar.isActive();
    }

    @Override
    protected void renderText(GuiGraphicsExtractor guiGraphics, float renderY, String text) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        getText(),
                        this.getRenderX(),
                        this.getRenderX() + this.getWidth(),
                        renderY,
                        0,
                        CommonColors.WHITE,
                        this.getRenderHorizontalAlignment(),
                        this.textShadow.get());
    }

    private StyledText getText() {
        MutableComponent text = Component.empty()
                .append(Component.literal(ShamanMaskType.LUNATIC.getMaskDisplayString())
                        .withStyle(Style.EMPTY
                                .withColor(ShamanMaskType.LUNATIC.getColor().asInt())
                                .withFont(CommonFonts.COMMON_FONT)))
                .append(Component.literal(" "));
        if (showMaskNames.get()) {
            text.append(Component.literal("Lunatic ")
                    .withStyle(Style.EMPTY.withColor(
                            ShamanMaskType.LUNATIC.getColor().asInt())));
        }
        text.append(Component.literal("+" + Models.Ability.mantraBar.getMaskOverload(ShamanMaskType.LUNATIC) + "%")
                        .withStyle(getOverloadColor(ShamanMaskType.LUNATIC)))
                .append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(ShamanMaskType.HERETIC.getMaskDisplayString())
                        .withStyle(Style.EMPTY
                                .withColor(ShamanMaskType.HERETIC.getColor().asInt())
                                .withFont(CommonFonts.COMMON_FONT)))
                .append(Component.literal(" "));
        if (showMaskNames.get()) {
            text.append(Component.literal("Heretic ")
                    .withStyle(Style.EMPTY.withColor(
                            ShamanMaskType.HERETIC.getColor().asInt())));
        }
        text.append(Component.literal("+" + Models.Ability.mantraBar.getMaskOverload(ShamanMaskType.HERETIC) + "%")
                        .withStyle(getOverloadColor(ShamanMaskType.HERETIC)))
                .append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(ShamanMaskType.FANATIC.getMaskDisplayString())
                        .withStyle(Style.EMPTY
                                .withColor(ShamanMaskType.FANATIC.getColor().asInt())
                                .withFont(CommonFonts.COMMON_FONT)))
                .append(Component.literal(" "));
        if (showMaskNames.get()) {
            text.append(Component.literal("Fanatic ")
                    .withStyle(Style.EMPTY.withColor(
                            ShamanMaskType.FANATIC.getColor().asInt())));
        }
        text.append(Component.literal("+" + Models.Ability.mantraBar.getMaskOverload(ShamanMaskType.FANATIC) + "%")
                .withStyle(getOverloadColor(ShamanMaskType.FANATIC)));

        return StyledText.fromComponent(text);
    }

    private ChatFormatting getOverloadColor(ShamanMaskType maskType) {
        int overload = Models.Ability.mantraBar.getMaskOverload(maskType);

        if (overload == 0) {
            return ChatFormatting.RED;
        } else if (Models.Ability.mantraBar.isOverloadCapped(maskType)) {
            return ChatFormatting.GREEN;
        }

        return ChatFormatting.WHITE;
    }
}
