#version 330

// Paired with vanilla's core/position_tex_color vertex shader (see
// CustomRenderPipelines.FOG_MASK_PIPELINE). Sampler0 is a per-map-tile fog mask with one texel per
// chunk (red = 1.0 where the chunk is discovered), sampled with LINEAR filtering so the boundary
// between discovered and undiscovered chunks fades over one chunk instead of stepping. The quad is
// drawn over the map tile in the fog colour, with its alpha scaled by how undiscovered the pixel is.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float discovered = smoothstep(0.0, 1.0, texture(Sampler0, texCoord0).r);

    vec4 color = vertexColor;
    color.a *= 1.0 - discovered;

    if (color.a == 0.0) {
        discard;
    }

    fragColor = color * ColorModulator;
}
