/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigProviders;
import com.wynntils.core.config.Configurable;
import com.wynntils.core.config.reflection.ConfigField;
import com.wynntils.core.config.reflection.ConfigReflection;
import com.wynntils.core.config.ui.base.ConfigWidget;
import com.wynntils.core.config.ui.base.VariableListWidget;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.ColorUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class ConfigScreen extends Screen {
    VariableListWidget entries;

    public ConfigScreen(Configurable config) {
        super(new TextComponent("Config"));

        List<ConfigField<?>> configFields = ConfigReflection.getConfigFields(config);

        entries = new VariableListWidget(width / 10, height / 10, width * 4 / 5, height * 4 / 5);

        if (config instanceof Feature feature) {}

        int y = 0;

        for (ConfigField<?> field : configFields) {
            Entry<?> entry = new Entry<>(field, width * 4 / 5);
            entries.addEntry(entry);
            y += entry.getHeight();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return entries.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        fill(poseStack, 0, 0, 10, 10, 0);

        entries.render(poseStack, mouseX, mouseY, partialTick);
    }

    public static class HeaderEntry implements VariableListWidget.ListEntry {
        private static final int textColor = ColorUtils.generateColor(0, 0, 0, 0);

        private final List<String> infos = new ArrayList<>();

        private final int width;

        public HeaderEntry(Feature feature, int width) {
            infos.add("Name:  " + feature.getName());

            for (Class<?> clazz = feature.getClass();
                    Feature.class.isAssignableFrom(clazz);
                    clazz = clazz.getSuperclass()) {
                FeatureInfo featureInfo = clazz.getAnnotation(FeatureInfo.class);

                if (featureInfo != null) {
                    infos.add("Stability: " + featureInfo.stability());
                    infos.add("Gameplay Impact: " + featureInfo.gameplay());
                    infos.add("Performance Impact: " + featureInfo.performance());
                }
            }

            this.width = width;
        }

        @Override
        public void render(
                PoseStack poseStack,
                int mouseX,
                int mouseY,
                boolean isMouseOver,
                float partialTick) {
            int currentY = 0;

            for (String info : infos) {
                drawString(poseStack, McUtils.mc().font, info, 0, currentY, textColor);
                currentY += 10;
            }
        }

        public int getHeight() {
            return infos.size() * 10;
        }

        public int getWidth() {
            return width;
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {}
    }

    public static class Entry<T> implements VariableListWidget.ListEntry {
        private final ConfigField<T> field;
        private final ConfigWidget<T> widget;

        private final int width;

        private static final int textColor = ColorUtils.generateColor(0, 0, 0, 0);

        public Entry(ConfigField<T> field, int width) {
            this.field = field;
            this.widget = ConfigProviders.generate(field);

            this.width = Math.max(width, widget.getWidth());

            widget.setXY(0, 20);
        }

        @Override
        public void render(
                PoseStack poseStack,
                int mouseX,
                int mouseY,
                boolean isMouseOver,
                float partialTick) {
            drawString(poseStack, McUtils.mc().font, field.getName(), 0, 0, textColor);
            drawString(poseStack, McUtils.mc().font, field.getDescription(), 0, 10, textColor);
            widget.render(poseStack, mouseX, mouseY, partialTick);
        }

        public int getHeight() {
            return 20 + widget.getHeight();
        }

        public int getWidth() {
            return width;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return widget.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {}
    }
}
