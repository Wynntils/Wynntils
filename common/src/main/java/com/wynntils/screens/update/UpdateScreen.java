/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.update;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.changelog.ChangelogScreen;
import com.wynntils.services.athena.type.UpdateResult;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.UniversalTexture;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

public final class UpdateScreen extends WynntilsScreen {
    private final Screen titleScreen;
    private final ServerData serverData;

    private Button updateButton;
    private Button updateNowButton;
    private Button ignoreNowButton;
    private Button ignoreUpdateButton;
    private Button changelogButton;
    private Button titleScreenButton;
    private UpdateResult updateResult;

    private CompletionTrigger completionTrigger = null;
    private long completionFinish = 0L;

    private UpdateScreen(ServerData serverData, Screen titleScreen) {
        super(Component.literal("Update Screen"));

        this.serverData = serverData;
        this.titleScreen = titleScreen;
    }

    public static Screen create(ServerData serverData, Screen titleScreen) {
        return new UpdateScreen(serverData, titleScreen);
    }

    @Override
    protected void doInit() {
        super.doInit();

        updateButton = new Button.Builder(
                        Component.translatable("screens.wynntils.update.update"), (button) -> downloadUpdate(false))
                .pos(this.width / 2 - 150, this.height / 2 + 40)
                .size(140, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.update.updateTooltip")))
                .build();
        this.addRenderableWidget(updateButton);

        updateNowButton = new Button.Builder(
                        Component.translatable("screens.wynntils.update.updateExit"), (button) -> downloadUpdate(true))
                .pos(this.width / 2 + 10, this.height / 2 + 40)
                .size(140, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.update.updateExitTooltip")))
                .build();
        this.addRenderableWidget(updateNowButton);

        ignoreNowButton = new Button.Builder(
                        Component.translatable("screens.wynntils.update.ignoreNow"), (button) -> ignoreUpdate(false))
                .pos(this.width / 2 - 150, this.height / 2 + 70)
                .size(140, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.update.ignoreNowTooltip")))
                .build();
        this.addRenderableWidget(ignoreNowButton);

        ignoreUpdateButton = new Button.Builder(
                        Component.translatable("screens.wynntils.update.ignorePermenantly"),
                        (button) -> ignoreUpdate(true))
                .pos(this.width / 2 + 10, this.height / 2 + 70)
                .size(140, 20)
                .tooltip(Tooltip.create(Component.translatable(
                        "screens.wynntils.update.ignorePermenantlyTooltip",
                        Services.Update.getModUpdateInfo().version())))
                .build();
        this.addRenderableWidget(ignoreUpdateButton);

        changelogButton = new Button.Builder(
                        Component.translatable("screens.wynntils.update.changelog"), (button) -> showChangelog())
                .pos(this.width / 2 - 150, this.height / 2 + 100)
                .size(140, 20)
                .tooltip(Tooltip.create(Component.translatable(
                        "screens.wynntils.update.changelogTooltip",
                        WynntilsMod.getVersion(),
                        Services.Update.getModUpdateInfo().version())))
                .build();
        this.addRenderableWidget(changelogButton);

        titleScreenButton = new Button.Builder(
                        Component.translatable("screens.wynntils.update.titleScreen"), (button) -> onClose())
                .pos(this.width / 2 + 10, this.height / 2 + 100)
                .size(140, 20)
                .build();
        this.addRenderableWidget(titleScreenButton);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        PoseStack poseStack = guiGraphics.pose();

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable(
                                        "screens.wynntils.update.title",
                                        Services.Update.getModUpdateInfo().version())
                                .withStyle(ChatFormatting.UNDERLINE)),
                        this.width / 2f,
                        30,
                        CommonColors.DARK_AQUA,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        3f);

        if (Services.Update.getUpdateProgress() != -1f) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(Component.translatable("screens.wynntils.update.downloading")),
                            this.width / 2f,
                            60,
                            CommonColors.AQUA,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            1.5f);

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString((int) (Services.Update.getUpdateProgress() * 100) + "%"),
                            this.width / 2f,
                            this.height / 2f - Texture.UNIVERSAL_BAR.height(),
                            CommonColors.LIGHT_GREEN,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            BufferedRenderUtils.drawColoredProgressBar(
                    poseStack,
                    guiGraphics.bufferSource,
                    Texture.UNIVERSAL_BAR,
                    CommonColors.LIGHT_GREEN,
                    this.width / 2f - Texture.UNIVERSAL_BAR.width(),
                    this.height / 2f - UniversalTexture.A.getHeight(),
                    this.width / 2f + Texture.UNIVERSAL_BAR.width(),
                    this.height / 2f + UniversalTexture.A.getHeight(),
                    0,
                    UniversalTexture.A.getTextureY1(),
                    Texture.UNIVERSAL_BAR.width(),
                    UniversalTexture.A.getTextureY2(),
                    Services.Update.getUpdateProgress());
            return;
        }

