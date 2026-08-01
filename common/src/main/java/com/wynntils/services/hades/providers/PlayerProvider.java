/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.hades.providers;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.features.map.MinimapFeature;
import com.wynntils.services.hades.HadesUser;
import com.wynntils.services.hades.event.HadesEvent;
import com.wynntils.services.hades.event.HadesUserEvent;
import com.wynntils.services.mapdata.attributes.impl.AbstractMapLocationAttributes;
import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.services.mapdata.attributes.type.MapLocationAttributes;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.features.type.MapLocation;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.utils.mc.SkinUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HealthTexture;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;

public class PlayerProvider extends BuiltInProvider {
    private static final List<MapFeature> PROVIDED_FEATURES = new ArrayList<>();

    @SubscribeEvent
    public void onHadesAuthenticatedEvent(HadesEvent.Authenticated event) {
        reloadHadesUsers();
    }

    @SubscribeEvent
    public void onHadesDisconnectedEvent(HadesEvent.Disconnected event) {
        reloadHadesUsers();
    }

    @SubscribeEvent
    public void onHadesUserAddedEvent(HadesUserEvent.Added event) {
        reloadHadesUsers();
    }

    @SubscribeEvent
    public void onHadesUserRemovedEvent(HadesUserEvent.Removed event) {
        reloadHadesUsers();
    }

    private void reloadHadesUsers() {
        PROVIDED_FEATURES.forEach(this::notifyCallbacks);
        PROVIDED_FEATURES.clear();
        Services.Hades.getHadesUsers().map(RemotePlayerLocation::new).forEach(PROVIDED_FEATURES::add);
    }

    @Override
    public String getProviderId() {
        return "players";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return PROVIDED_FEATURES.stream();
    }

    @Override
    public void reloadData() {
        reloadHadesUsers();
    }

    public static final class RemotePlayerLocation implements MapLocation {
        private final HadesUser hadesUser;

        public RemotePlayerLocation(HadesUser hadesUser) {
            this.hadesUser = hadesUser;
        }

        @Override
        public Location getLocation() {
            return new Location((int) hadesUser.getX(), (int) hadesUser.getY(), (int) hadesUser.getZ());
        }

        @Override
        public String getFeatureId() {
            return hadesUser.getName().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getCategoryId() {
            return "wynntils:player:" + (hadesUser.getRelation().name().toLowerCase(Locale.ROOT));
        }

        @Override
        public Optional<MapLocationAttributes> getAttributes() {
            return Optional.of(new AbstractMapLocationAttributes() {
                @Override
                public Optional<String> getLabel() {
                    return Optional.of(hadesUser.getName());
                }

                @Override
                public Optional<MapDecoration> getIconDecoration() {
                    return Optional.of(new PlayerIconDecoration(hadesUser));
                }
            });
        }

        @Override
        public List<String> getTags() {
            return List.of();
        }

        private static final class PlayerIconDecoration implements MapDecoration {
            private static final float INITIAL_PLAYER_HEAD_RENDER_SIZE = 24;

            private final HadesUser hadesUser;

            private PlayerIconDecoration(HadesUser hadesUser) {
                this.hadesUser = hadesUser;
            }

            @Override
            public boolean isVisible() {
                return true;
            }

            @Override
            public void render(GuiGraphics guiGraphics, boolean hovered, boolean fullscreenMap, float zoomLevel) {
                float playerHeadRenderSize = INITIAL_PLAYER_HEAD_RENDER_SIZE
                        * (fullscreenMap
                                ? 1
                                : Managers.Feature.getFeatureInstance(MinimapFeature.class)
                                        .minimapOverlay
                                        .remotePlayersHeadScale
                                        .get());

                guiGraphics.pose().pushMatrix();
                // center the player icon
                guiGraphics.pose().translate(-playerHeadRenderSize / 2f, -playerHeadRenderSize / 2f);

                Identifier skin = SkinUtils.getSkin(hadesUser.getUuid());

                if (!fullscreenMap) {
                    // outline
                    RenderUtils.drawRectBorders(
                            guiGraphics,
                            hadesUser.getRelationColor(),
                            0,
                            0,
                            playerHeadRenderSize,
                            playerHeadRenderSize,
                            2);
                }

                // head
                RenderUtils.drawTexturedRect(
                        guiGraphics, skin, 0, 0, playerHeadRenderSize, playerHeadRenderSize, 8, 8, 8, 8, 64, 64);

                // hat
                RenderUtils.drawTexturedRect(
                        guiGraphics, skin, 0, 0, playerHeadRenderSize, playerHeadRenderSize, 40, 8, 8, 8, 64, 64);

                // health
                if (fullscreenMap) {
                    HealthTexture healthTexture = Managers.Feature.getFeatureInstance(MainMapFeature.class)
                            .remotePlayerHealthTexture
                            .get();
                    RenderUtils.drawProgressBar(
                            guiGraphics,
                            Texture.HEALTH_BAR,
                            -10,
                            playerHeadRenderSize - INITIAL_PLAYER_HEAD_RENDER_SIZE - 7,
                            playerHeadRenderSize + 10,
                            playerHeadRenderSize - INITIAL_PLAYER_HEAD_RENDER_SIZE - 1,
                            0,
                            healthTexture.getTextureY1(),
                            81,
                            healthTexture.getTextureY2(),
                            (float) hadesUser.getHealth().getProgress());
                }

                guiGraphics.pose().popMatrix();
            }
        }
    }
}
