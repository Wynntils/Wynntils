/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.players;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.features.tooltips.ItemStatInfoFeature;
import com.wynntils.mc.event.EntityNameTagRenderEvent;
import com.wynntils.mc.event.GetCameraEntityEvent;
import com.wynntils.mc.event.PlayerNametagRenderEvent;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.mc.extension.EntityRenderStateExtension;
import com.wynntils.models.inventory.type.InventoryAccessory;
import com.wynntils.models.inventory.type.InventoryArmor;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.players.WynntilsUser;
import com.wynntils.models.players.type.AccountType;
import com.wynntils.screens.playerviewer.PlayerViewerScreen;
import com.wynntils.services.hades.HadesUser;
import com.wynntils.services.leaderboard.type.LeaderboardBadge;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.wynn.ColorScaleUtils;
import com.wynntils.utils.wynn.RaycastUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.PLAYERS)
public class CustomNametagRendererFeature extends Feature {
    // how much larger account tags should be relative to gear lines
    private static final float ACCOUNT_TYPE_MULTIPLIER = 1.5f;
    private static final float NAMETAG_HEIGHT = 0.25875f;
    private static final float BADGE_MARGIN = 2;
    private static final int BADGE_SCROLL_SPEED = 40;
    private static final ResourceLocation WYNNTILS_NAMETAG_LOGO_FONT =
            ResourceLocation.fromNamespaceAndPath("wynntils", "nametag");
    private static final String WYNNTILS_NAMETAG_LOGO = "\uE100";

    @Persisted
    private final Config<Boolean> hideAllNametags = new Config<>(false);

    @Persisted
    private final Config<Boolean> showOwnNametag = new Config<>(false);

    @Persisted
    private final Config<Boolean> hidePlayerNametags = new Config<>(false);

    @Persisted
    private final Config<Boolean> hideNametagBackground = new Config<>(false);

    @Persisted
    private final Config<Boolean> showLeaderboardBadges = new Config<>(true);

    @Persisted
    private final Config<Integer> badgeCount = new Config<>(7);

    @Persisted
    private final Config<Boolean> showGearOnHover = new Config<>(true);

    @Persisted
    private final Config<Boolean> showGearPercentage = new Config<>(true);

    @Persisted
    private final Config<Boolean> showWynntilsMarker = new Config<>(true);

    @Persisted
    private final Config<Float> customNametagScale = new Config<>(0.5f);

    private Player hitPlayerCache = null;

    public CustomNametagRendererFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.NEW_PLAYER, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onPlayerNameTagRender(PlayerNametagRenderEvent event) {
        Entity entity = ((EntityRenderStateExtension) event.getEntityRenderState()).getEntity();
        if (!(entity instanceof AbstractClientPlayer player)) return;
        if (Models.Player.isNpc(player)) return;

        if (hidePlayerNametags.get()) {
            event.setCanceled(true);
            return;
        }

        // If we are viewing this player's gears, do not show plus info
        if (McUtils.screen() instanceof PlayerViewerScreen playerViewerScreen
                && playerViewerScreen.getPlayer() == player) {
            return;
        }

        List<CustomNametag> nametags = new ArrayList<>();

        if (showGearOnHover.get()) {
            addGearNametags(event, nametags);
        }

        addAccountTypeNametag(event, nametags);

        // need to handle the rendering ourselves
        if (!nametags.isEmpty()) {
            event.setCanceled(true);
            drawNametags(event, nametags);
        } else {
            drawBadges(event, 0);
        }
    }

    @SubscribeEvent
    public void onEntityNameTagRender(EntityNameTagRenderEvent event) {
        if (hideAllNametags.get()) {
            event.setCanceled(true);
            return;
        }

        if (hideNametagBackground.get()) {
            event.setBackgroundOpacity(0f);
        }
    }

    @SubscribeEvent
    public void onCameraCheck(GetCameraEntityEvent e) {
        if (!showOwnNametag.get()) return;
        // Only render when a screen is not open or in the chat screen
        if (McUtils.screen() != null && !(McUtils.screen() instanceof ChatScreen)) return;

        // We don't need to check if the entity is the local player as that is already done in
        // LivingEntityRenderer.shouldShowName
        e.setEntity(null);
    }

