#include veil:common
#include veil:space_helper

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D LightSampler;
uniform sampler2D NormalSampler;
uniform sampler2D WaterNormalSampler;
uniform sampler2D StarsSampler;

uniform float Intensity;
uniform float BlurAmount;
uniform float ReflectionStrength;
uniform float ReflectionSoftness;
uniform int WaterEffectsEnabled;
uniform float NightSkyStrength;
uniform float FogIntensity;
uniform float FogVariation;
uniform float LightVariation;
uniform float GameTime;
uniform int PresetIndex;
uniform int FilmGrainEnabled;
uniform int VignetteEnabled;
uniform int ScanlinesEnabled;
uniform int ChromaticAberrationEnabled;

uniform int StarsEnabled;
uniform float StarBrightness;
uniform float StarTwinkle;
uniform int CelestialSphere;
uniform float CameraYaw;
uniform float CameraPitch;
uniform float CameraFov;
uniform float AspectRatio;
uniform float SkyAngle;

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

vec3 decodeNormal(vec3 encodedNormal) {
    return normalize((encodedNormal * 2.0) - 1.0);
}

float waterSurfaceMask(vec3 albedo, vec3 normalVS, float depth) {
    float brightness = dot(albedo, vec3(0.299, 0.587, 0.114));
    float flatness = smoothstep(0.32, 0.96, abs(normalVS.z));
    float blueBias = smoothstep(0.02, 0.18, albedo.b - max(albedo.r, albedo.g * 0.92));
    float aquaBias = smoothstep(0.12, 0.44, albedo.g - albedo.r * 0.45);
    float visibility = 1.0 - smoothstep(0.985, 1.0, depth);
    float darknessReject = 1.0 - smoothstep(0.0, 0.06, brightness);
    return clamp(flatness * blueBias * aquaBias * visibility * darknessReject, 0.0, 1.0);
}

