/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.request.Request;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.screens.WynntilsQuestBookScreen;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.Utils;
import com.wynntils.utils.WebUtils;
import com.wynntils.wynn.model.questbook.QuestBookManager;
import com.wynntils.wynn.model.questbook.QuestInfo;
import com.wynntils.wynn.model.questbook.QuestStatus;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class QuestButton extends AbstractButton {
    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);
    private static final CustomColor TRACKED_BUTTON_COLOR = new CustomColor(176, 197, 148);
    private static final CustomColor TRACKED_BUTTON_COLOR_HOVERED = new CustomColor(126, 211, 106);
    private final QuestInfo questInfo;
    private final WynntilsQuestBookScreen questBookScreen;

    public QuestButton(int x, int y, int width, int height, QuestInfo questInfo, WynntilsQuestBookScreen screen) {
        super(x, y, width, height, new TextComponent("Quest Button"));
        this.questInfo = questInfo;
        this.questBookScreen = screen;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor backgroundColor = questBookScreen.getTracked() == this.questInfo
                ? (this.isHovered ? TRACKED_BUTTON_COLOR_HOVERED : TRACKED_BUTTON_COLOR)
                : (this.isHovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR);

        RenderUtils.drawRect(poseStack, backgroundColor, this.x, this.y, 0, this.width, this.height);

        int maxTextWidth = this.width - 10 - 11;
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StringUtils.getMaxFittingText(
                                questInfo.getName(),
                                maxTextWidth,
                                FontRenderer.getInstance().getFont()),
                        this.x + 14,
                        this.y + 1,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.NONE);

        Texture stateTexture =
                switch (questInfo.getStatus()) {
                    case STARTED -> Texture.QUEST_STARTED;
                    case COMPLETED -> Texture.QUEST_FINISHED;
                    case CAN_START -> Texture.QUEST_CAN_START;
                    case CANNOT_START -> Texture.QUEST_CANNOT_START;
                };

        RenderUtils.drawTexturedRect(
                poseStack,
                stateTexture.resource(),
                this.x + 1,
                this.y + 1,
                stateTexture.width(),
                stateTexture.height(),
                stateTexture.width(),
                stateTexture.height());
    }

    // Not called
    @Override
    public void onPress() {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            trackQuest();
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            // TODO open map
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            openQuestWiki();
        }

        return true;
    }

    private void trackQuest() {
        if (this.questInfo.getStatus() != QuestStatus.CANNOT_START) {
            McUtils.soundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_LAND, 1.0F));
            QuestBookManager.trackQuest(this.questInfo);

            if (questBookScreen.getTracked() != this.questInfo) {
                questBookScreen.setTracked(this.questInfo);
            } else {
                questBookScreen.setTracked(null);
            }
        }
    }

    private void openQuestWiki() {
        final String baseUrl = "https://wynncraft.fandom.com/wiki/";

        // todo handle mini quest
        String name = this.questInfo.getName();
        String wikiQuestPageNameQuery = WebManager.getApiUrl("WikiQuestQuery");
        String url = wikiQuestPageNameQuery + WebUtils.encodeForCargoQuery(name);
        Request req = new RequestBuilder(url, "WikiQuestQuery")
                .handleJsonArray(jsonOutput -> {
                    String pageTitle = jsonOutput
                            .get(0)
                            .getAsJsonObject()
                            .get("_pageTitle")
                            .getAsString();
                    Utils.openUrl(baseUrl + WebUtils.encodeForWikiTitle(pageTitle));
                    return true;
                })
                .build();

        RequestHandler handler = new RequestHandler();

        handler.addAndDispatch(req, true);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    public QuestInfo getQuestInfo() {
        return questInfo;
    }
}
