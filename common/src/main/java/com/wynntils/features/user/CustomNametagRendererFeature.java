/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.profiles.item.ItemProfile;
import com.wynntils.mc.event.NametagRenderEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.model.UserInfoModel;
import com.wynntils.wynn.objects.account.AccountType;
import com.wynntils.wynn.utils.WynnPlayerUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CustomNametagRendererFeature extends UserFeature {
    private static final float RAYCAST_RANGE = 5f;

    @Config
    public boolean hideAllNametags = false;

    @Config
    public boolean showAccountType = true;

    @Config
    public boolean showGearOnHover = false;

    @SubscribeEvent
    public void onNameTagRender(NametagRenderEvent event) {
        if (hideAllNametags) {
            event.setCanceled(true);
            return;
        }

        if (showGearOnHover) {
            addGearNametag(event);
        }

        if (showAccountType) {
            addAccountTypeNametag(event);
        }
    }

    private static void addGearNametag(NametagRenderEvent event) {
        LocalPlayer player = McUtils.player();

        Vec3 start = player.getEyePosition(1f);
        Vec3 look = player.getLookAngle();
        Vec3 direction = start.add(look.x * RAYCAST_RANGE, look.y * RAYCAST_RANGE, look.z * RAYCAST_RANGE);
        AABB bb = player.getBoundingBox()
                .expandTowards(look.x * RAYCAST_RANGE, look.y * RAYCAST_RANGE, look.z * RAYCAST_RANGE)
                .expandTowards(1, 1, 1);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                McUtils.mc().level, player, start, direction, bb, (e) -> e instanceof Player);

        if (hitResult == null) return;
        if (hitResult.getEntity() != event.getEntity()) return;

        Player hitPlayer = (Player) hitResult.getEntity();

        if (hitPlayer.getScoreboardName().contains("§")) return; // npc
        if (WynnPlayerUtils.isPlayerGhost(hitPlayer)) return;

        ItemStack heldItem = hitPlayer.getMainHandItem();
        if (heldItem != null) {
            getItemComponent(event, heldItem);
        }

        for (ItemStack armorStack : hitPlayer.getArmorSlots()) {
            getItemComponent(event, armorStack);
        }
    }

    private static void getItemComponent(NametagRenderEvent event, ItemStack itemStack) {
        String itemName = ComponentUtils.getUnformatted(itemStack.getHoverName());

        if (itemName.contains("Crafted")) {
            event.addInjectedLine(new TextComponent(itemName).withStyle(ChatFormatting.DARK_AQUA));
            return;
        }

        ItemProfile itemProfile = WebManager.getItemsMap().get(itemName);
        if (itemProfile == null) return;

        // this solves an unidentified item showcase exploit
        // boxes items are STONE_SHOVEL, 1 represents UNIQUE boxes and 6 MYTHIC boxes
        if (itemStack.getItem() == Items.STONE_SHOVEL
                && itemStack.getDamageValue() >= 1
                && itemStack.getDamageValue() <= 6) {
            event.addInjectedLine(new TextComponent("Unidentified Item")
                    .withStyle(itemProfile.getTier().getChatFormatting()));
            return;
        }

        event.addInjectedLine(new TextComponent(itemProfile.getDisplayName())
                .withStyle(itemProfile.getTier().getChatFormatting()));
    }

    private static void addAccountTypeNametag(NametagRenderEvent event) {
        for (AccountType type : AccountType.values()) {
            if (type.getComponent() == null) continue;

            if (UserInfoModel.isAccountType(event.getEntity().getUUID(), type)) {
                event.addInjectedLine(type.getComponent());
            }
        }
    }

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(UserInfoModel.class);
    }
}
