/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.screens.mythicblacksmith.widgets.ConfirmWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import java.util.Optional;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class MythicBlacksmithWarnFeature extends Feature {
    @Persisted
    public final Config<Boolean> emphasizeMythics = new Config<>(true);

    @Persisted
    public final Config<Boolean> tomesWarning = new Config<>(false);

    private static final String BLACKSMITH_TITLE = "\uDAFF\uDFF8\uE016";
    private static final ResourceLocation CIRCLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/wynn/gui/tutorial.png");
    private static final int CONFIRM_BUTTON_SLOT = 17;

    private int emphasizeAnimationFrame = 0; // 0-indexed 4 frames of animation
    private int emphasizeAnimationDelay = 0;
    private int emphasizeDirection = 1; // 1 for forward, -1 for reverse

    private ConfirmWidget confirmWidget;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        if (!emphasizeMythics.get()) return;
        if (!(e.getScreen() instanceof ContainerScreen cs)
                || !cs.getTitle().getString().equals(BLACKSMITH_TITLE)) return;

        int indexOfMythic = cs.getMenu().getItems().indexOf(e.getSlot().getItem());
        if (indexOfMythic > 24 || indexOfMythic < 11) return; // Not in the sell slots

        Optional<WynnItem> item = Models.Item.getWynnItem(e.getSlot().getItem());
        if (item.isEmpty() || !(item.get() instanceof GearTierItemProperty gtip)) return;

        if (gtip.getGearTier() != GearTier.UNIQUE) return;

        if (item.get() instanceof TomeItem && !tomesWarning.get()) return;

        RenderSystem.enableDepthTest();
        RenderUtils.drawTexturedRectWithColor(
                e.getPoseStack(),
                CIRCLE_TEXTURE,
                CommonColors.RED,
                e.getSlot().x - 16,
                e.getSlot().y - 16,
                200,
                48,
                48,
                0,
                emphasizeAnimationFrame * 48,
                48,
                48,
                48,
                192);
        RenderSystem.disableDepthTest();
    }

    @SubscribeEvent
    public void onSetSlot(SetSlotEvent.Post e) {
        if (!(McUtils.mc().screen instanceof ContainerScreen cs)
                || !cs.getTitle().getString().equals(BLACKSMITH_TITLE)) return;

        for (int i = 11; i <= 24; i++) {
            Optional<GearTierItemProperty> optGearTier =
                    Models.Item.asWynnItemProperty(cs.getMenu().getItems().get(i), GearTierItemProperty.class);

            if (optGearTier.isPresent() && optGearTier.get().getGearTier() == GearTier.UNIQUE) {
                initConfirmSliderWidget(cs);
                return;
            }
        }

        McUtils.mc().screen.removeWidget(confirmWidget);
        confirmWidget = null;
    }

    @SubscribeEvent
    public void onSlotClicked(ContainerClickEvent e) {
        if (e.getSlotNum() == CONFIRM_BUTTON_SLOT && confirmWidget != null && !confirmWidget.isConfirmed()) {
            e.setCanceled(true);

            for (int i = 0; i < 12; i += 6) {
                Managers.TickScheduler.scheduleLater(() -> confirmWidget.setTextColor(CommonColors.RED), i);
                Managers.TickScheduler.scheduleLater(() -> confirmWidget.setTextColor(CommonColors.WHITE), i + 3);
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (!emphasizeMythics.get()) return;

        if (++emphasizeAnimationDelay % 4 == 0) {
            emphasizeAnimationFrame += emphasizeDirection;
            if (emphasizeAnimationFrame == 4 || emphasizeAnimationFrame == -1) {
                emphasizeDirection *= -1;
                emphasizeAnimationFrame =
                        Math.max(0, Math.min(3, emphasizeAnimationFrame)); // Keeps the frame within bounds
            }
        }
    }

    private void initConfirmSliderWidget(ContainerScreen cs) {
        if (confirmWidget != null) return;
        confirmWidget = new ConfirmWidget(cs.leftPos, cs.topPos - 21, cs.width - 2 * cs.leftPos, 14);
        cs.addRenderableWidget(confirmWidget);
    }
}
