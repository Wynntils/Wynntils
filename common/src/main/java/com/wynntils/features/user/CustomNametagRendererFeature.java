/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.gui.screens.GearViewerScreen;
import com.wynntils.mc.event.NametagRenderEvent;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.objects.account.AccountType;
import com.wynntils.wynn.objects.account.WynntilsUser;
import com.wynntils.wynn.objects.profiles.item.GearProfile;
import com.wynntils.wynn.utils.RaycastUtils;
import com.wynntils.wynn.utils.WynnItemUtils;
import com.wynntils.wynn.utils.WynnPlayerUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CustomNametagRendererFeature extends UserFeature {
    @Config
    public boolean hideAllNametags = false;

    @Config
    public boolean showGearOnHover = true;

    private Player hitPlayerCache = null;

    @SubscribeEvent
    public void onNameTagRender(NametagRenderEvent event) {
        if (hideAllNametags) {
            event.setCanceled(true);
            return;
        }

        // If we are viewing this player's gears, do not show plus info
        if (McUtils.mc().screen instanceof GearViewerScreen gearViewerScreen
                && gearViewerScreen.getPlayer() == event.getEntity()) {
            return;
        }

        if (showGearOnHover) {
            addGearNametag(event);
        }

        addAccountTypeNametag(event);
    }

    @SubscribeEvent
    public void onRenderLevel(RenderLevelEvent.Pre event) {
        Optional<Player> hitPlayer = RaycastUtils.getHoveredPlayer();
        hitPlayerCache = hitPlayer.orElse(null);
    }

    private void addGearNametag(NametagRenderEvent event) {
        LocalPlayer player = McUtils.player();

        if (hitPlayerCache != event.getEntity()) return;

        if (!WynnPlayerUtils.isLocalPlayer(player)) return;

        ItemStack heldItem = hitPlayerCache.getMainHandItem();
        if (heldItem != null) {
            getItemComponent(event, heldItem);
        }

        for (ItemStack armorStack : hitPlayerCache.getArmorSlots()) {
            getItemComponent(event, armorStack);
        }
    }

    private static void getItemComponent(NametagRenderEvent event, ItemStack itemStack) {
        String itemName = WynnItemUtils.getTranslatedName(itemStack);

        if (itemName.contains("Crafted")) {
            event.addInjectedLine(Component.literal(itemName).withStyle(ChatFormatting.DARK_AQUA));
            return;
        }

        GearProfile gearProfile = Managers.GearProfiles.getItemsProfile(itemName);
        if (gearProfile == null) return;

        // this solves an unidentified item showcase exploit
        // boxes items are STONE_SHOVEL, 1 represents UNIQUE boxes and 6 MYTHIC boxes
        if (itemStack.getItem() == Items.STONE_SHOVEL
                && itemStack.getDamageValue() >= 1
                && itemStack.getDamageValue() <= 6) {
            event.addInjectedLine(Component.literal("Unidentified Item")
                    .withStyle(gearProfile.getTier().getChatFormatting()));
            return;
        }

        event.addInjectedLine(Component.literal(gearProfile.getDisplayName())
                .withStyle(gearProfile.getTier().getChatFormatting()));
    }

    private static void addAccountTypeNametag(NametagRenderEvent event) {
        WynntilsUser user =
                Models.RemoteWynntilsUserInfo.getUser(event.getEntity().getUUID());
        if (user == null) return;
        AccountType accountType = user.accountType();
        if (accountType.getComponent() == null) return;

        event.addInjectedLine(accountType.getComponent());
    }

    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.RemoteWynntilsUserInfo);
    }
}
