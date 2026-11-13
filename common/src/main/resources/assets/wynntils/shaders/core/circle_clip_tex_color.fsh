#version 330

// Paired with core/circle_clip_tex_color.vsh. Same texture-sample-times-vertex-color logic as
// vanilla's core/position_tex_color.fsh, plus a smooth (anti-aliased) discard/fade of pixels
// outside the unit circle in circleCoord space - the same distance test as
// assets/wynntils/shaders/core/circle_mask.fsh, applied here per-icon instead of to a flat
// background quad, so an icon crossing the mask boundary clips exactly like the map tile does
// instead of being shown/hidden as a whole based on its center point.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;
in vec2 circleCoord;

out vec4 fragColor;

void main() {
    float dist = length(circleCoord);
    float edge = fwidth(dist);
    float coverage = 1.0 - smoothstep(1.0 - edge, 1.0 + edge, dist);

    if (coverage <= 0.0) {
        discard;
    }

    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    color.a *= coverage;

    if (color.a == 0.0) {
        discard;
    }

    fragColor = color * ColorModulator;
}