vec3 sampleWaterNormal(vec2 uv, float time) {
    vec2 waveUvA = (uv * vec2(7.0, 5.0)) + vec2(time * 0.018, -time * 0.011);
    vec2 waveUvB = (uv * vec2(4.0, 6.5)) + vec2(-time * 0.009, time * 0.015);
    vec3 waveA = decodeNormal(texture(WaterNormalSampler, waveUvA).xyz);
    vec3 waveB = decodeNormal(texture(WaterNormalSampler, waveUvB).xyz);
    vec3 combined = normalize(vec3(waveA.xy + waveB.xy, max(0.35, waveA.z + waveB.z)));
    return combined;
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
    vec4 baseColor = texture(DiffuseSampler0, texCoord);

    // Sample depth from Minecraft framebuffer
    float depth = texture(DiffuseDepthSampler, texCoord).r;
    bool isSky = depth >= 0.99999;

    // 1. Chromatic aberration & blur (only when Post Effects is enabled)
    vec3 sourceColor;
    if (Intensity > 0.001) {
        vec2 offset = vec2(0.0);
        if (ChromaticAberrationEnabled != 0) {
            offset = centeredUv * 0.0035 * strength;
        }
        float red = texture(DiffuseSampler0, texCoord + offset).r;
        vec2 blueOffset = PresetIndex == 2 ? offset * 1.6 : offset * 0.8;
        float blue = texture(DiffuseSampler0, texCoord - blueOffset).b;
        vec3 blurredColor = sampleBlur(texCoord, BlurAmount * strength);
        vec3 chromaticColor = vec3(red, mix(baseColor.g, blurredColor.g, clamp(BlurAmount, 0.0, 1.0)), blue);
        sourceColor = mix(baseColor.rgb, chromaticColor, clamp(strength, 0.0, 1.0));
    } else {
        sourceColor = baseColor.rgb;
    }

    vec3 color = sourceColor;

    // 2. Lighting compositing from Veil deferred light buffer
    if (!isSky) {
        vec3 light = texture(LightSampler, texCoord).rgb;
        float lightFlicker = 1.0 + sin(GameTime * 2.5) * 0.04 * LightVariation;
        vec3 adjustedLight = light * lightFlicker;
        color += adjustedLight * 0.75;
    }

    // 3. Water effects (specular shimmer on water surfaces)
    if (WaterEffectsEnabled != 0 && !isSky) {
        vec3 normalVS = texture(NormalSampler, texCoord).xyz;
        float flatness = smoothstep(0.4, 0.95, abs(normalVS.z));
        float blueRatio = sourceColor.b - max(sourceColor.r, sourceColor.g * 0.9);
        float waterHint = clamp(flatness * smoothstep(0.02, 0.15, blueRatio), 0.0, 1.0);
        if (waterHint > 0.01) {
            vec2 waveUv = (texCoord * vec2(8.0, 6.0)) + vec2(GameTime * 0.02, -GameTime * 0.015);
            vec3 waveNormal = decodeNormal(texture(WaterNormalSampler, waveUv).xyz);
            vec3 sunDir = normalize(vec3(-0.35, 0.45, 0.82));
            float spec = pow(max(dot(waveNormal, sunDir), 0.0), 32.0);
            color += spec * 0.35 * ReflectionStrength * waterHint;
        }
    }

    // 4. Smooth atmospheric distance fog (ONLY on terrain, NEVER on the sky!)
    if (!isSky && FogIntensity > 0.001) {
        vec3 viewPos = screenToLocalSpace(texCoord, depth).xyz;
        float dist = length(viewPos);
        float fogFactor = smoothstep(24.0, 180.0, dist) * FogIntensity;
        if (fogFactor > 0.001) {
            vec3 fogCol = presetFogColor(PresetIndex);
            color = mix(color, fogCol, clamp(fogFactor, 0.0, 0.8));
        }
    }

    // 5. Preset color grading (only when Post Effects is enabled)
    if (Intensity > 0.001) {
        if (PresetIndex == 1) { // Warm
            color = mix(color, vec3(dot(color, vec3(0.299, 0.587, 0.114))), 0.08 * strength);
            color *= vec3(1.04, 0.98, 0.92);
        } else if (PresetIndex == 2) { // Moonlit
            color *= vec3(0.92, 1.01, 1.07);
        }
    }

    // 6. Stars rendering (in the sky during night)
    if (isSky && StarsEnabled != 0 && NightSkyStrength > 0.001) {
        vec3 worldDir;
        if (CelestialSphere != 0) {
            worldDir = viewDirFromUv(texCoord);
        } else {
            worldDir = vec3((texCoord - 0.5) * 2.0, 1.0);
        }

        // Fade stars near the horizon
        float horizonFade = smoothstep(-0.02, 0.12, worldDir.y);
        if (horizonFade > 0.001) {
            float azimuth = atan(worldDir.x, worldDir.z) / 6.2831853 + 0.5;
            float elevation = asin(clamp(worldDir.y, 0.0, 1.0)) / 1.5707963;

            // Rotate stars across the celestial sphere with world time
            vec2 starsUv = fract(vec2(azimuth * 4.0 + SkyAngle * 0.5, elevation * 3.0));

            // Sample custom stars texture
            vec3 starTex = texture(StarsSampler, starsUv).rgb;
            float starLuma = max(max(starTex.r, starTex.g), starTex.b);

            // Twinkle calculation
            float twinkle = 1.0;
            if (StarTwinkle > 0.01) {
                float tHash = hash(floor(starsUv * 256.0) + floor(GameTime * 5.0));
                twinkle = (1.0 - StarTwinkle * 0.45) + tHash * StarTwinkle * 0.5;
            }

            // Amplify texture star points
            vec3 starLight = starTex * (1.0 + smoothstep(0.04, 0.20, starLuma) * 3.5) * twinkle * StarBrightness * 2.0;

            // Procedural crisp star field overlay to guarantee sparkling starry sky
            vec2 pGrid = starsUv * 140.0;
            vec2 pCell = floor(pGrid);
            vec2 pUv = fract(pGrid) - 0.5;
            float pSeed = hash(pCell * 19.17 + 7.31);
            if (pSeed > 0.965) {
                float pDist = length(pUv);
                float pIntensity = smoothstep(0.14, 0.0, pDist);
                float pTwink = (1.0 - StarTwinkle * 0.4) + sin(GameTime * 4.0 + pSeed * 45.0) * StarTwinkle * 0.4;
                starLight += vec3(pIntensity * (pSeed - 0.965) * 28.0 * pTwink * StarBrightness);
            }

            // ADDITIVE sky blending: stars emit light onto the night sky
            color += starLight * horizonFade * NightSkyStrength;
        }
    }

    // 7. Intentional Cinematic Post Effects (Film Grain, Scanlines, Vignette)
    // ONLY applied when Post Effects is enabled in configuration!
    if (Intensity > 0.001) {
        if (FilmGrainEnabled != 0) {
            float grain = hash(texCoord * vec2(1920.0, 1080.0) + fract(GameTime * 17.0)) - 0.5;
            color += grain * 0.05 * strength;
        }

        if (ScanlinesEnabled != 0) {
            float scanline = sin(texCoord.y * 540.0 * 3.14159) * 0.5 + 0.5;
            color *= 1.0 - (scanline * 0.08 * strength);
        }

        if (VignetteEnabled != 0) {
            float dist = length(centeredUv * vec2(1.0, 1.0 / max(0.1, AspectRatio)));
            float vignette = smoothstep(0.38, 0.95, dist);
            color *= 1.0 - (vignette * 0.5 * strength);
        }
    }

    fragColor = vec4(clamp(color, 0.0, 1.0), baseColor.a);
}