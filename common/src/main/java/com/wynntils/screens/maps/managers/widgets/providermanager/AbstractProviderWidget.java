/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.screens.maps.managers.type.ProviderType;
import com.wynntils.services.mapdata.attributes.DefaultMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractProviderWidget extends AbstractWidget {
    protected final ProviderType providerType;
    protected List<Component> generatedTooltip = new ArrayList<>();
    public boolean display = true;

    protected AbstractProviderWidget(
            Component label,
            int height,
            ProviderType providerType) {
        super(0, 0, 0, height, label);
        this.providerType = providerType;
    }

    public ProviderType getProviderType() {
        return providerType;
    }
}
