#version 330

// Paired with vanilla's core/position_tex_color vertex shader (see
// CustomRenderPipelines.FOG_PARCHMENT_PIPELINE). Like fog_mask.fsh, Sampler0 is the per-chunk
// discovery mask (padded by one texel per side) and texCoord0 is in mask space. Sampler1 is the map
// tile itself: undiscovered terrain is redrawn as a monotone ink-on-parchment ramp of the tile's
// luminance, lightly blurred and posterised into a few flat tones so that only the broad geography
// (coast, forest, mountain masses) stays legible while fine detail, colour, names and icons are
// withheld. Water keeps a distinct pale, cool tone so coastlines and rivers read. The vertex alpha is the fog strength: it blends between plain greyscale (0) and the
// full ramp (1), never back towards the original colours.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

const vec3 INK = vec3(0.44, 0.36, 0.27);
const vec3 PAPER = vec3(0.94, 0.88, 0.74);
const float MIN_LUMINANCE = 0.35;
const float TONE_BANDS = 4.0;
const float BLUR_TEXELS = 1.0;
// Water on the map is a deeply saturated blue; ice and snow are only faintly blue-tinted
const vec3 WATER = vec3(0.84, 0.85, 0.80);
const float WATER_BLUE_DOMINANCE = 0.30;

void main() {
    float discovered = smoothstep(0.0, 1.0, texture(Sampler0, texCoord0).r);

    // Strip the one-texel padding ring to get back to tile space
    vec2 maskTexels = vec2(textureSize(Sampler0, 0));
    vec2 tileCoord = (texCoord0 * maskTexels - 1.0) / (maskTexels - 2.0);
    vec2 blur = BLUR_TEXELS / vec2(textureSize(Sampler1, 0));
    vec4 tile = (texture(Sampler1, tileCoord)
                    + texture(Sampler1, tileCoord + vec2(blur.x, 0.0))
                    + texture(Sampler1, tileCoord - vec2(blur.x, 0.0))
                    + texture(Sampler1, tileCoord + vec2(0.0, blur.y))
                    + texture(Sampler1, tileCoord - vec2(0.0, blur.y)))
            / 5.0;

    bool isWater = tile.b - max(tile.r, tile.g) > WATER_BLUE_DOMINANCE;
    float luminance = dot(tile.rgb, vec3(0.299, 0.587, 0.114));
    luminance = floor(luminance * TONE_BANDS + 0.5) / TONE_BANDS;
    vec3 parchment = isWater ? WATER : mix(INK, PAPER, mix(MIN_LUMINANCE, 1.0, luminance));
    vec4 color = vec4(mix(vec3(luminance), parchment, vertexColor.a), tile.a * (1.0 - discovered));

    if (color.a == 0.0) {
        discard;
    }

    fragColor = color * ColorModulator;
}
