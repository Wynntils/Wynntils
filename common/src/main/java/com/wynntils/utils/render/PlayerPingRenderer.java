/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.hades.protocol.enums.PlayerPingType;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.pipelines.CustomRenderTypes;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class PlayerPingRenderer {
    private static final float ORB_RADIUS = 0.22f;
    private static final float MARKER_HEIGHT = 0.85f;
    private static final float LABEL_DISTANCE = 1.05f;
    private static final float ORB_ROTATION_PERIOD_TICKS = 40f;
    private static final float ORB_BOB_PERIOD_TICKS = 30f;
    private static final float ORB_BOB_AMPLITUDE = 0.08f;
    private static final float ORB_SPARK_RADIUS = 0.045f;
    private static final float ORB_SPARK_ORBIT_RADIUS = 0.31f;

    private static final int ICON_CIRCLE_SEGMENTS = 24;
    private static final float ICON_OUTLINE_SCALE = 1.08f;
    private static final int ICON_OUTLINE_COLOR = 0xFF000000;

    private static final long ANIMATION_PERIOD_TICKS = 120L;

    private static final int PULSE_COUNT = 2;
    private static final float PULSE_CYCLE_TICKS = 24f;
    private static final float PULSE_THICKNESS = 0.06f;
    private static final float PULSE_MIN_RADIUS = 0.46f;
    private static final float PULSE_MAX_RADIUS = 1.2f;
    private static final float PULSE_HEIGHT = 0.02f;

    private static final int TARGET_TEXT_RENDER_ORDER = 1;

    private static final float[] EYE_OUTER_X = {-.46f, -.20f, 0f, .20f, .46f, .20f, 0f, -.20f};
    private static final float[] EYE_OUTER_Y = {0f, .17f, .22f, .17f, 0f, -.17f, -.22f, -.17f};
    private static final float[] EYE_INNER_X = {-.34f, -.14f, 0f, .14f, .34f, .14f, 0f, -.14f};
    private static final float[] EYE_INNER_Y = {0f, .08f, .11f, .08f, 0f, -.08f, -.11f, -.08f};

    public static void submitMarker(
            SubmitNodeCollector submitNodeCollector,
            Vec3 cameraPosition,
            PoseStack poseStack,
            Vec3 worldPosition,
            Direction direction,
            float markerScale,
            String targetName,
            PlayerPingType pingType,
            long gameTime,
            int color) {
        float animationTime = (gameTime % ANIMATION_PERIOD_TICKS)
                + McUtils.mc().getDeltaTracker().getGameTimeDeltaPartialTick(false);

        Vec3 faceCenter = getFaceCenter(worldPosition, direction);

        poseStack.pushPose();
        poseStack.translate(
                faceCenter.x - cameraPosition.x, faceCenter.y - cameraPosition.y, faceCenter.z - cameraPosition.z);

        poseStack.pushPose();
        poseStack.mulPose(direction.getRotation());
        poseStack.scale(markerScale, markerScale, markerScale);

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                CustomRenderTypes.PLAYER_PING_QUAD,
                (pose, consumer) -> drawMarker(pose, consumer, pingType, color, animationTime));
        poseStack.popPose();

        if (targetName != null && !targetName.isBlank()) {
            // Render the label after the marker so its see-through glyphs cannot be overwritten by the marker.
            submitTargetLabel(
                    submitNodeCollector.order(TARGET_TEXT_RENDER_ORDER),
                    poseStack,
                    direction,
                    markerScale,
                    targetName,
                    color);
        }

        poseStack.popPose();
    }

    public static void submitTargetText(
            SubmitNodeCollector submitNodeCollector,
            Vec3 cameraPosition,
            PoseStack poseStack,
            Vec3 worldPosition,
            Direction direction,
            float markerScale,
            String targetName,
            int color) {
        if (targetName == null || targetName.isBlank()) return;

        Vec3 faceCenter = getFaceCenter(worldPosition, direction);

        poseStack.pushPose();
        poseStack.translate(
                faceCenter.x - cameraPosition.x, faceCenter.y - cameraPosition.y, faceCenter.z - cameraPosition.z);

        submitTargetLabel(
                submitNodeCollector.order(TARGET_TEXT_RENDER_ORDER),
                poseStack,
                direction,
                markerScale,
                targetName,
                color);

        poseStack.popPose();
    }

    private static Vec3 getFaceCenter(Vec3 worldPosition, Direction direction) {
        return worldPosition
                .add(0.5d, 0.5d, 0.5d)
                .subtract(Vec3.atLowerCornerOf(direction.getUnitVec3i()).scale(0.5d));
    }

    private static void drawMarker(
            PoseStack.Pose pose, VertexConsumer consumer, PlayerPingType pingType, int color, float animationTime) {
        Matrix4f matrix = pose.pose();
        CustomColor markerColor = CustomColor.fromARGBInt(color);

        float bobOffset = (float)
                (Math.sin(cycleProgress(animationTime, ORB_BOB_PERIOD_TICKS) * 2 * Math.PI) * ORB_BOB_AMPLITUDE);
        double rotationRadians = cycleProgress(animationTime, ORB_ROTATION_PERIOD_TICKS) * 2 * Math.PI;
        float markerY = MARKER_HEIGHT + bobOffset;

        drawPingIcon(matrix, consumer, pingType, markerColor, markerY, rotationRadians);
        drawPulseRings(matrix, consumer, markerColor, animationTime);
    }

    private static float cycleProgress(float time, float period) {
        return (time % period) / period;
    }

    private static void drawPingIcon(
            Matrix4f matrix,
            VertexConsumer consumer,
            PlayerPingType type,
            CustomColor color,
            float centerY,
            double rotationRadians) {
        Matrix4f outlineMatrix = new Matrix4f(matrix)
                .translate(0f, centerY, 0f)
                .scale(ICON_OUTLINE_SCALE)
                .translate(0f, -centerY, 0f);

        drawPingIconShape(outlineMatrix, consumer, type, centerY, rotationRadians, ICON_OUTLINE_COLOR);

        drawPingIconShape(
                matrix,
                consumer,
                type,
                centerY,
                rotationRadians,
                color.withAlpha(255).asInt());

        drawIconInteriorOutlines(matrix, consumer, type, centerY, rotationRadians);
    }

    private static void drawIconInteriorOutlines(
            Matrix4f matrix, VertexConsumer consumer, PlayerPingType type, float centerY, double rotationRadians) {
        switch (type) {
            case ATTACK_HERE ->
                drawUprightRing(
                        matrix, consumer, centerY, .282f, .258f, .081f, rotationRadians / 2d, ICON_OUTLINE_COLOR);
            case LOOK_HERE -> drawEyeInteriorOutlines(matrix, consumer, centerY, rotationRadians);
            default -> {}
        }
    }

    private static void drawEyeInteriorOutlines(
            Matrix4f matrix, VertexConsumer consumer, float centerY, double rotationRadians) {
        drawScaledContour(matrix, consumer, centerY, rotationRadians, .071f, EYE_INNER_X, EYE_INNER_Y, .97f, 1.03f);

        drawUprightRing(matrix, consumer, centerY, .083f, .067f, .091f, rotationRadians, ICON_OUTLINE_COLOR);
    }

    private static void drawScaledContour(
            Matrix4f matrix,
            VertexConsumer consumer,
            float centerY,
            double rotationRadians,
            float depth,
            float[] x,
            float[] y,
            float innerScale,
            float outerScale) {
        float[] outerX = new float[x.length];
        float[] outerY = new float[y.length];
        float[] innerX = new float[x.length];
        float[] innerY = new float[y.length];

        for (int i = 0; i < x.length; i++) {
            outerX[i] = x[i] * outerScale;
            outerY[i] = y[i] * outerScale;
            innerX[i] = x[i] * innerScale;
            innerY[i] = y[i] * innerScale;
        }

        drawExtrudedContour(
                matrix, consumer, centerY, rotationRadians, depth, outerX, outerY, innerX, innerY, ICON_OUTLINE_COLOR);
    }

    private static void drawPingIconShape(
            Matrix4f matrix,
            VertexConsumer consumer,
            PlayerPingType type,
            float centerY,
            double rotationRadians,
            int color) {
        switch (type) {
            case FOCUS -> drawFocusOrb(matrix, consumer, centerY, rotationRadians, color);
            case NEED_HELP -> drawPlus(matrix, consumer, centerY, rotationRadians, color);
            case WAIT_HERE -> drawDownArrow(matrix, consumer, centerY, rotationRadians, color);
            case ATTACK_HERE -> drawTarget(matrix, consumer, centerY, rotationRadians / 2d, color);
            case LOOK_HERE -> drawEye(matrix, consumer, centerY, rotationRadians, color);
            case GROUP_UP -> drawGroupArrow(matrix, consumer, centerY, rotationRadians, color);
        }
    }

    private static void drawFocusOrb(
            Matrix4f matrix, VertexConsumer consumer, float centerY, double rotationRadians, int color) {
        float top = centerY + ORB_RADIUS;
        float bottom = centerY - ORB_RADIUS;

        for (int i = 0; i < 4; i++) {
            double start = rotationRadians + i * Math.PI / 2;
            double end = start + Math.PI / 2;

            float ax = (float) (Math.cos(start) * ORB_RADIUS);
            float az = (float) (Math.sin(start) * ORB_RADIUS);
            float bx = (float) (Math.cos(end) * ORB_RADIUS);
            float bz = (float) (Math.sin(end) * ORB_RADIUS);

            drawTriangle(matrix, consumer, ax, centerY, az, bx, centerY, bz, 0f, top, 0f, color);

            drawTriangle(matrix, consumer, ax, centerY, az, bx, centerY, bz, 0f, bottom, 0f, color);
        }

        drawSpark(matrix, consumer, (float) (Math.cos(rotationRadians) * ORB_SPARK_ORBIT_RADIUS), centerY, (float)
                (Math.sin(rotationRadians) * ORB_SPARK_ORBIT_RADIUS));
    }

    private static void drawSpark(
            Matrix4f matrix, VertexConsumer consumer, float centerX, float centerY, float centerZ) {
        float radius = ORB_SPARK_RADIUS;
        int color = 0xDCFFFFFF;

        drawTriangle(
                matrix,
                consumer,
                centerX - radius,
                centerY,
                centerZ,
                centerX + radius,
                centerY,
                centerZ,
                centerX,
                centerY + radius,
                centerZ,
                color);

        drawTriangle(
                matrix,
                consumer,
                centerX - radius,
                centerY,
                centerZ,
                centerX + radius,
                centerY,
                centerZ,
                centerX,
                centerY - radius,
                centerZ,
                color);
    }

    private static void drawPlus(
            Matrix4f matrix, VertexConsumer consumer, float centerY, double rotationRadians, int color) {
        drawExtrudedRectangle(matrix, consumer, centerY, rotationRadians, .13f, -.13f, .42f, .13f, -.42f, color);

        drawExtrudedRectangle(matrix, consumer, centerY, rotationRadians, .13f, .13f, .13f, .42f, -.13f, color);

        drawExtrudedRectangle(matrix, consumer, centerY, rotationRadians, .13f, -.42f, .13f, -.13f, -.13f, color);
    }

    private static void drawDownArrow(
            Matrix4f matrix, VertexConsumer consumer, float centerY, double rotationRadians, int color) {
        drawExtrudedRectangle(matrix, consumer, centerY, rotationRadians, .15f, -.13f, .42f, .13f, -.05f, color);

        drawExtrudedPolygon(
                matrix,
                consumer,
                centerY,
                rotationRadians,
                .15f,
                new float[] {-.34f, .34f, 0f},
                new float[] {-.05f, -.05f, -.42f},
                color);
    }

    private static void drawTarget(
            Matrix4f matrix, VertexConsumer consumer, float centerY, double rotationRadians, int color) {
        drawUprightRing(matrix, consumer, centerY, .38f, .27f, .08f, rotationRadians, color);

        drawExtrudedCircle(matrix, consumer, centerY, rotationRadians, .11f, .09f, color);
    }

    private static void drawEye(
            Matrix4f matrix, VertexConsumer consumer, float centerY, double rotationRadians, int color) {
        drawExtrudedContour(
                matrix,
                consumer,
                centerY,
                rotationRadians,
                .07f,
                EYE_OUTER_X,
                EYE_OUTER_Y,
                EYE_INNER_X,
                EYE_INNER_Y,
                color);

        drawUprightRing(matrix, consumer, centerY, .13f, .075f, .09f, rotationRadians, color);
    }

    private static void drawGroupArrow(
            Matrix4f matrix, VertexConsumer consumer, float centerY, double rotationRadians, int color) {
        drawExtrudedRectangle(matrix, consumer, centerY, rotationRadians, .13f, -.26f, .42f, -.14f, -.05f, color);

        drawExtrudedPolygon(
                matrix,
                consumer,
                centerY,
                rotationRadians,
                .13f,
                new float[] {-.40f, 0f, -.20f},
                new float[] {-.05f, -.05f, -.42f},
                color);

        drawExtrudedRectangle(matrix, consumer, centerY, rotationRadians, .13f, .14f, .42f, .26f, -.05f, color);

        drawExtrudedPolygon(
                matrix,
                consumer,
                centerY,
                rotationRadians,
                .13f,
                new float[] {0f, .40f, .20f},
                new float[] {-.05f, -.05f, -.42f},
                color);
    }

    private static void drawExtrudedRectangle(
            Matrix4f matrix,
            VertexConsumer consumer,
            float centerY,
            double rotationRadians,
            float depth,
            float left,
            float top,
            float right,
            float bottom,
            int color) {
        drawExtrudedPolygon(
                matrix,
                consumer,
                centerY,
                rotationRadians,
                depth,
                new float[] {left, right, right, left},
                new float[] {top, top, bottom, bottom},
                color);
    }

    private static void drawExtrudedCircle(
            Matrix4f matrix,
            VertexConsumer consumer,
            float centerY,
            double rotationRadians,
            float depth,
            float radius,
            int color) {
        int segments = ICON_CIRCLE_SEGMENTS;
        float[] x = new float[segments];
        float[] y = new float[segments];

        for (int i = 0; i < segments; i++) {
            double angle = i * Math.PI * 2d / segments;
            x[i] = (float) (Math.cos(angle) * radius);
            y[i] = (float) (Math.sin(angle) * radius);
        }

        drawExtrudedPolygon(matrix, consumer, centerY, rotationRadians, depth, x, y, color);
    }

    private static void drawUprightRing(
            Matrix4f matrix,
            VertexConsumer consumer,
            float centerY,
            float outerRadius,
            float innerRadius,
            float depth,
            double rotationRadians,
            int color) {
        int segments = ICON_CIRCLE_SEGMENTS;

        float[] outerX = new float[segments];
        float[] outerY = new float[segments];
        float[] innerX = new float[segments];
        float[] innerY = new float[segments];

        for (int i = 0; i < segments; i++) {
            double angle = i * Math.PI * 2d / segments;

            outerX[i] = (float) (Math.cos(angle) * outerRadius);
            outerY[i] = (float) (Math.sin(angle) * outerRadius);
            innerX[i] = (float) (Math.cos(angle) * innerRadius);
            innerY[i] = (float) (Math.sin(angle) * innerRadius);
        }

        drawExtrudedContour(matrix, consumer, centerY, rotationRadians, depth, outerX, outerY, innerX, innerY, color);
    }

    private static void drawExtrudedContour(
            Matrix4f matrix,
            VertexConsumer consumer,
            float centerY,
            double rotationRadians,
            float depth,
            float[] outerX,
            float[] outerY,
            float[] innerX,
            float[] innerY,
            int color) {
        float sin = (float) Math.sin(rotationRadians);
        float cos = (float) Math.cos(rotationRadians);

        for (int i = 0; i < outerX.length; i++) {
            int next = (i + 1) % outerX.length;

            drawExtrudedQuad(
                    matrix,
                    consumer,
                    centerY,
                    sin,
                    cos,
                    outerX[i],
                    outerY[i],
                    depth,
                    outerX[next],
                    outerY[next],
                    depth,
                    innerX[next],
                    innerY[next],
                    depth,
                    innerX[i],
                    innerY[i],
                    depth,
                    color);

            drawExtrudedQuad(
                    matrix,
                    consumer,
                    centerY,
                    sin,
                    cos,
                    outerX[i],
                    outerY[i],
                    -depth,
                    outerX[next],
                    outerY[next],
                    -depth,
                    outerX[next],
                    outerY[next],
                    depth,
                    outerX[i],
                    outerY[i],
                    depth,
                    color);

            drawExtrudedQuad(
                    matrix,
                    consumer,
                    centerY,
                    sin,
                    cos,
                    innerX[next],
                    innerY[next],
                    -depth,
                    innerX[i],
                    innerY[i],
                    -depth,
                    innerX[i],
                    innerY[i],
                    depth,
                    innerX[next],
                    innerY[next],
                    depth,
                    color);
        }
    }

    private static void drawExtrudedPolygon(
            Matrix4f matrix,
            VertexConsumer consumer,
            float centerY,
            double rotationRadians,
            float depth,
            float[] x,
            float[] y,
            int color) {
        float sin = (float) Math.sin(rotationRadians);
        float cos = (float) Math.cos(rotationRadians);

        for (int i = 1; i < x.length - 1; i++) {
            drawExtrudedTriangle(
                    matrix, consumer, centerY, sin, cos, x[0], y[0], depth, x[i], y[i], depth, x[i + 1], y[i + 1],
                    depth, color);
        }

        for (int i = 0; i < x.length; i++) {
            int next = (i + 1) % x.length;

            drawExtrudedQuad(
                    matrix, consumer, centerY, sin, cos, x[i], y[i], -depth, x[next], y[next], -depth, x[next], y[next],
                    depth, x[i], y[i], depth, color);
        }
    }

    private static void drawExtrudedTriangle(
            Matrix4f matrix,
            VertexConsumer consumer,
            float centerY,
            float sin,
            float cos,
            float ax,
            float ay,
            float az,
            float bx,
            float by,
            float bz,
            float cx,
            float cy,
            float cz,
            int color) {
        drawTriangle(
                matrix,
                consumer,
                ax * cos - az * sin,
                centerY + ay,
                ax * sin + az * cos,
                bx * cos - bz * sin,
                centerY + by,
                bx * sin + bz * cos,
                cx * cos - cz * sin,
                centerY + cy,
                cx * sin + cz * cos,
                color);
    }

    private static void drawExtrudedQuad(
            Matrix4f matrix,
            VertexConsumer consumer,
            float centerY,
            float sin,
            float cos,
            float ax,
            float ay,
            float az,
            float bx,
            float by,
            float bz,
            float cx,
            float cy,
            float cz,
            float dx,
            float dy,
            float dz,
            int color) {
        drawQuad(
                matrix,
                consumer,
                ax * cos - az * sin,
                centerY + ay,
                ax * sin + az * cos,
                bx * cos - bz * sin,
                centerY + by,
                bx * sin + bz * cos,
                cx * cos - cz * sin,
                centerY + cy,
                cx * sin + cz * cos,
                dx * cos - dz * sin,
                centerY + dy,
                dx * sin + dz * cos,
                color);
    }

    private static void drawTriangle(
            Matrix4f matrix,
            VertexConsumer consumer,
            float ax,
            float ay,
            float az,
            float bx,
            float by,
            float bz,
            float cx,
            float cy,
            float cz,
            int color) {
        drawQuad(matrix, consumer, ax, ay, az, bx, by, bz, cx, cy, cz, cx, cy, cz, color);
    }

    private static void drawPulseRings(
            Matrix4f matrix, VertexConsumer consumer, CustomColor color, float animationTime) {
        for (int i = 0; i < PULSE_COUNT; i++) {
            float progress = (cycleProgress(animationTime, PULSE_CYCLE_TICKS) + (float) i / PULSE_COUNT) % 1f;

            float easedProgress = 1f - (1f - progress) * (1f - progress);
            float radius = PULSE_MIN_RADIUS + (PULSE_MAX_RADIUS - PULSE_MIN_RADIUS) * easedProgress;

            drawSquareRing(
                    matrix,
                    consumer,
                    radius,
                    color.withAlpha(Math.round(180 * (1f - progress))).asInt());
        }
    }

    private static void drawSquareRing(Matrix4f matrix, VertexConsumer consumer, float radius, int color) {
        float innerRadius = Math.max(0f, radius - PULSE_THICKNESS / 2f);
        float outerRadius = radius + PULSE_THICKNESS / 2f;

        drawQuad(
                matrix,
                consumer,
                -innerRadius,
                PULSE_HEIGHT,
                -innerRadius,
                -outerRadius,
                PULSE_HEIGHT,
                -outerRadius,
                outerRadius,
                PULSE_HEIGHT,
                -outerRadius,
                innerRadius,
                PULSE_HEIGHT,
                -innerRadius,
                color);

        drawQuad(
                matrix,
                consumer,
                innerRadius,
                PULSE_HEIGHT,
                -innerRadius,
                outerRadius,
                PULSE_HEIGHT,
                -outerRadius,
                outerRadius,
                PULSE_HEIGHT,
                outerRadius,
                innerRadius,
                PULSE_HEIGHT,
                innerRadius,
                color);

        drawQuad(
                matrix,
                consumer,
                innerRadius,
                PULSE_HEIGHT,
                innerRadius,
                outerRadius,
                PULSE_HEIGHT,
                outerRadius,
                -outerRadius,
                PULSE_HEIGHT,
                outerRadius,
                -innerRadius,
                PULSE_HEIGHT,
                innerRadius,
                color);

        drawQuad(
                matrix,
                consumer,
                -innerRadius,
                PULSE_HEIGHT,
                innerRadius,
                -outerRadius,
                PULSE_HEIGHT,
                outerRadius,
                -outerRadius,
                PULSE_HEIGHT,
                -outerRadius,
                -innerRadius,
                PULSE_HEIGHT,
                -innerRadius,
                color);
    }

    private static void drawQuad(
            Matrix4f matrix,
            VertexConsumer consumer,
            float ax,
            float ay,
            float az,
            float bx,
            float by,
            float bz,
            float cx,
            float cy,
            float cz,
            float dx,
            float dy,
            float dz,
            int color) {
        addVertex(matrix, consumer, ax, ay, az, color);
        addVertex(matrix, consumer, bx, by, bz, color);
        addVertex(matrix, consumer, cx, cy, cz, color);
        addVertex(matrix, consumer, dx, dy, dz, color);
    }

    private static void addVertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, int color) {
        consumer.addVertex(matrix, x, y, z).setColor(color);
    }

    private static void submitTargetLabel(
            OrderedSubmitNodeCollector submitNodeCollector,
            PoseStack poseStack,
            Direction direction,
            float markerScale,
            String targetName,
            int color) {
        Font font = McUtils.mc().font;

        poseStack.pushPose();

        float labelDistance = LABEL_DISTANCE * markerScale;

        poseStack.translate(
                direction.getStepX() * labelDistance,
                direction.getStepY() * labelDistance,
                direction.getStepZ() * labelDistance);

        poseStack.mulPose(McUtils.mc().gameRenderer.getMainCamera().rotation());
        poseStack.scale(0.025f, -0.025f, 0.025f);

        List<FormattedCharSequence> lines = font.split(
                Component.literal(targetName).withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)),
                200);

        int offsetY = -(font.lineHeight * lines.size()) / 2;

        for (FormattedCharSequence line : lines) {
            int offsetX = -font.width(line) / 2;

            submitNodeCollector.submitText(
                    poseStack,
                    offsetX,
                    offsetY,
                    line,
                    false,
                    Font.DisplayMode.SEE_THROUGH,
                    0xF000F0,
                    color,
                    0x80000000,
                    0);

            offsetY += font.lineHeight + 2;
        }

        poseStack.popPose();
    }
}
