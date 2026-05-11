#include veil:common

uniform sampler2D DiffuseSampler0;

uniform float Intensity;
uniform float BlurAmount;
uniform float GameTime;
uniform int PresetIndex;
uniform int FilmGrainEnabled;
uniform int VignetteEnabled;
uniform int ScanlinesEnabled;
uniform int ChromaticAberrationEnabled;

in vec2 texCoord;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7))) * 43758.5453123);
}

vec3 sampleBlur(vec2 uv, float amount) {
    if (amount <= 0.0001) {
        return texture(DiffuseSampler0, uv).rgb;
    }

    vec2 texel = amount * 2.5 / vec2(textureSize(DiffuseSampler0, 0));
    vec3 color = texture(DiffuseSampler0, uv).rgb * 0.227027;
    color += texture(DiffuseSampler0, uv + vec2(texel.x, 0.0)).rgb * 0.1945946;
    color += texture(DiffuseSampler0, uv - vec2(texel.x, 0.0)).rgb * 0.1945946;
    color += texture(DiffuseSampler0, uv + vec2(0.0, texel.y)).rgb * 0.1945946;
    color += texture(DiffuseSampler0, uv - vec2(0.0, texel.y)).rgb * 0.1945946;
    return color;
}

void main() {
    float presetBoost = 1.0 + (float(PresetIndex) * 0.15);
    float strength = Intensity * presetBoost;
    vec2 centeredUv = texCoord - vec2(0.5);
    vec2 offset = vec2(0.0);
    vec4 baseColor = texture(DiffuseSampler0, texCoord);
    vec3 blurredColor = sampleBlur(texCoord, BlurAmount);
    vec3 sourceColor = mix(baseColor.rgb, blurredColor, clamp(BlurAmount, 0.0, 1.0));

    if (ChromaticAberrationEnabled != 0) {
        offset = centeredUv * 0.004 * strength;
    }

    float red = texture(DiffuseSampler0, texCoord + offset).r;
    vec2 blueOffset = PresetIndex == 2 ? offset * 1.8 : offset * 0.9;
    float blue = texture(DiffuseSampler0, texCoord - blueOffset).b;
    vec3 color = vec3(red, sourceColor.g, blue);

    if (FilmGrainEnabled != 0) {
        float grain = hash(texCoord * vec2(1920.0, 1080.0) + GameTime * 12.0) - 0.5;
        color += grain * 0.06 * strength;
    }

    if (ScanlinesEnabled != 0) {
        float scanlines = sin((texCoord.y * 960.0) + GameTime * 22.0) * 0.04 * strength;
        color -= scanlines;
    }

    if (VignetteEnabled != 0) {
        float distanceToCenter = dot(centeredUv, centeredUv) * 3.2;
        float vignette = smoothstep(0.35, 1.1, distanceToCenter);
        color *= 1.0 - vignette * 0.55 * strength;
    }

    if (PresetIndex == 1) {
        color = mix(color, vec3(dot(color, vec3(0.299, 0.587, 0.114))), 0.18 * strength);
    } else if (PresetIndex == 2) {
        color *= vec3(0.92, 1.03, 1.08);
    }

    fragColor = vec4(clamp(color, 0.0, 1.0), baseColor.a);
}