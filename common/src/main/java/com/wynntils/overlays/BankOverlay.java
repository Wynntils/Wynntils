/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.Reference;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.mc.event.ContainerScreenInitEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BankOverlay extends Overlay {
    private static final Pattern PAGE_PATTERN =
            Pattern.compile("\\[Pg\\. ([0-9]*)\\] [a-z_A-Z0-9 ]+'s? Bank");
    private static final Pattern PAGE_CUSTOM_NAME_PATTERN =
            Pattern.compile("\\[Pg\\. ([0-9]*)\\] ([a-z_A-Z0-9 ]+)");

    private Screen matchingScreen = null;

    public BankOverlay() {
        super(0, 0, true, OverlayGrowFrom.CENTER, OverlayGrowFrom.CENTER);
    }

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        matchingScreen = null;
    }

    @Override
    protected boolean onEnable() {
        WynntilsMod.getEventBus().register(this);
        return true;
    }

    @Override
    protected void onDisable() {
        WynntilsMod.getEventBus().unregister(this);
    }

    @Override
    public String getName() {
        return "Bank Overlay";
    }

    @SubscribeEvent
    public void onContainerScreenInit(ContainerScreenInitEvent event) {
        ContainerScreen container = event.getContainerScreen();

        String containerTitle =
                ChatFormatting.stripFormatting(ComponentUtils.getUnformatted(container.getTitle()));

        if (containerTitle == null) {
            matchingScreen = null;
            return;
        }
        boolean isBank;

        Matcher pageMatcher = PAGE_PATTERN.matcher(containerTitle);

        if (pageMatcher.matches()) {
            isBank = true;
        } else {
            Matcher customPageMatcher = PAGE_CUSTOM_NAME_PATTERN.matcher(containerTitle);
            isBank = customPageMatcher.matches();
        }

        if (!isBank) {
            matchingScreen = null;
            return;
        }

        this.staticSize = new Point(container.width, container.height);
        matchingScreen = container;
    }

    @Override
    public void render(RenderEvent.Pre e) {
        if (matchingScreen == null || matchingScreen != McUtils.mc().screen) return;

        Reference.LOGGER.info("Log pre bank render");
    }

    @Override
    public void render(RenderEvent.Post e) {
        if (matchingScreen == null || matchingScreen != McUtils.mc().screen) return;

        Reference.LOGGER.info("Log post bank render");
    }

    @Override
    public void tick() {
        if (matchingScreen == null || matchingScreen != McUtils.mc().screen) return;

        Reference.LOGGER.info("Log tick bank render");
    }
}
