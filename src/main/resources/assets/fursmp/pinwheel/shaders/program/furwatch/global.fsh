#include veil:common

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D LightSampler;
uniform sampler2D NormalSampler;

uniform float Intensity;
uniform float BlurAmount;
uniform float ReflectionStrength;
uniform float ReflectionSoftness;
uniform float FogIntensity;
uniform float FogVariation;
uniform float LightVariation;
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

float materialWeight(vec3 albedo, float low, float high, float chromaLimit) {
    float luma = dot(albedo, vec3(0.299, 0.587, 0.114));
    float chroma = max(max(albedo.r, albedo.g), albedo.b) - min(min(albedo.r, albedo.g), albedo.b);
    return smoothstep(low, high, luma) * (1.0 - smoothstep(chromaLimit, chromaLimit + 0.18, chroma));
}

vec4 classifySurface(vec3 albedo, vec3 normalVS) {
    float luma = dot(albedo, vec3(0.299, 0.587, 0.114));
    float chroma = max(max(albedo.r, albedo.g), albedo.b) - min(min(albedo.r, albedo.g), albedo.b);
    float flatness = smoothstep(0.25, 0.95, abs(normalVS.z));

    float polished = materialWeight(albedo, 0.18, 0.72, 0.22) * flatness;
    float roughMetal = materialWeight(albedo, 0.08, 0.58, 0.28) * (1.0 - polished * 0.65);
    float tile = smoothstep(0.58, 0.92, luma) * (1.0 - smoothstep(0.16, 0.42, chroma)) * (1.0 - polished * 0.55);
    float stone = smoothstep(0.18, 0.62, 1.0 - luma) * (1.0 - smoothstep(0.22, 0.48, chroma));

    float total = polished + roughMetal + tile + stone;
    if (total <= 0.0001) {
        return vec4(0.1, 0.3, 0.25, 0.35);
    }

    return vec4(polished, roughMetal, tile, stone) / total;
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

vec3 presetFogColor(int presetIndex) {
    if (presetIndex == 1) {
        return vec3(0.62, 0.48, 0.35);
    }
    if (presetIndex == 2) {
        return vec3(0.28, 0.36, 0.48);
    }
    return vec3(0.46, 0.48, 0.52);
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

    vec3 normalVS = texture(NormalSampler, texCoord).xyz;
    vec3 light = texture(LightSampler, texCoord).rgb;
    float depth = texture(DiffuseDepthSampler, texCoord).r;
    vec4 surface = classifySurface(sourceColor, normalVS);
    float reflectivity = (surface.x * 0.62) + (surface.y * 0.24) + (surface.z * 0.14) + (surface.w * 0.06);
    float roughness = clamp((surface.y * 0.65) + (surface.z * 0.55) + (surface.w * 0.82), 0.06, 0.95);
    float lightLuma = dot(light, vec3(0.299, 0.587, 0.114));
    float variationNoise = hash(texCoord * vec2(941.0, 733.0) + GameTime * 2.7) - 0.5;
    vec3 adjustedLight = light * (1.0 + (variationNoise * LightVariation * 0.18));

    vec3 keyLightDir = normalize(vec3(-0.35, 0.42, 0.84));
    float highlightPower = mix(8.0, 48.0, clamp(1.0 - roughness - ReflectionSoftness * 0.35, 0.0, 1.0));
    float specular = pow(max(dot(normalize(normalVS), keyLightDir), 0.0), highlightPower);
    vec3 highlightColor = adjustedLight * specular * ReflectionStrength * reflectivity * (0.65 + surface.x * 0.45 + surface.y * 0.2);

    vec2 reflectionOffset = (normalVS.xy * (0.03 + ReflectionSoftness * 0.08)) + (centeredUv * -0.035);
    vec2 reflectionUv = clamp(texCoord + reflectionOffset, vec2(0.001), vec2(0.999));
    vec3 reflectionSample = sampleBlur(reflectionUv, ReflectionSoftness + (roughness * 0.45));
    vec3 reflectedColor = reflectionSample * (0.3 + adjustedLight * 0.7);
    float reflectionMask = ReflectionStrength * reflectivity * smoothstep(0.04, 0.85, lightLuma + 0.08);

    color += adjustedLight * (0.9 + surface.x * 0.35 + surface.z * 0.1 - surface.w * 0.08);
    color = mix(color, reflectedColor, clamp(reflectionMask, 0.0, 0.85));
    color += highlightColor;

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

    float fogNoise = hash((texCoord * vec2(640.0, 360.0)) + vec2(GameTime * 0.85, GameTime * 0.31));
    float fogVariation = mix(1.0 - FogVariation * 0.35, 1.0 + FogVariation * 0.65, fogNoise);
    float fogFactor = smoothstep(0.18, 0.985, depth) * FogIntensity * fogVariation;
    vec3 fogColor = presetFogColor(PresetIndex) + (adjustedLight * 0.16);
    color = mix(color, fogColor, clamp(fogFactor, 0.0, 0.92));
    color += adjustedLight * clamp(fogFactor * 0.18, 0.0, 0.24);

    fragColor = vec4(clamp(color, 0.0, 1.0), baseColor.a);
}