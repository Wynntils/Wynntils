/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.worlds.type.BombInfo;
import com.wynntils.models.worlds.type.BombType;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;

public class BombBellOverlay extends Overlay {
    @Persisted
    private final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

    @Persisted
    private final Config<Float> fontScale = new Config<>(1.0f);

    @Persisted
    private final Config<Boolean> groupBombs = new Config<>(true);

    @Persisted
    private final Config<Integer> maxBombs = new Config<>(5);

    @Persisted
    private final Config<SortOrder> sortOrder = new Config<>(SortOrder.NEWEST);

    @Persisted
    private final Config<Boolean> showCombatBombs = new Config<>(true);

    @Persisted
    private final Config<Boolean> showDungeonBombs = new Config<>(true);

    @Persisted
    private final Config<Boolean> showLootBombs = new Config<>(true);

    @Persisted
    private final Config<Boolean> showLootChestBombs = new Config<>(true);

    @Persisted
    private final Config<Boolean> showProfessionXpBombs = new Config<>(true);

    @Persisted
    private final Config<Boolean> showProfessionSpeedBombs = new Config<>(true);

    private final Map<BombType, Supplier<Boolean>> bombTypeMap = Map.ofEntries(
            Map.entry(BombType.COMBAT_XP, showCombatBombs::get),
            Map.entry(BombType.DUNGEON, showDungeonBombs::get),
            Map.entry(BombType.LOOT, showLootBombs::get),
            Map.entry(BombType.LOOT_CHEST, showLootChestBombs::get),
            Map.entry(BombType.PROFESSION_XP, showProfessionXpBombs::get),
            Map.entry(BombType.PROFESSION_SPEED, showProfessionSpeedBombs::get));

    private Comparator<BombInfo> comparator;
    private TextRenderSetting textRenderSetting;

    private List<TextRenderTask> renderTasks = new ArrayList<>();

    public BombBellOverlay() {
        super(
                new OverlayPosition(
                        130,
                        -5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.TOP_RIGHT),
                new OverlaySize(200, 110));

        updateTextRenderSetting();
    }

    @Override
    protected boolean hideWhenNoGui() {
        return false;
    }

    @Override
    protected boolean isVisible() {
        return !Models.Bomb.getBombBells().isEmpty();
    }

    @Override
    public void render(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        BufferedFontRenderer.getInstance()
                .renderTextsWithAlignment(
                        guiGraphics.pose(),
                        bufferSource,
                        this.getRenderX(),
                        this.getRenderY(),
                        renderTasks,
                        this.getWidth(),
                        this.getHeight(),
                        this.getRenderHorizontalAlignment(),
                        this.getRenderVerticalAlignment(),
                        fontScale.get());
    }

    @Override
    public void renderPreview(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        BufferedFontRenderer.getInstance()
                .renderTextsWithAlignment(
                        guiGraphics.pose(),
                        bufferSource,
                        this.getRenderX(),
                        this.getRenderY(),
                        List.of(
                                new TextRenderTask(
                                        StyledText.fromString("§6Combat XP§7 on §fWC32 §6(16m 35s)"),
                                        textRenderSetting),
                                new TextRenderTask(
                                        StyledText.fromString("§6Profession Speed§7 on §fWC1 §6(3m 12s)"),
                                        textRenderSetting)),
                        this.getWidth(),
                        this.getHeight(),
                        this.getRenderHorizontalAlignment(),
                        this.getRenderVerticalAlignment(),
                        fontScale.get());
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        comparator = sortOrder.get() == SortOrder.NEWEST
                ? Comparator.comparing(BombInfo::getRemainingLong).reversed()
                : Comparator.comparing(BombInfo::getRemainingLong);
        updateTextRenderSetting();
    }

    @Override
    public void tick() {
        Stream<BombInfo> filteredBombs = Models.Bomb.getBombBells().stream().filter(bombInfo -> {
            BombType bombType = bombInfo.bomb();
            Supplier<Boolean> bombTypeSupplier = bombTypeMap.get(bombType);
            return bombTypeSupplier != null && bombTypeSupplier.get();
        });

        Stream<BombInfo> processedBombs = groupBombs.get()
                ? filteredBombs.collect(Collectors.groupingBy(BombInfo::bomb)).values().stream()
                        .flatMap(list -> list.stream().sorted(comparator).limit(maxBombs.get()))
                : filteredBombs.sorted(comparator).limit(maxBombs.get());

        renderTasks = processedBombs
                .map(bombInfo -> new TextRenderTask(bombInfo.asString(), textRenderSetting))
                .toList();
    }

    private void updateTextRenderSetting() {
        textRenderSetting = TextRenderSetting.DEFAULT
                .withMaxWidth(this.getWidth())
                .withHorizontalAlignment(this.getRenderHorizontalAlignment())
                .withTextShadow(textShadow.get());
    }

    private enum SortOrder {
        NEWEST,
        OLDEST
    }
}
