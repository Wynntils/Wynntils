#version 330

// Textured, colored quad (same shape as vanilla's core/position_tex_color) that also carries a
// per-vertex "circle-local" coordinate, used by the paired fragment shader to discard/fade pixels
// outside an arbitrary circular (or elliptical) mask - e.g. a minimap icon clipped to the round
// minimap's boundary, matching how the map texture itself is already clipped.
//
// There is no generic per-vertex float channel available through Minecraft's GuiElementRenderState
// / VertexConsumer API (only the fixed Position/Color/UV0/UV1/UV2/Normal/LineWidth attributes it
// exposes setters for), so the circle-local coordinate is packed into UV1 - vanilla declares this
// attribute as an ivec2 itself (see core/entity.vsh, used there as raw overlay-texture texel
// coordinates via texelFetch), so it is passed through as raw integers regardless of how it's used;
// here it is decoded as a coordinate scaled by CIRCLE_COORD_SCALE instead of a texel lookup index.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in ivec2 UV1;

out vec2 texCoord0;
out vec4 vertexColor;
out vec2 circleCoord;

const float CIRCLE_COORD_SCALE = 8000.0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexColor = Color;
    circleCoord = vec2(UV1) / CIRCLE_COORD_SCALE;
}
