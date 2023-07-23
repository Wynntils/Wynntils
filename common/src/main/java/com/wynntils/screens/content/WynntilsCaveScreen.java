/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.content;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.models.content.CaveInfo;
import com.wynntils.models.content.event.ContentTrackerUpdatedEvent;
import com.wynntils.models.content.event.ContentUpdatedEvent;
import com.wynntils.models.content.type.ContentSortOrder;
import com.wynntils.models.content.type.ContentType;
import com.wynntils.screens.base.WynntilsListScreen;
import com.wynntils.screens.base.widgets.BackButton;
import com.wynntils.screens.base.widgets.PageSelectorButton;
import com.wynntils.screens.base.widgets.ReloadButton;
import com.wynntils.screens.base.widgets.SortOrderWidget;
import com.wynntils.screens.base.widgets.SortableContentScreen;
import com.wynntils.screens.content.widgets.CaveButton;
import com.wynntils.screens.content.widgets.CaveProgressButton;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class WynntilsCaveScreen extends WynntilsListScreen<CaveInfo, CaveButton>
        implements SortableContentScreen {
    private ContentSortOrder contentSortOrder = ContentSortOrder.LEVEL;
    private CaveInfo trackingRequested = null;

    private WynntilsCaveScreen() {
        super(Component.translatable("screens.wynntils.wynntilsCaveBook.name"));

        // Only register this once
        WynntilsMod.registerEventListener(this);
    }

    @SubscribeEvent
    public void onCaveUpdate(ContentUpdatedEvent event) {
        if (event.getContentType() == ContentType.CAVE && McUtils.mc().screen == this) {
            this.reloadElements();
        }
    }

    @SubscribeEvent
    public void onTrackedContentUpdate(ContentTrackerUpdatedEvent event) {
        // Reload so we have the proper order
        if (McUtils.mc().screen == this) {
            this.reloadElements();
        }
    }

    // FIXME: We only need this hack to stop the screen from closing when tracking Caves.
    //        Adding a proper way to add quests with scripted container queries would mean this can get removed.
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onMenuClose(MenuEvent.MenuClosedEvent event) {
        if (McUtils.mc().screen != this) return;

        event.setCanceled(true);
    }

    @Override
    public void onClose() {
        WynntilsMod.unregisterEventListener(this);

        super.onClose();
    }

    public static WynntilsCaveScreen create() {
        return new WynntilsCaveScreen();
    }

    @Override
    protected void doInit() {
        Models.Cave.reloadCaves();

        super.doInit();

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW.width() / 2,
                Texture.BACK_ARROW.height(),
                WynntilsMenuScreen.create()));

        this.addRenderableWidget(new ReloadButton(
                Texture.QUEST_BOOK_BACKGROUND.width() - 21,
                11,
                (int) (Texture.RELOAD_BUTTON.width() / 2 / 1.7f),
                (int) (Texture.RELOAD_BUTTON.height() / 1.7f),
                "cave",
                Models.Cave::reloadCaves));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 50 - Texture.FORWARD_ARROW.width() / 2,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() - 50,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                true,
                this));

        this.addRenderableWidget(new SortOrderWidget(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 1,
                11,
                (int) (Texture.SORT_DISTANCE.width() / 1.7f),
                (int) (Texture.SORT_DISTANCE.height() / 2 / 1.7f),
                this));
        this.addRenderableWidget(new CaveProgressButton(
                (int) (Texture.QUEST_BOOK_BACKGROUND.width() / 4f), 10, Texture.CAVE.width(), Texture.CAVE.height()));

        reloadElements();
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = getTranslationX();
        final float translationY = getTranslationY();
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsCaves.name"));

        renderVersion(poseStack);

        renderWidgets(poseStack, mouseX, mouseY, partialTick);

        if (elements.isEmpty()) {
            renderNoCavesHelper(poseStack);
        }

        renderDescription(poseStack, I18n.get("screens.wynntils.wynntilsCaves.screenDescription"), "");

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        poseStack.popPose();

        renderTooltip(poseStack, mouseX, mouseY);
    }

    private void renderNoCavesHelper(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.wynntilsCaves.tryReload")),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f + 15f,
                        Texture.QUEST_BOOK_BACKGROUND.width() - 15f,
                        0,
                        Texture.QUEST_BOOK_BACKGROUND.height(),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30f,
                        CommonColors.BLACK,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE);
    }

    @Override
    protected CaveButton getButtonFromElement(int i) {
        int offset = i % getElementsPerPage();
        return new CaveButton(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 15,
                offset * 13 + 25,
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 - 37,
                9,
                elements.get(i),
                this);
    }

    @Override
    protected void reloadElementsList(String searchTerm) {
        elements.addAll(Models.Cave.getSortedCaves(contentSortOrder).stream()
                .filter(info -> StringUtils.partialMatch(info.getName(), searchTerm))
                .toList());
    }

    @Override
    public ContentSortOrder getContentSortOrder() {
        return contentSortOrder;
    }

    @Override
    public void setContentSortOrder(ContentSortOrder newSortOrder) {
        if (newSortOrder == null) {
            throw new IllegalStateException("Tried to set null content sort order");
        }

        this.contentSortOrder = newSortOrder;
        this.setCurrentPage(0);
    }

    public void setTrackingRequested(CaveInfo caveInfo) {
        this.trackingRequested = caveInfo;
    }

    public CaveInfo getTrackingRequested() {
        return trackingRequested;
    }
}
