/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Consumer;

import com.wynntils.features.WynncraftButtonFeature;
import com.wynntils.framework.events.EventBus;
import com.wynntils.framework.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class WynntilsMod {
    public static final String MOD_ID = "wynntils";

    public static EventBus eventBus;

    static {
        eventBus = new EventBus();
    }

    public static final Feature[] features = new Feature[]{new WynncraftButtonFeature()};

    public static void init() {
        System.out.println("Wynntils initialized");

    }
}
