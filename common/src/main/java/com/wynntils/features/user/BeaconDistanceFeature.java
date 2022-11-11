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
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
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

public class BeaconDistanceFeature extends UserFeature {

    @Config
    public CustomColor textColor = CommonColors.WHITE;

    @Config
    public int backgroundOpacity = 80;

    @Config
    public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.NONE;

    private Vector3d ndc = null;
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
        Vector3f positiveX = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f positiveY = new Vector3f(0.0f, 1.0f, 0.0f);

        // apply camera rotation
        projection.multiply(positiveX.rotationDegrees(camera.getXRot()));
        projection.multiply(positiveY.rotationDegrees(camera.getYRot() + 180.0F));

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
        if (CompassModel.getCompassLocation().isEmpty() || ndc == null || isOnScreen()) return;

        Window window = event.getWindow();

        float backgroundWidth = FontRenderer.getInstance().getFont().width(distanceText);
        float backgroundHeight = FontRenderer.getInstance().getFont().lineHeight;

        float displayPositionX = (float) ((ndc.x + 1.0f) / 2.0f) * window.getGuiScaledWidth();
        float displayPositionY = (float) ((1.0f - ndc.y) / 2.0f) * window.getGuiScaledHeight();

        RenderUtils.drawRect(
                event.getPoseStack(),
                CommonColors.BLACK.withAlpha(backgroundOpacity),
                displayPositionX - (backgroundHeight / 2) - 2,
                displayPositionY - 2,
                0,
                backgroundWidth + 3,
                backgroundHeight + 2);
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        event.getPoseStack(),
                        distanceText,
                        displayPositionX - (backgroundHeight / 2),
                        displayPositionX + backgroundWidth,
                        displayPositionY,
                        displayPositionY + backgroundHeight,
                        textColor,
                        HorizontalAlignment.Left,
                        textShadow);
    }

    // ndc range check, make sure the point is on screen
    // z check is flipped to countereact matrix transformation in onRenderGuiPost
    // possible behavior change?
    private boolean isOnScreen() {
        return !(ndc.x > 1 || ndc.x < -1 || ndc.y > 1 || ndc.y < -1 || !(ndc.z > 1));
    }

    private void worldToNdc(Vector3f delta, Matrix4f projection) {
        Vector4f clipCoords = new Vector4f(delta.x(), delta.y(), delta.z(), 1.0f);
        clipCoords.transform(projection);

        this.ndc = new Vector3d(
                clipCoords.x() / clipCoords.w(), clipCoords.y() / clipCoords.w(), clipCoords.z() / clipCoords.w());
    }
}
