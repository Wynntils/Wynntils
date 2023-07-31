/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.services.map.pois.WaypointPoi;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

@ConfigCategory(Category.MAP)
public class WorldWaypointDistanceFeature extends Feature {
    @RegisterConfig
    public final Config<CustomColor> textColor = new Config<>(CommonColors.WHITE);

    @RegisterConfig
    public final Config<Float> backgroundOpacity = new Config<>(0.2f);

    @RegisterConfig
    public final Config<TextShadow> textShadow = new Config<>(TextShadow.NONE);

    @RegisterConfig
    public final Config<Float> bottomBoundingDistance = new Config<>(100f);

    @RegisterConfig
    public final Config<Float> topBoundingDistance = new Config<>(40f);

    @RegisterConfig
    public final Config<Float> horizontalBoundingDistance = new Config<>(30f);

    @RegisterConfig
    public final Config<Integer> maxWaypointTextDistance = new Config<>(5000);

    private double distance;

    private String distanceText;
    private Vec3 screenCoord;

    @SubscribeEvent
    public void onRenderLevelPost(RenderLevelEvent.Post event) {
        if (Models.Compass.getCompassLocation().isEmpty()) return;

        Location location = Models.Compass.getCompassLocation().get();
        Matrix4f projection = new Matrix4f(event.getProjectionMatrix());
        Camera camera = event.getCamera();
        Position cameraPos = camera.getPosition();

        // apply camera rotation
        Vector3f xp = new Vector3f(1, 0, 0);
        Vector3f yp = new Vector3f(0, 1, 0);
        Quaternionf xRotation = new Quaternionf().rotationAxis((float) Math.toRadians(camera.getXRot()), xp);
        Quaternionf yRotation = new Quaternionf().rotationAxis((float) Math.toRadians(camera.getYRot() + 180f), yp);
        projection.mul(new Matrix4f().rotation(xRotation));
        projection.mul(new Matrix4f().rotation(yRotation));

        // offset to put text to the center of the block
        float dx = (float) (location.x + 0.5 - cameraPos.x());
        float dy = (float) (location.y + 0.5 - cameraPos.y());
        float dz = (float) (location.z + 0.5 - cameraPos.z());

        if (location.y <= 0 || location.y > 255) {
            dy = 0;
        }

        double squaredDistance = dx * dx + dy * dy + dz * dz;

        distance = Math.sqrt(squaredDistance);
        int maxDistance = McUtils.options().renderDistance().get() * 16;

        this.distanceText = Math.round((float) distance) + "m";

        // move the position to avoid ndc z leak past 1
        if (distance > maxDistance) {
            double posScale = maxDistance / distance;
            dx *= posScale;
            dz *= posScale;
        }
        this.screenCoord = worldToScreen(new Vector3f(dx, dy, dz), projection);
    }

