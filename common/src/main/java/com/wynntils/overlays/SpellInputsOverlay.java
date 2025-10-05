/*
 * Copyright © Wynntils 2025.
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
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Arrays;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;

public class SpellInputsOverlay extends Overlay {
    // Upon reaching level 11, the spell inputs are displayed using the smaller icon
    private static final int SMALL_CHARACTERS_LEVEL = 11;
    private static final ResourceLocation SPELL_INPUTS_FONT =
            ResourceLocation.withDefaultNamespace("hud/gameplay/default/bottom_middle");

    private static final String FULL_NO_CLICK_CHARACTER = "\uE102";
    private static final String SMALL_NO_CLICK_CHARACTER = "\uE105";
    private static final String SEPARATOR_CHARACTER = "\uE106";

    @Persisted
    private final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    @Persisted
    private final Config<SpellInputStyle> inputStyle = new Config<>(SpellInputStyle.ORIGINAL);

    @Persisted
    private final Config<TextShadow> textShadow = new Config<>(TextShadow.NONE);

    @Persisted
    protected final Config<Float> fontScale = new Config<>(1.0f);

    private StyledText spellText = StyledText.EMPTY;

    public SpellInputsOverlay() {
        super(
                new OverlayPosition(
                        -62,
                        0,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(100, 20),
                HorizontalAlignment.CENTER,
                VerticalAlignment.BOTTOM);
    }

    @SubscribeEvent
    public void onSpellCast(SpellEvent.Partial event) {
        SpellDirection[] dirs = event.getSpellDirectionArray();
        SpellInputStyle style = inputStyle.get();

        spellText = switch (style) {
            case SMALL -> buildUnicodeInputs(dirs, true);
            case FULL -> buildUnicodeInputs(dirs, false);
            case ORIGINAL -> buildUnicodeInputs(dirs, Models.CharacterStats.getLevel() >= SMALL_CHARACTERS_LEVEL);
            case LEGACY -> buildLegacyInputs(dirs);
        };
    }

    @SubscribeEvent
    public void onSpellExpired(SpellEvent.Expired event) {
        spellText = StyledText.EMPTY;
    }

    @SubscribeEvent
    public void onWorldStateChanged(WorldStateEvent event) {
        spellText = StyledText.EMPTY;
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        Models.Spell.setHideSpellInputs(Managers.Overlay.isEnabled(this) && !this.shouldDisplayOriginal.get());
    }

    @Override
    public void render(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        if (spellText.isEmpty()) return;

        BufferedFontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics.pose(),
                        bufferSource,
                        spellText,
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

    @Override
    public void renderPreview(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        BufferedFontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics.pose(),
                        bufferSource,
                        buildUnicodeInputs(new SpellDirection[] {SpellDirection.LEFT, SpellDirection.RIGHT}, true),
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

    private StyledText buildUnicodeInputs(SpellDirection[] dirs, boolean small) {
        String[] icons = Arrays.stream(dirs)
                .map(d -> small ? d.getSmallIcon() : d.getFullIcon())
                .toArray(String[]::new);

        String noClick = small ? SMALL_NO_CLICK_CHARACTER : FULL_NO_CLICK_CHARACTER;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i > 0) {
                sb.append(" ").append(SEPARATOR_CHARACTER).append(" ");
            }

            sb.append(i < icons.length ? icons[i] : noClick);
        }

        MutableComponent component =
                Component.literal(sb.toString()).withStyle(Style.EMPTY.withFont(SPELL_INPUTS_FONT));

        return StyledText.fromComponent(component);
    }

    private StyledText buildLegacyInputs(SpellDirection[] dirs) {
        MutableComponent component = Component.empty();

        String[] icons = Arrays.stream(dirs).map(d -> d.name().substring(0, 1)).toArray(String[]::new);

        int firstMissingIndex = icons.length;

        for (int i = 0; i < 3; i++) {
            if (i < icons.length) {
                component.append(Component.literal(icons[i]).withStyle(style -> style.withColor(ChatFormatting.GREEN)));
            } else {
                MutableComponent noInput =
                        Component.literal("?").withStyle(style -> style.withColor(ChatFormatting.GRAY));

                if (i == firstMissingIndex) {
                    noInput = noInput.withStyle(style -> style.withUnderlined(true));
                }

                component.append(noInput);
            }

            if (i != 2) {
                component.append(Component.literal("-").withStyle(style -> style.withColor(ChatFormatting.GRAY)));
            }
        }

        return StyledText.fromComponent(component);
    }

    private enum SpellInputStyle {
        ORIGINAL,
        FULL,
        SMALL,
        LEGACY
    }
}
