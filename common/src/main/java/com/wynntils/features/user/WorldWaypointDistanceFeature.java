/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.platform.Window;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.model.CompassModel;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldWaypointDistanceFeature extends UserFeature {

    @Config
    public CustomColor textColor = CommonColors.WHITE;

    @Config
    public float backgroundOpacity = 0.2f;

    @Config
    public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.NONE;

    private Vector3d normalizedDeviceCoordinates = null;
    private String distanceText;

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(CompassModel.class);
    }

    @SubscribeEvent
    public void onRenderLevelPost(RenderLevelEvent.Post event) {
        if (CompassModel.getCompassLocation().isEmpty()) return;

        Location location = CompassModel.getCompassLocation().get();
        Matrix4f projection = event.getProjectionMatrix().copy();
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        // apply camera rotation
        projection.multiply(Vector3f.XP.rotationDegrees(camera.getXRot()));
        projection.multiply(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));

        // offset to put text to the center of the block
        float dx = (float) (location.x + 0.5 - cameraPos.x);
        float dy = (float) (location.y + 0.5 - cameraPos.y);
        float dz = (float) (location.z + 0.5 - cameraPos.z);

        double squaredDistance = dx * dx + dy * dy + dz * dz;

        double distance = Math.sqrt(squaredDistance);
        int maxDistance = McUtils.mc().options.renderDistance * 16;

        this.distanceText = Math.round((float) distance) + "m";

        // move the position to avoid ndc z leak past 1
        if (distance > maxDistance) {
            double posScale = maxDistance / distance;
            dx *= posScale;
            dz *= posScale;
        }

        worldToNdc(new Vector3f(dx, dy, dz), projection);
    }

    @SubscribeEvent
    public void onRenderGuiPost(RenderEvent.Post event) {
        if (CompassModel.getCompassLocation().isEmpty() || normalizedDeviceCoordinates == null || isOnScreen()) return;

        Window window = event.getWindow();

        float backgroundWidth = FontRenderer.getInstance().getFont().width(distanceText);
        float backgroundHeight = FontRenderer.getInstance().getFont().lineHeight;
        WynntilsMod.info(String.valueOf(backgroundWidth));

        float displayPositionX = (float) ((normalizedDeviceCoordinates.x + 1.0f) / 2.0f) * window.getGuiScaledWidth();
        float displayPositionY = (float) ((1.0f - normalizedDeviceCoordinates.y) / 2.0f) * window.getGuiScaledHeight();

        RenderUtils.drawRect(
                event.getPoseStack(),
                CommonColors.BLACK.withAlpha(backgroundOpacity),
                displayPositionX - (backgroundWidth / 2) - 2,
                displayPositionY - (backgroundHeight / 2) - 2,
                0,
                backgroundWidth + 3,
                backgroundHeight + 2);
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        event.getPoseStack(),
                        distanceText,
                        displayPositionX - backgroundWidth,
                        displayPositionX + backgroundWidth,
                        displayPositionY - backgroundHeight,
                        displayPositionY + backgroundHeight,
                        0,
                        textColor,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Middle,
                        textShadow);
    }

    private boolean isOnScreen() {
        return normalizedDeviceCoordinates.x <= 1
                && normalizedDeviceCoordinates.x >= -1
                && normalizedDeviceCoordinates.y <= 1
                && normalizedDeviceCoordinates.y >= -1
                && normalizedDeviceCoordinates.z > 1;
    }

    private void worldToNdc(Vector3f delta, Matrix4f projection) {
        Vector4f clipCoords = new Vector4f(delta.x(), delta.y(), delta.z(), 1.0f);
        clipCoords.transform(projection);

        this.normalizedDeviceCoordinates = new Vector3d(
                clipCoords.x() / clipCoords.w(), clipCoords.y() / clipCoords.w(), clipCoords.z() / clipCoords.w());
    }
}
