package com.wynntils.utils.rendering;

import com.mojang.math.Vector3f;

public class MatrixMathUtils {

    public static Matrix4f rotate(float angle, Vector3f axis, Matrix4f src, Matrix4f dest) {
        return rotate(angle, axis.x(), axis.y(), axis.z(), src, dest);
    }

    public static Matrix4f rotate(float angle, float x_axis, float y_axis, float z_axis, Matrix4f src, Matrix4f dest) {
        if (dest == null) dest = new Matrix4f();

        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float oneminusc = 1.0f - c;

        float xy = x_axis * y_axis;
        float yz = y_axis * z_axis;
        float xz = x_axis * z_axis;
        float xs = x_axis * s;
        float ys = y_axis * s;
        float zs = z_axis * s;

        float f00 = x_axis * x_axis * oneminusc + c;
        float f01 = xy * oneminusc + zs;
        float f02 = xz * oneminusc - ys;

        float f10 = xy * oneminusc - zs;
        float f11 = y_axis * y_axis * oneminusc + c;
        float f12 = yz * oneminusc + xs;

        float f20 = xz * oneminusc + ys;
        float f21 = yz * oneminusc - xs;
        float f22 = z_axis * z_axis * oneminusc + c;

        float t00 = src.m00 * f00 + src.m10 * f01 + src.m20 * f02;
        float t01 = src.m01 * f00 + src.m11 * f01 + src.m21 * f02;
        float t02 = src.m02 * f00 + src.m12 * f01 + src.m22 * f02;
        float t03 = src.m03 * f00 + src.m13 * f01 + src.m23 * f02;
        float t10 = src.m00 * f10 + src.m10 * f11 + src.m20 * f12;
        float t11 = src.m01 * f10 + src.m11 * f11 + src.m21 * f12;
        float t12 = src.m02 * f10 + src.m12 * f11 + src.m22 * f12;
        float t13 = src.m03 * f10 + src.m13 * f11 + src.m23 * f12;
        float t20 = src.m00 * f20 + src.m10 * f21 + src.m20 * f22;
        float t21 = src.m01 * f20 + src.m11 * f21 + src.m21 * f22;
        float t22 = src.m02 * f20 + src.m12 * f21 + src.m22 * f22;
        float t23 = src.m03 * f20 + src.m13 * f21 + src.m23 * f22;

        dest.m00 = t00;
        dest.m01 = t01;
        dest.m02 = t02;
        dest.m03 = t03;
        dest.m10 = t10;
        dest.m11 = t11;
        dest.m12 = t12;
        dest.m13 = t13;
        dest.m20 = t20;
        dest.m21 = t21;
        dest.m22 = t22;
        dest.m23 = t23;

        return dest;
    }

}
