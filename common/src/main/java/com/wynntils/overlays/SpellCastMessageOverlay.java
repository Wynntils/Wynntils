/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;

public class SpellCastMessageOverlay extends Overlay {
    private static final FontDescription SPELL_COSTS_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("hud/gameplay/default/bottom_middle"));
    private static final FontDescription WYNNCRAFT_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("language/wynncraft"));

    private static final String MANA_ICON = "\uE531";
    private static final String HEALTH_ICON = "\uE530";

    @Persisted
    private final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    @Persisted
    private final Config<MessageStyle> messageStyle = new Config<>(MessageStyle.MODERN);

    @Persisted
    private final Config<Boolean> showFailedCasts = new Config<>(true);

    @Persisted
    private final Config<TextShadow> textShadow = new Config<>(TextShadow.NORMAL);

    @Persisted
    protected final Config<Float> fontScale = new Config<>(1.0f);

    private StyledText spellMessage = StyledText.EMPTY;

    public SpellCastMessageOverlay() {
        super(
                new OverlayPosition(
                        -62,
                        0,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(200, 20),
                HorizontalAlignment.CENTER,
                VerticalAlignment.BOTTOM);
    }

    @SubscribeEvent
    public void onSpellCast(SpellEvent.Cast event) {
        int manaCost = event.getManaCost();
        int healthCost = event.getHealthCost();
        boolean modernStyle = messageStyle.get() == MessageStyle.MODERN;
        MutableComponent component = Component.empty();

        component.append(Component.literal(event.getSpellType().getName() + " Cast! ")
                .withStyle(Style.EMPTY
                        .withFont(modernStyle ? WYNNCRAFT_FONT : FontDescription.DEFAULT)
                        .withColor(ChatFormatting.GRAY)));

        if (!modernStyle) {
            component.append(Component.literal("[").withStyle(ChatFormatting.DARK_AQUA));
        }

        component.append(Component.literal("-" + manaCost + " ")
                .withStyle(Style.EMPTY
                        .withFont(modernStyle ? WYNNCRAFT_FONT : FontDescription.DEFAULT)
                        .withColor(ChatFormatting.AQUA)));

        if (modernStyle) {
            component.append(Component.literal(MANA_ICON).withStyle(Style.EMPTY.withFont(SPELL_COSTS_FONT)));
        } else {
            component.append(Component.literal("✺").withStyle(ChatFormatting.AQUA));
            component.append(Component.literal("]").withStyle(ChatFormatting.DARK_AQUA));
        }

        if (healthCost > 0) {
            if (!modernStyle) {
                component.append(Component.literal(" [").withStyle(ChatFormatting.DARK_RED));
            }

            component.append(Component.literal((modernStyle ? " -" : "-") + healthCost + " ")
                    .withStyle(Style.EMPTY
                            .withFont(modernStyle ? WYNNCRAFT_FONT : FontDescription.DEFAULT)
                            .withColor(ChatFormatting.RED)));

            if (modernStyle) {
                component.append(Component.literal(HEALTH_ICON).withStyle(Style.EMPTY.withFont(SPELL_COSTS_FONT)));
            } else {
                component.append(Component.literal("❤").withStyle(ChatFormatting.RED));
                component.append(Component.literal("]").withStyle(ChatFormatting.DARK_RED));
            }
        }

        spellMessage = StyledText.fromComponent(component);
    }

    @SubscribeEvent
    public void onSpellPartial(SpellEvent.Partial event) {
        // Only reset on the 1st input so that failure message can be shown
        if (event.getSpellDirectionArray().length != 1) return;

        spellMessage = StyledText.EMPTY;
    }

    @SubscribeEvent
    public void onSpellCastExpire(SpellEvent.Expired event) {
        spellMessage = StyledText.EMPTY;
    }

    @SubscribeEvent
    public void onSpellCastExpire(SpellEvent.CastExpired event) {
        spellMessage = StyledText.EMPTY;
    }

    @SubscribeEvent
    public void onSpellFailed(SpellEvent.Failed event) {
        if (!showFailedCasts.get()) return;

        spellMessage = StyledText.fromComponent(Component.literal(
                        event.getFailureReason().getDisplayMessage())
                .withStyle(Style.EMPTY
                        .withFont(messageStyle.get() == MessageStyle.MODERN ? WYNNCRAFT_FONT : FontDescription.DEFAULT)
                        .withColor(ChatFormatting.RED)));
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        Models.Spell.setHideSpellCasts(Managers.Overlay.isEnabled(this) && !this.shouldDisplayOriginal.get());
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        if (spellMessage.isEmpty()) return;

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        spellMessage,
                        this.getRenderX(),
                        this.getRenderX() + this.getWidth(),
                        this.getRenderY(),
                        this.getRenderY() + this.getHeight(),
                        this.getWidth(),
                        CommonColors.WHITE,
                        this.getRenderHorizontalAlignment(),
                        this.getRenderVerticalAlignment(),
                        textShadow.get(),
                        fontScale.get());
    }

    private enum MessageStyle {
        MODERN,
        LEGACY
    }
}