        if (updateResult != null) {
            if (updateResult == UpdateResult.SUCCESSFUL) {
                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                StyledText.fromComponent(Component.translatable("screens.wynntils.update.downloaded")),
                                this.width / 2f,
                                60,
                                CommonColors.GREEN,
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.MIDDLE,
                                TextShadow.NORMAL,
                                1.5f);

                if (completionTrigger == CompletionTrigger.CONNECT) {
                    FontRenderer.getInstance()
                            .renderText(
                                    poseStack,
                                    StyledText.fromComponent(Component.translatable(
                                            "screens.wynntils.update.connecting",
                                            (int) Math.ceil((completionFinish - System.currentTimeMillis()) / 1000f))),
                                    this.width / 2f,
                                    100,
                                    CommonColors.AQUA,
                                    HorizontalAlignment.CENTER,
                                    VerticalAlignment.MIDDLE,
                                    TextShadow.NORMAL,
                                    1.5f);
                } else {
                    FontRenderer.getInstance()
                            .renderText(
                                    poseStack,
                                    StyledText.fromComponent(
                                            Component.translatable("screens.wynntils.update.exiting", (int) Math.ceil(
                                                    (completionFinish - System.currentTimeMillis()) / 1000f))),
                                    this.width / 2f,
                                    100,
                                    CommonColors.AQUA,
                                    HorizontalAlignment.CENTER,
                                    VerticalAlignment.MIDDLE,
                                    TextShadow.NORMAL,
                                    1.5f);
                }
            } else {
                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                StyledText.fromComponent(updateResult.getMessage()),
                                this.width / 2f,
                                60,
                                CommonColors.RED,
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.MIDDLE,
                                TextShadow.NORMAL,
                                1.5f);
            }

            return;
        }

        if (completionTrigger != null) return;

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.update.description")),
                        this.width / 2f - 200,
                        this.width / 2f + 200,
                        60,
                        120,
                        400,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1.5f);
    }

    private void downloadUpdate(boolean exit) {
        if (exit) {
            completionTrigger = CompletionTrigger.EXIT;
        } else {
            completionTrigger = CompletionTrigger.CONNECT;
        }

        Services.Update.setHasPromptedUpdate(true);
        toggleButtons(false);

        Services.Update.tryUpdate().thenAccept(result -> {
            if (result == UpdateResult.SUCCESSFUL) {
                completionFinish = System.currentTimeMillis() + 3000L;
                Executors.newSingleThreadScheduledExecutor()
                        .schedule(
                                () -> {
                                    // This has to be done on the main thread
                                    McUtils.mc().execute(() -> {
                                        if (exit) {
                                            System.exit(0);
                                        } else {
                                            connectToServer();
                                        }
                                    });
                                },
                                3000,
                                TimeUnit.MILLISECONDS);
            } else {
                toggleButtons(true);
            }

            this.updateResult = result;
        });
    }

    private void ignoreUpdate(boolean ignorePermenantly) {
        if (ignorePermenantly) {
            Services.Update.ignoredUpdate.store(
                    Services.Update.getModUpdateInfo().version());
        }

        Services.Update.setHasPromptedUpdate(true);
        connectToServer();
    }

    private void showChangelog() {
        Services.Update.getChangelog(
                        WynntilsMod.getVersion(),
                        Services.Update.getModUpdateInfo().version(),
                        false)
                .thenAccept(changelog -> {
                    if (changelog == null || changelog.isEmpty()) {
                        changelogButton.setMessage(Component.translatable("screens.wynntils.update.changelog")
                                .withStyle(ChatFormatting.RED));
                        changelogButton.setTooltip(
                                Tooltip.create(Component.translatable("screens.wynntils.update.changelogFailed")));
                        return;
                    }
                    Managers.TickScheduler.scheduleNextTick(
                            () -> McUtils.setScreen(ChangelogScreen.create(changelog, this)));
                });
    }

    private void connectToServer() {
        // We pass in the titleScreen here so that if failing to connect the title screen is returned to instead of this
        ConnectScreen.startConnecting(
                titleScreen, McUtils.mc(), ServerAddress.parseString(serverData.ip), serverData, false, null);
    }

    private void toggleButtons(boolean active) {
        updateButton.active = active;
        updateNowButton.active = active;
        ignoreNowButton.active = active;
        ignoreUpdateButton.active = active;
        changelogButton.active = active;
        titleScreenButton.active = active;
    }

    private enum CompletionTrigger {
        CONNECT,
        EXIT
    }
}