    @SubscribeEvent
    public void onRenderLevel(RenderLevelEvent.Pre event) {
        Optional<Player> hitPlayer = RaycastUtils.getHoveredPlayer();
        hitPlayerCache = hitPlayer.orElse(null);
    }

    private void addGearNametags(PlayerNametagRenderEvent event, List<CustomNametag> nametags) {
        Entity entity = ((EntityRenderStateExtension) event.getEntityRenderState()).getEntity();
        if (!(entity instanceof AbstractClientPlayer player)) return;
        LocalPlayer localPlayer = McUtils.player();

        if (hitPlayerCache != player) return;
        if (!Models.Player.isLocalPlayer(localPlayer)) return;

        Optional<HadesUser> hadesUserOpt = Services.Hades.getHadesUser(hitPlayerCache.getUUID());
        if (hadesUserOpt.isEmpty()) return;

        MutableComponent handComp = getItemComponent(hadesUserOpt.get().getHeldItem(), showGearPercentage.get());
        if (handComp != null) {
            nametags.add(new CustomNametag(handComp, customNametagScale.get()));
        }

        for (InventoryAccessory accessory : hadesUserOpt.get().getAccessories().descendingKeySet()) {
            MutableComponent accessoryComp =
                    getItemComponent(hadesUserOpt.get().getAccessories().get(accessory), showGearPercentage.get());
            if (accessoryComp != null) {
                nametags.add(new CustomNametag(accessoryComp, customNametagScale.get()));
            }
        }

        for (InventoryArmor armor : hadesUserOpt.get().getArmor().descendingKeySet()) {
            MutableComponent armorComp =
                    getItemComponent(hadesUserOpt.get().getArmor().get(armor), showGearPercentage.get());
            if (armorComp != null) {
                nametags.add(new CustomNametag(armorComp, customNametagScale.get()));
            }
        }
    }

    private static MutableComponent getItemComponent(WynnItem wynnItem, boolean showGearPercentage) {
        if (wynnItem == null) return null;

        if (wynnItem instanceof GearItem gearItem) {
            String itemName = gearItem.getItemInfo().name();
            MutableComponent gearComponent = Component.literal(itemName)
                    .withStyle(gearItem.getItemInfo().tier().getChatFormatting());

            if (gearItem.getShinyStat().isPresent()) {
                gearComponent = Component.literal("⬡ ")
                        .append(Component.literal("Shiny ")
                                .withStyle(gearItem.getItemInfo().tier().getChatFormatting()))
                        .append(gearComponent);
            }

            if (showGearPercentage && gearItem.hasOverallValue()) {
                ItemStatInfoFeature isif = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);
                gearComponent.append(ColorScaleUtils.getPercentageTextComponent(
                        isif.getColorMap(),
                        gearItem.getOverallPercentage(),
                        isif.colorLerp.get(),
                        isif.decimalPlaces.get()));
            }

            return gearComponent;
        } else if (wynnItem instanceof CraftedGearItem craftedGearItem) {
            return Component.literal(craftedGearItem.getName())
                    .withStyle(craftedGearItem.getGearTier().getChatFormatting());
        }