    @SubscribeEvent
    public void onRenderGuiPost(RenderEvent.Post event) {
        Optional<Location> compassLocationOpt = Models.Compass.getCompassLocation();
        Optional<WaypointPoi> compassWaypointOpt = Models.Compass.getCompassWaypoint();
        if (compassLocationOpt.isEmpty()
                || compassWaypointOpt.isEmpty()
                || screenCoord == null
                || (maxWaypointTextDistance.get() != 0 && maxWaypointTextDistance.get() < distance)) return;

        WaypointPoi waypointPoi = compassWaypointOpt.get();

        float backgroundWidth = FontRenderer.getInstance().getFont().width(distanceText);
        float backgroundHeight = FontRenderer.getInstance().getFont().lineHeight;

        float displayPositionX;
        float displayPositionY;

        Vec2 intersectPoint = getBoundingIntersectPoint(screenCoord, event.getWindow());
        Texture icon = Models.Compass.getTargetIcon();
        float[] color = Models.Compass.getTargetColor().asFloatArray();
        RenderSystem.setShaderColor(color[0], color[1], color[2], 1f);

        // The set waypoint is visible on the screen, so we render the icon + distance
        if (intersectPoint == null) {
            displayPositionX = (float) screenCoord.x;
            displayPositionY = (float) screenCoord.y;

            RenderUtils.drawScalingTexturedRect(
                    event.getPoseStack(),
                    icon.resource(),
                    displayPositionX - icon.width() / 2,
                    displayPositionY - icon.height() - backgroundHeight / 2 - 3f,
                    0,
                    icon.width(),
                    icon.height(),
                    icon.width(),
                    icon.height());
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderUtils.drawRect(
                    event.getPoseStack(),
                    CommonColors.BLACK.withAlpha(backgroundOpacity.get()),
                    displayPositionX - (backgroundWidth / 2) - 2,
                    displayPositionY - (backgroundHeight / 2) - 2,
                    0,
                    backgroundWidth + 3,
                    backgroundHeight + 2);
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            event.getPoseStack(),
                            StyledText.fromString(distanceText),
                            displayPositionX - backgroundWidth,
                            displayPositionX + backgroundWidth,
                            displayPositionY - backgroundHeight,
                            displayPositionY + backgroundHeight,
                            0,
                            textColor.get(),
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            textShadow.get());
        } else {
            displayPositionX = intersectPoint.x;
            displayPositionY = intersectPoint.y;

            RenderUtils.drawScalingTexturedRect(
                    event.getPoseStack(),
                    icon.resource(),
                    displayPositionX - icon.width() / 2,
                    displayPositionY - icon.height() / 2,
                    0,
                    icon.width(),
                    icon.height(),
                    icon.width(),
                    icon.height());
            RenderSystem.setShaderColor(1, 1, 1, 1);

            // pointer position is determined by finding the point on circle centered around displayPosition
            double angle = Math.toDegrees(StrictMath.atan2(
                            displayPositionY - event.getWindow().getGuiScaledHeight() / 2,
                            displayPositionX - event.getWindow().getGuiScaledWidth() / 2))
                    + 90f;
            float radius = icon.width() / 2 + 8f;
            float pointerDisplayPositionX =
                    (float) (displayPositionX + radius * StrictMath.cos((angle - 90) * 3 / 180));
            float pointerDisplayPositionY =
                    (float) (displayPositionY + radius * StrictMath.sin((angle - 90) * 3 / 180));

            // apply rotation
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate(pointerDisplayPositionX, pointerDisplayPositionY, 0);
            poseStack.mulPose(new Quaternionf().rotationXYZ(0, 0, (float) Math.toRadians(angle)));
            poseStack.translate(-pointerDisplayPositionX, -pointerDisplayPositionY, 0);

            MultiBufferSource.BufferSource bufferSource =
                    MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            waypointPoi
                    .getPointerPoi()
                    .renderAt(poseStack, bufferSource, pointerDisplayPositionX, pointerDisplayPositionY, false, 1, 1);
            bufferSource.endBatch();
            poseStack.popPose();
        }
    }

    private Vec3 worldToScreen(Vector3f delta, Matrix4f projection) {
        Vector4f clipCoords = new Vector4f(delta.x(), delta.y(), delta.z(), 1.0f);
        projection.transform(clipCoords);

        // stands for Normalized Device Coordinates
        Vector3d ndc = new Vector3d(
                clipCoords.x() / clipCoords.w(), clipCoords.y() / clipCoords.w(), clipCoords.z() / clipCoords.w());

        Window window = McUtils.window();

        return new Vec3(
                (float) ((ndc.x + 1.0f) / 2.0f) * window.getGuiScaledWidth(),
                (float) ((1.0f - ndc.y) / 2.0f) * window.getGuiScaledHeight(),
                (float) ndc.z);
    }

    // draw a line from screen center to the target's screenspace coordinate
    // and find the intersect point on one of the screen's bounding
    private Vec2 getBoundingIntersectPoint(Vec3 position, Window window) {
        if (isInBound(position, window)) return null;
        Vec3 centerPoint = new Vec3(window.getGuiScaledWidth() / 2, window.getGuiScaledHeight() / 2, 0);

        // minecraft's origin point is top left corner
        // so positive Y is at the screen bottom
        float minX = (float) -(centerPoint.x - horizontalBoundingDistance.get());
        float maxX = (float) centerPoint.x - horizontalBoundingDistance.get();
        float minY = (float) -(centerPoint.y - topBoundingDistance.get());
        float maxY = (float) centerPoint.y - bottomBoundingDistance.get();

        // drag the origin point to center since indicator's screenspace position / rotation is in relation to it
        Vec3 centerRelativePosition = screenCoord.subtract(centerPoint);

        // invert xy axis if target is behind camera
        if (centerRelativePosition.z > 1) {
            centerRelativePosition = centerRelativePosition.multiply(-1, -1, 1);
        }

        // since center point is now the origin point, atan2 is used to get the angle, and angle is used to get the
        // line's slope
        double angle = StrictMath.atan2(centerRelativePosition.y, centerRelativePosition.x);
        double m = StrictMath.tan(angle);

        // trying to solve (y2 - y1) = m (x2 - x1) + c here
        // starting from origin point/screen center (x1, y1), end at one of the screen bounding
        // (y2 - y1) is the equivalent of the y position
        // (x2 - x1) is the equivalent of the x position
        // c is the line's y-intercept, but line pass through origin, so this will be 0
        // finalize to y = mx, or x = y/m

        if (centerRelativePosition.x > 0) {
            centerRelativePosition = new Vec3(maxX, maxX * m, 0);
        } else {
            centerRelativePosition = new Vec3(minX, minX * m, 0);
        }

        if (centerRelativePosition.y > maxY) {
            centerRelativePosition = new Vec3(maxY / m, maxY, 0);
        } else if (centerRelativePosition.y < minY) {
            centerRelativePosition = new Vec3(minY / m, minY, 0);
        }

        // bring the position back to normal screen space (top left origin point)
        return new Vec2(
                (float) (centerRelativePosition.x + centerPoint.x), (float) (centerRelativePosition.y + centerPoint.y));
    }

    private boolean isInBound(Position position, Window window) {
        return position.x() > 0
                && position.x() < window.getGuiScaledWidth()
                && position.y() > 0
                && position.y() < window.getGuiScaledHeight()
                && position.z() < 1;
    }

    // limit the bounding distance to prevent divided by zero in getBoundingIntersectPoint
    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        Window window = McUtils.window();

        switch (configHolder.getFieldName()) {
            case "topBoundingDistance", "bottomBoundingDistance" -> {
                if ((float) configHolder.getValue() > window.getGuiScaledHeight() * 0.4f) {
                    configHolder.setValue(window.getGuiScaledHeight() * 0.4f);
                }
            }
            case "horizontalBoundingDistance" -> {
                if ((float) configHolder.getValue() > window.getGuiScaledWidth() * 0.4f) {
                    configHolder.setValue(window.getGuiScaledWidth() * 0.4f);
                }
            }
        }
    }
}
