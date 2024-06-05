/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.providers.builtin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.features.map.MinimapFeature;
import com.wynntils.models.character.event.CharacterMovedEvent;
import com.wynntils.services.hades.HadesUser;
import com.wynntils.services.hades.event.HadesEvent;
import com.wynntils.services.hades.event.HadesUserEvent;
import com.wynntils.services.mapdata.attributes.AbstractMapAttributes;
import com.wynntils.services.mapdata.attributes.FixedMapVisibility;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapDecoration;
import com.wynntils.services.mapdata.attributes.type.MapIcon;
import com.wynntils.services.mapdata.attributes.type.MapVisibility;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapFeature;
import com.wynntils.services.mapdata.type.MapLocation;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.SkinUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HealthTexture;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CharacterProvider extends BuiltInProvider {
    private static final CharacterLocation CHARACTER_LOCATION = new CharacterLocation();
    private static final List<HadesPlayerLocation> PLAYERS = new ArrayList<>();

    // Provided features and categories
    private static final List<MapCategory> PROVIDED_CATEGORIES = List.of(new PlayersCategory());

    @SubscribeEvent
    public void onCharacterMove(CharacterMovedEvent e) {
        notifyCallbacks(CHARACTER_LOCATION);
    }

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
        PLAYERS.forEach(this::notifyCallbacks);
        PLAYERS.clear();
        Services.Hades.getHadesUsers().map(HadesPlayerLocation::new).forEach(PLAYERS::add);
    }

    @Override
    public String getProviderId() {
        return "characters";
    }

    @Override
    public Stream<MapFeature> getFeatures() {
        return Stream.concat(Stream.of(CHARACTER_LOCATION), PLAYERS.stream());
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return PROVIDED_CATEGORIES.stream();
    }

    private static final class PlayersCategory implements MapCategory {
        @Override
        public String getCategoryId() {
            return "wynntils:players";
        }

        @Override
        public Optional<String> getName() {
            return Optional.of("Player positions");
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<Integer> getPriority() {
                    return Optional.of(900);
                }

                @Override
                public Optional<MapVisibility> getIconVisibility() {
                    return Optional.of(FixedMapVisibility.ICON_ALWAYS);
                }
            });
        }
    }

    public static final class HadesPlayerLocation implements MapLocation {
        private final HadesUser hadesUser;

        public HadesPlayerLocation(HadesUser hadesUser) {
            this.hadesUser = hadesUser;
        }

        @Override
        public Location getLocation() {
            return new Location((int) hadesUser.getX(), (int) hadesUser.getY(), (int) hadesUser.getZ());
        }

        @Override
        public String getFeatureId() {
            return "player-" + hadesUser.getName().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getCategoryId() {
            return "wynntils:players:hades" + (hadesUser.getRelation().isEmpty() ? "" : ":" + hadesUser.getRelation());
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<String> getLabel() {
                    return Optional.of(hadesUser.getName());
                }

                @Override
                public Optional<CustomColor> getLabelColor() {
                    return Optional.of(hadesUser.getRelationColor());
                }

                @Override
                public Optional<String> getIconId() {
                    return Optional.of(MapIcon.NO_ICON_ID);
                }

                @Override
                public Optional<MapDecoration> getIconDecoration() {
                    return Optional.of(new MainMapIconDecoration());
                }
            });
        }

        @Override
        public List<String> getTags() {
            return List.of();
        }

        private final class MainMapIconDecoration implements MapDecoration {
            private static final float INITIAL_PLAYER_HEAD_RENDER_SIZE = 20;

            @Override
            public boolean isVisible() {
                return true;
            }

            @Override
            public void render(
                    PoseStack poseStack,
                    MultiBufferSource bufferSource,
                    boolean hovered,
                    boolean fullscreenMap,
                    float zoomLevel) {
                float playerHeadRenderSize = INITIAL_PLAYER_HEAD_RENDER_SIZE
                        * (fullscreenMap
                                ? 1
                                : Managers.Feature.getFeatureInstance(MinimapFeature.class)
                                        .minimapOverlay
                                        .remotePlayersHeadScale
                                        .get());

                poseStack.pushPose();
                // center the player icon
                poseStack.translate(-playerHeadRenderSize / 2f, -playerHeadRenderSize / 2f, 0);

                final float renderX = 0;
                final float renderY = fullscreenMap ? -25 : 0;

                HadesUser user = HadesPlayerLocation.this.hadesUser;
                ResourceLocation skin = SkinUtils.getSkin(user.getUuid());

                if (!fullscreenMap) {
                    // outline
                    BufferedRenderUtils.drawRectBorders(
                            poseStack,
                            bufferSource,
                            user.getRelationColor(),
                            renderX,
                            renderY,
                            renderX + playerHeadRenderSize,
                            renderY + playerHeadRenderSize,
                            0,
                            2);
                }

                // head
                BufferedRenderUtils.drawTexturedRect(
                        poseStack,
                        bufferSource,
                        skin,
                        renderX,
                        renderY,
                        0,
                        playerHeadRenderSize,
                        playerHeadRenderSize,
                        8,
                        8,
                        8,
                        8,
                        64,
                        64);

                // hat
                BufferedRenderUtils.drawTexturedRect(
                        poseStack,
                        bufferSource,
                        skin,
                        renderX,
                        renderY,
                        1,
                        playerHeadRenderSize,
                        playerHeadRenderSize,
                        40,
                        8,
                        8,
                        8,
                        64,
                        64);

                // health
                if (fullscreenMap) {
                    HealthTexture healthTexture = Managers.Feature.getFeatureInstance(MainMapFeature.class)
                            .remotePlayerHealthTexture
                            .get();
                    BufferedRenderUtils.drawProgressBar(
                            poseStack,
                            bufferSource,
                            Texture.HEALTH_BAR,
                            renderX - 10,
                            renderY + playerHeadRenderSize + 1,
                            renderX + playerHeadRenderSize + 10,
                            renderY + playerHeadRenderSize + 7,
                            0,
                            healthTexture.getTextureY1(),
                            81,
                            healthTexture.getTextureY2(),
                            (float) user.getHealth().getProgress());
                }

                poseStack.popPose();
            }
        }
    }

    private static final class CharacterLocation implements MapLocation {
        @Override
        public String getFeatureId() {
            return "player-you";
        }

        @Override
        public String getCategoryId() {
            return "wynntils:players:you";
        }

        @Override
        public Optional<MapAttributes> getAttributes() {
            return Optional.of(new AbstractMapAttributes() {
                @Override
                public Optional<MapVisibility> getIconVisibility() {
                    return Optional.of(FixedMapVisibility.ICON_NEVER);
                }
            });
        }

        @Override
        public List<String> getTags() {
            return List.of();
        }

        @Override
        public Location getLocation() {
            return new Location(McUtils.player().blockPosition());
        }
    }
}
