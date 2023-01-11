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
import com.wynntils.gui.render.RenderUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CustomNametagRendererFeature extends UserFeature {
    // how much larger account tags should be relative to gear lines
    private static final float ACCOUNT_TYPE_MULTIPLIER = 1.5f;
    private static final float NAMETAG_HEIGHT = 0.25875f;

    @Config
    public boolean hideAllNametags = false;

    @Config
    public boolean showGearOnHover = true;

    @Config
    public float customNametagScale = 0.5f;

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

        List<CustomNametag> nametags = new ArrayList<>();

        if (showGearOnHover) {
            addGearNametags(event, nametags);
        }

        addAccountTypeNametag(event, nametags);

        // need to handle the rendering ourselves
        if (!nametags.isEmpty()) {
            event.setCanceled(true);
            drawNametags(event, nametags);
        }
    }

    @SubscribeEvent
    public void onRenderLevel(RenderLevelEvent.Pre event) {
        Optional<Player> hitPlayer = RaycastUtils.getHoveredPlayer();
        hitPlayerCache = hitPlayer.orElse(null);
    }

    private void addGearNametags(NametagRenderEvent event, List<CustomNametag> nametags) {
        LocalPlayer player = McUtils.player();

        if (hitPlayerCache != event.getEntity()) return;

        if (!WynnPlayerUtils.isLocalPlayer(player)) return;

        ItemStack heldItem = hitPlayerCache.getMainHandItem();
        MutableComponent handComp = getItemComponent(heldItem);
        if (handComp != null) nametags.add(new CustomNametag(handComp, customNametagScale));

        for (ItemStack armorStack : hitPlayerCache.getArmorSlots()) {
            MutableComponent armorComp = getItemComponent(armorStack);
            if (armorComp != null) nametags.add(new CustomNametag(armorComp, customNametagScale));
        }
    }

    private static MutableComponent getItemComponent(ItemStack itemStack) {
        if (itemStack == null || itemStack == ItemStack.EMPTY) return null;

        String itemName = WynnItemUtils.getTranslatedName(itemStack);

        if (itemName.contains("Crafted")) {
            return Component.literal(itemName).withStyle(ChatFormatting.DARK_AQUA);
        }

        GearProfile gearProfile = Managers.GearProfiles.getItemsProfile(itemName);
        if (gearProfile == null) return null;

        // this solves an unidentified item showcase exploit
        // boxes items are STONE_SHOVEL, 1 represents UNIQUE boxes and 6 MYTHIC boxes
        if (itemStack.getItem() == Items.STONE_SHOVEL
                && itemStack.getDamageValue() >= 1
                && itemStack.getDamageValue() <= 6) {
            return Component.literal("Unidentified Item")
                    .withStyle(gearProfile.getTier().getChatFormatting());
        }

        return Component.literal(gearProfile.getDisplayName())
                .withStyle(gearProfile.getTier().getChatFormatting());
    }

    private void addAccountTypeNametag(NametagRenderEvent event, List<CustomNametag> nametags) {
        WynntilsUser user =
                Models.RemoteWynntilsUserInfo.getUser(event.getEntity().getUUID());
        if (user == null) return;
        AccountType accountType = user.accountType();
        if (accountType.getComponent() == null) return;

        nametags.add(new CustomNametag(accountType.getComponent(), customNametagScale * ACCOUNT_TYPE_MULTIPLIER));
    }

    private void drawNametags(NametagRenderEvent event, List<CustomNametag> nametags) {
        // calculate color of nametag box
        int backgroundColor = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255f) << 24;

        // add vanilla nametag to list
        nametags.add(new CustomNametag(event.getDisplayName(), 1f));

        float yOffset = 0f;
        for (CustomNametag nametag : nametags) {
            // move rendering up to fit the next line, plus a small gap
            yOffset += nametag.getScale() * NAMETAG_HEIGHT;

            RenderUtils.renderCustomNametag(
                    event.getPoseStack(),
                    event.getBuffer(),
                    event.getPackedLight(),
                    backgroundColor,
                    event.getEntityRenderDispatcher(),
                    event.getEntity(),
                    nametag.getComponent(),
                    event.getFont(),
                    nametag.getScale(),
                    yOffset);
        }
    }

    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.RemoteWynntilsUserInfo);
    }

    private static class CustomNametag {
        private final Component nametagComponent;
        private final float nametagScale;

        public CustomNametag(Component nametagComponent, float nametagScale) {
            this.nametagComponent = nametagComponent;
            this.nametagScale = nametagScale;
        }

        public Component getComponent() {
            return nametagComponent;
        }

        public float getScale() {
            return nametagScale;
        }
    }
}
