#version 330

// Paired with vanilla's core/position_tex_color vertex shader (see
// CustomRenderPipelines.CIRCLE_MASK_PIPELINE). There is no sampler here - UV0 is reused to carry
// each vertex's normalized position in [-1, 1] across the quad's bounding box, and this fragment
// shader masks it to an ellipse via a distance test.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float dist = length(texCoord0);
    float edge = fwidth(dist);
    float coverage = 1.0 - smoothstep(1.0 - edge, 1.0 + edge, dist);

    if (coverage <= 0.0) {
        discard;
    }

    vec4 color = vertexColor;
    color.a *= coverage;
    fragColor = color * ColorModulator;
}