        return null;
    }

    private void addAccountTypeNametag(PlayerNametagRenderEvent event, List<CustomNametag> nametags) {
        Entity entity = ((EntityRenderStateExtension) event.getEntityRenderState()).getEntity();
        if (!(entity instanceof AbstractClientPlayer player)) return;

        WynntilsUser user = Models.Player.getWynntilsUser(player);
        if (user == null) {
            if (!nametags.isEmpty()) {
                // We will cancel vanilla rendering, so we must add back the normal vanilla base nametag
                Component realName = event.getDisplayName();
                nametags.add(new CustomNametag(realName, 1f));
            }
            return;
        }

        AccountType accountType = user.accountType();
        if (accountType != null && accountType.getComponent() != null) {
            nametags.add(
                    new CustomNametag(accountType.getComponent(), customNametagScale.get() * ACCOUNT_TYPE_MULTIPLIER));
        }

        if (!showWynntilsMarker.get()) {
            // We will cancel vanilla rendering, so we must add back the normal vanilla base nametag
            Component realName = event.getDisplayName();
            nametags.add(new CustomNametag(realName, 1f));
            return;
        }

        // Add an appropriate Wynntils marker
        ChatFormatting logoColor;
        if (Models.Player.isLocalPlayer(player)) {
            logoColor = ChatFormatting.WHITE;
        } else {
            logoColor = ChatFormatting.GRAY;
        }
        Component prefixedName = Component.empty()
                .append(Component.literal(WYNNTILS_NAMETAG_LOGO)
                        .withStyle(
                                Style.EMPTY.withFont(WYNNTILS_NAMETAG_LOGO_FONT).withColor(logoColor)))
                .append(" ")
                .append(event.getDisplayName());
        nametags.add(new CustomNametag(prefixedName, 1f));
    }

    private void drawNametags(PlayerNametagRenderEvent event, List<CustomNametag> nametags) {
        Entity entity = ((EntityRenderStateExtension) event.getEntityRenderState()).getEntity();
        if (!(entity instanceof AbstractClientPlayer player)) return;

        // calculate color of nametag box
        int backgroundColor =
                hideNametagBackground.get() ? 0 : ((int) (McUtils.options().getBackgroundOpacity(0.25F) * 255f) << 24);

        float yOffset = 0f;
        for (CustomNametag nametag : nametags) {
            // move rendering up to fit the next line, plus a small gap
            yOffset += nametag.nametagScale() * NAMETAG_HEIGHT;

            RenderUtils.renderCustomNametag(
                    event.getPoseStack(),
                    event.getBuffer(),
                    event.getPackedLight(),
                    backgroundColor,
                    event.getEntityRenderDispatcher(),
                    player,
                    nametag.nametagComponent(),
                    event.getFont(),
                    nametag.nametagScale(),
                    yOffset);
        }

        drawBadges(event, yOffset);
    }

    private void drawBadges(PlayerNametagRenderEvent event, float height) {
        if (!showLeaderboardBadges.get()) return;
        if (badgeCount.get() <= 0) return;
        Entity entity = ((EntityRenderStateExtension) event.getEntityRenderState()).getEntity();
        if (!(entity instanceof AbstractClientPlayer player)) return;

        List<LeaderboardBadge> allBadges = Services.Leaderboard.getPlayerBadges(Models.Player.getUserUUID(player));

        if (allBadges.isEmpty()) return;

        List<LeaderboardBadge> badgesToRender = new ArrayList<>();

        if (badgeCount.get() >= allBadges.size()) {
            badgesToRender = allBadges;
        } else {
            int offset = (McUtils.player().tickCount / BADGE_SCROLL_SPEED) % allBadges.size();
            for (int i = 0; i < badgeCount.get(); i++) {
                badgesToRender.add(allBadges.get((i + offset) % allBadges.size()));
            }
        }

        float totalWidth = LeaderboardBadge.WIDTH * badgesToRender.size() + BADGE_MARGIN * (badgesToRender.size() - 1);
        float xOffset = -(totalWidth / 2) + LeaderboardBadge.WIDTH / 2F;
        float yOffset = 15F;
        if (height == 0) {
            yOffset += 10F;
        }

        for (LeaderboardBadge badge : badgesToRender) {
            RenderUtils.renderProfessionBadge(
                    event.getPoseStack(),
                    event.getEntityRenderDispatcher(),
                    player,
                    Texture.LEADERBOARD_BADGES.resource(),
                    LeaderboardBadge.WIDTH,
                    LeaderboardBadge.HEIGHT,
                    badge.uOffset(),
                    badge.vOffset(),
                    LeaderboardBadge.WIDTH,
                    LeaderboardBadge.HEIGHT,
                    Texture.LEADERBOARD_BADGES.width(),
                    Texture.LEADERBOARD_BADGES.height(),
                    height,
                    xOffset,
                    yOffset);

            xOffset += LeaderboardBadge.WIDTH + BADGE_MARGIN;
        }
    }

    private record CustomNametag(Component nametagComponent, float nametagScale) {}
}
