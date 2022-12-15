/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.core.managers.Models;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.utils.McUtils;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldWaypointDistanceFeature extends UserFeature {

    @Config
    public CustomColor textColor = CommonColors.WHITE;

    @Config
    public float backgroundOpacity = 0.2f;

    @Config
    public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.NONE;

    @Config
    public float bottomBoundingDistance = 100f;

    @Config
    public float topBoundingDistance = 40f;

    @Config
    public float horizontalBoundingDistance = 30f;

    @Config
    public int maxWaypointTextDistance = 5000;

    private double distance;

    private String distanceText;
    private Vec3 screenCoord;

    @Override
    public List<? extends Model> getModelDependencies() {
        return List.of(Models.Compass);
    }

    @SubscribeEvent
    public void onRenderLevelPost(RenderLevelEvent.Post event) {
        if (Models.Compass.getCompassLocation().isEmpty()) return;

        Location location = Models.Compass.getCompassLocation().get();
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

        if (location.y <= 0 || location.y > 255) {
            dy = 0;
        }

        double squaredDistance = dx * dx + dy * dy + dz * dz;

        distance = Math.sqrt(squaredDistance);
        int maxDistance = McUtils.mc().options.renderDistance * 16;

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
        if (Models.Compass.getCompassLocation().isEmpty()
                || screenCoord == null
                || (maxWaypointTextDistance != 0 && maxWaypointTextDistance < distance)) return;

        float backgroundWidth = FontRenderer.getInstance().getFont().width(distanceText);
        float backgroundHeight = FontRenderer.getInstance().getFont().lineHeight;

        float displayPositionX;
        float displayPositionY;

        Vec2 intersectPoint = getBoundingIntersectPoint(screenCoord, event.getWindow());
        Texture icon = Models.Compass.getTargetIcon();

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
            poseStack.mulPose(Quaternion.fromXYZDegrees(new Vector3f(0, 0, (float) angle)));
            poseStack.translate(-pointerDisplayPositionX, -pointerDisplayPositionY, 0);

            Models.Compass.getCompassWaypoint()
                    .get()
                    .getPointerPoi()
                    .renderAt(poseStack, pointerDisplayPositionX, pointerDisplayPositionY, false, 1, 1);
            poseStack.popPose();
        }
    }

    private Vec3 worldToScreen(Vector3f delta, Matrix4f projection) {
        Vector4f clipCoords = new Vector4f(delta.x(), delta.y(), delta.z(), 1.0f);
        clipCoords.transform(projection);

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
        float minX = (float) -(centerPoint.x - horizontalBoundingDistance);
        float maxX = (float) centerPoint.x - horizontalBoundingDistance;
        float minY = (float) -(centerPoint.y - topBoundingDistance);
        float maxY = (float) centerPoint.y - bottomBoundingDistance;

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

    private boolean isInBound(Vec3 position, Window window) {
        return position.x > 0
                && position.x < window.getGuiScaledWidth()
                && position.y > 0
                && position.y < window.getGuiScaledHeight()
                && position.z < 1;
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
