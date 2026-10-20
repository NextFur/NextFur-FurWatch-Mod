package net.nextfur.fwc.client.sanity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.init.FwModSounds;
import net.nextfur.fwc.sanity.SanityData;
import net.nextfur.fwc.sanity.SanityService;

public final class SanityClientHandler {
    private static final ResourceLocation VIGNETTE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");
    private static final ResourceLocation PDA_BLUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "textures/gui/pda/pda_blur.png");
    private static final ResourceLocation PDA_VIGNETTE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "textures/gui/pda/pda_vignette.png");
    private static final ResourceLocation VHS_NOISE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "textures/shaders/vhs_noise.png");
    private static final ResourceLocation SUPER_NOISE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "textures/shaders/super_noise.png");

    private static float clientSanity = SanityData.MAX_SANITY;
    private static float smoothedIntensity = 0.0f;

    private static int heartbeatTimer = 40;
    private static float heartbeatPulse = 0.0f;

    private static int hallucinationCooldown = 100;
    private static float blurSpike = 0.0f;
    private static float darknessWave = 0.0f;
    private static float darknessWaveTarget = 0.0f;
    private static int darknessWaveTimer = 60;

    private static int cameraTremorTicks = 0;
    private static float tremorIntensity = 0.0f;

    private static int noiseOffsetU = 0;
    private static int noiseOffsetV = 0;

    private SanityClientHandler() {
    }

    public static void onSanitySynced(float sanity, boolean triggerSpike) {
        clientSanity = Mth.clamp(sanity, SanityData.MIN_SANITY, SanityData.MAX_SANITY);
        if (triggerSpike && clientSanity < SanityService.LOW_SANITY_START) {
            float targetIntensity = SanityService.getInsanityIntensity(clientSanity);
            blurSpike = Math.min(1.0f, blurSpike + 0.45f + 0.45f * targetIntensity);
            cameraTremorTicks = Math.max(cameraTremorTicks, 25);
            tremorIntensity = Math.max(tremorIntensity, 0.35f + 0.65f * targetIntensity);
        }
    }

    public static float getClientSanity() {
        return clientSanity;
    }

    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clientSanity = SanityData.MAX_SANITY;
        smoothedIntensity = 0.0f;
        heartbeatPulse = 0.0f;
        blurSpike = 0.0f;
        darknessWave = 0.0f;
        darknessWaveTarget = 0.0f;
        cameraTremorTicks = 0;
        tremorIntensity = 0.0f;
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.isPaused()) {
            return;
        }

        if (player.isCreative() || player.isSpectator() || !player.isAlive()) {
            smoothedIntensity = Mth.lerp(0.1f, smoothedIntensity, 0.0f);
            heartbeatPulse *= 0.85f;
            blurSpike *= 0.85f;
            darknessWave *= 0.85f;
            cameraTremorTicks = 0;
            return;
        }

        float targetIntensity = SanityService.getInsanityIntensity(clientSanity);
        smoothedIntensity = Mth.lerp(0.05f, smoothedIntensity, targetIntensity);

        // Decay transient pulses
        heartbeatPulse *= 0.86f;
        blurSpike *= 0.94f;
        if (cameraTremorTicks > 0) {
            cameraTremorTicks--;
        } else {
            tremorIntensity *= 0.85f;
        }

        if (smoothedIntensity <= 0.002f) {
            darknessWave = Mth.lerp(0.08f, darknessWave, 0.0f);
            return;
        }

        RandomSource random = player.getRandom();
        noiseOffsetU = random.nextInt(256);
        noiseOffsetV = random.nextInt(256);

        // 1. Heartbeat loop (starts below ~30% sanity and speeds up exponentially toward 10%)
        if (clientSanity < 30.0f) {
            if (heartbeatTimer > 0) {
                heartbeatTimer--;
            } else {
                int interval = Mth.lerpInt(smoothedIntensity, 56, 18);
                heartbeatTimer = interval;

                float volume = Mth.lerp(smoothedIntensity, 0.22f, 0.90f);
                float pitch = Mth.lerp(smoothedIntensity, 0.85f, 1.15f);
                mc.getSoundManager().play(SimpleSoundInstance.forUI(FwModSounds.HEARTBEAT.get(), pitch, volume));

                heartbeatPulse = Math.min(1.0f, 0.30f + 0.70f * smoothedIntensity);
            }
        }

        // 2. Darkness & Blur Waves (periodic waves of heavy darkness/blur that swell and recede)
        if (darknessWaveTimer > 0) {
            darknessWaveTimer--;
        } else {
            // More frequent and deeper darkness waves as sanity approaches 10%
            if (random.nextFloat() < (0.15f + 0.65f * smoothedIntensity)) {
                darknessWaveTarget = Mth.lerp(smoothedIntensity, 0.20f, 0.85f) * (0.6f + 0.4f * random.nextFloat());
                blurSpike = Math.max(blurSpike, darknessWaveTarget * 0.75f);
                darknessWaveTimer = Mth.lerpInt(smoothedIntensity, 80, 140);
            } else {
                darknessWaveTarget = 0.0f;
                darknessWaveTimer = Mth.lerpInt(smoothedIntensity, 160, 45);
            }
        }
        darknessWave = Mth.lerp(0.04f, darknessWave, darknessWaveTarget);

        // 3. Random Scary Sounds (Auditory Hallucinations)
        if (hallucinationCooldown > 0) {
            hallucinationCooldown--;
        } else {
            // Exponential chance per tick once off cooldown
            float chancePerTick = 0.002f + (0.038f * smoothedIntensity);
            if (random.nextFloat() < chancePerTick) {
                playRandomScarySound(mc, player, random, smoothedIntensity);

                // Cooldown shrinks exponentially as intensity approaches 1.0:
                // ~25-45s near 34% sanity -> ~4-9s at <=10% sanity
                int minCooldown = Mth.lerpInt(smoothedIntensity, 500, 80);
                int maxCooldown = Mth.lerpInt(smoothedIntensity, 900, 180);
                hallucinationCooldown = Mth.nextInt(random, minCooldown, maxCooldown);
            }
        }
    }

    private static void playRandomScarySound(
            Minecraft mc,
            LocalPlayer player,
            RandomSource random,
            float intensity
    ) {
        int roll = random.nextInt(100);
        float volume = Mth.lerp(intensity, 0.35f, 0.95f) * (0.8f + 0.4f * random.nextFloat());
        float pitch = 0.70f + random.nextFloat() * 0.45f;

        if (roll < 22) {
            // FurWatch insanity whisper/drone
            SoundEvent sound = random.nextBoolean() ? FwModSounds.INSANITY.get() : FwModSounds.INSANITY1.get();
            mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
            blurSpike = Math.min(1.0f, blurSpike + 0.55f + 0.35f * intensity);
            cameraTremorTicks = 30;
            tremorIntensity = 0.4f + 0.6f * intensity;
        } else if (roll < 36) {
            // Sudden shadow whoosh / lights out blackout
            SoundEvent sound = (intensity > 0.35f && random.nextBoolean())
                    ? FwModSounds.LIGHTS_OUT.get()
                    : FwModSounds.WHOOSH.get();
            mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
            darknessWaveTarget = Math.min(1.0f, 0.55f + 0.40f * intensity);
            blurSpike = Math.min(1.0f, blurSpike + 0.65f);
            cameraTremorTicks = 20;
            tremorIntensity = 0.5f + 0.5f * intensity;
        } else if (roll < 55) {
            // Rustling or swishing right behind the player
            SoundEvent sound = random.nextBoolean() ? FwModSounds.LEAVES_RUSTLE.get() : FwModSounds.SWISH.get();
            playBehindPlayer(mc, player, random, sound, volume, pitch, 2.0D, 4.5D);
            blurSpike = Math.min(1.0f, blurSpike + 0.30f);
        } else if (roll < 75) {
            // Fake footsteps right behind the player
            SoundEvent stepSound = switch (random.nextInt(3)) {
                case 0 -> SoundEvents.STONE_STEP;
                case 1 -> SoundEvents.WOOD_STEP;
                default -> SoundEvents.GRASS_STEP;
            };
            playBehindPlayer(mc, player, random, stepSound, Math.min(1.0f, volume * 1.15f), 0.85f + random.nextFloat() * 0.25f, 1.5D, 3.2D);
            blurSpike = Math.min(1.0f, blurSpike + 0.25f);
        } else {
            // Psychological monster/ambient hallucination behind the player
            SoundEvent eerieSound;
            int sub = random.nextInt(6);
            if (sub == 0) {
                eerieSound = SoundEvents.ENDERMAN_STARE;
            } else if (sub == 1 && intensity > 0.3f) {
                eerieSound = SoundEvents.CREEPER_PRIMED;
                pitch = 0.55f + random.nextFloat() * 0.25f;
            } else if (sub == 2) {
                eerieSound = SoundEvents.WARDEN_NEARBY_CLOSER;
            } else if (sub == 3) {
                eerieSound = SoundEvents.PHANTOM_SWOOP;
            } else if (sub == 4 && intensity > 0.4f) {
                eerieSound = SoundEvents.SCULK_SHRIEKER_SHRIEK;
            } else {
                eerieSound = SoundEvents.AMBIENT_CAVE.value();
            }
            playBehindPlayer(mc, player, random, eerieSound, volume * 0.85f, pitch, 2.5D, 5.5D);
            blurSpike = Math.min(1.0f, blurSpike + 0.50f);
            cameraTremorTicks = 25;
            tremorIntensity = 0.45f + 0.55f * intensity;
        }
    }

    private static void playBehindPlayer(
            Minecraft mc,
            LocalPlayer player,
            RandomSource random,
            SoundEvent sound,
            float volume,
            float pitch,
            double minDist,
            double maxDist
    ) {
        Vec3 look = player.getLookAngle();
        double dist = Mth.lerp(random.nextDouble(), minDist, maxDist);
        double sideOffset = (random.nextDouble() - 0.5D) * 2.5D;

        double x = player.getX() - look.x * dist + (-look.z * sideOffset);
        double y = player.getEyeY() + (random.nextDouble() - 0.5D) * 1.0D;
        double z = player.getZ() - look.z * dist + (look.x * sideOffset);

        SimpleSoundInstance instance = new SimpleSoundInstance(
                sound.getLocation(),
                SoundSource.AMBIENT,
                volume,
                pitch,
                random,
                false,
                0,
                SoundInstance.Attenuation.LINEAR,
                x,
                y,
                z,
                false
        );
        mc.getSoundManager().play(instance);
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (smoothedIntensity <= 0.005f && blurSpike <= 0.005f && darknessWave <= 0.005f) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }

        GuiGraphics gui = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        float time = (mc.player.tickCount + event.getPartialTick().getGameTimeDeltaPartialTick(false)) * 0.08f;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 1. Blur & Unfocused Double-Vision Filter
        float totalBlur = Mth.clamp(smoothedIntensity * 0.55f + blurSpike * 0.65f + heartbeatPulse * 0.25f, 0.0f, 1.0f);
        if (totalBlur > 0.02f) {
            float blurAlpha = Mth.clamp(totalBlur * 0.42f, 0.0f, 0.55f);
            int offset = Math.max(1, Math.round(totalBlur * 8.0f));
            int dx = (int) Math.round(Math.sin(time * 1.7f) * offset);
            int dy = (int) Math.round(Math.cos(time * 1.3f) * offset);

            RenderSystem.setShaderColor(0.85f, 0.78f, 0.82f, blurAlpha);
            gui.blit(PDA_BLUR_TEXTURE, -offset + dx, -offset + dy, width + offset * 2, height + offset * 2, 0, 0, 256, 256, 256, 256);
            gui.blit(PDA_BLUR_TEXTURE, -offset - dx, -offset - dy, width + offset * 2, height + offset * 2, 0, 0, 256, 256, 256, 256);
        }

        // 2. Animated Static / VHS Distortion Filter
        float staticAlpha = Mth.clamp((smoothedIntensity * 0.16f) + (blurSpike * 0.14f), 0.0f, 0.26f);
        if (staticAlpha > 0.01f) {
            RenderSystem.setShaderColor(0.75f, 0.65f, 0.70f, staticAlpha);
            gui.blit(VHS_NOISE_TEXTURE, 0, 0, width, height, noiseOffsetU, noiseOffsetV, width, height, 256, 256);
            if (smoothedIntensity > 0.35f) {
                RenderSystem.setShaderColor(0.6f, 0.15f, 0.15f, staticAlpha * 0.55f);
                gui.blit(SUPER_NOISE_TEXTURE, 0, 0, width, height, 255 - noiseOffsetU, 255 - noiseOffsetV, width, height, 256, 256);
            }
        }

        // 3. Darkness Overlay (pulsing darkness waves + base low-sanity dimming)
        float darknessAlpha = Mth.clamp(
                (smoothedIntensity * 0.32f) + (darknessWave * 0.50f) + (blurSpike * 0.20f),
                0.0f,
                0.78f
        );
        if (darknessAlpha > 0.01f) {
            int alphaInt = Mth.clamp((int) (darknessAlpha * 255.0f), 0, 205);
            int darkColor = (alphaInt << 24) | 0x050102;
            gui.fill(0, 0, width, height, darkColor);
        }

        // 4. Creeping Shadow & Crimson Heartbeat Vignette
        float vignetteStrength = Mth.clamp(
                (smoothedIntensity * 0.75f) + (heartbeatPulse * 0.35f) + (darknessWave * 0.25f),
                0.0f,
                0.95f
        );
        if (vignetteStrength > 0.02f) {
            // Soft outer shadow vignette
            RenderSystem.setShaderColor(0.05f, 0.01f, 0.02f, vignetteStrength * 0.75f);
            gui.blit(PDA_VIGNETTE_TEXTURE, 0, 0, width, height, 0, 0, 256, 256, 256, 256);

            // Inverse blend for deep edge darkening with subtle crimson pulse on heartbeat
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.ZERO,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );
            float pulseRed = heartbeatPulse * 0.18f * smoothedIntensity;
            float invR = Mth.clamp(vignetteStrength - pulseRed, 0.0f, 1.0f);
            float invG = Mth.clamp(vignetteStrength + pulseRed * 0.5f, 0.0f, 1.0f);
            float invB = Mth.clamp(vignetteStrength + pulseRed * 0.5f, 0.0f, 1.0f);
            RenderSystem.setShaderColor(invR, invG, invB, 1.0f);
            gui.blit(VIGNETTE_TEXTURE, 0, 0, width, height, 0, 0, 256, 256, 256, 256);
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (smoothedIntensity <= 0.01f) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }

        float darkenFactor = Mth.clamp(smoothedIntensity * 0.75f + darknessWave * 0.35f, 0.0f, 0.92f);
        float targetR = 0.04f + (heartbeatPulse * 0.05f * smoothedIntensity);
        float targetG = 0.01f;
        float targetB = 0.015f;

        event.setRed(Mth.lerp(darkenFactor, event.getRed(), targetR));
        event.setGreen(Mth.lerp(darkenFactor, event.getGreen(), targetG));
        event.setBlue(Mth.lerp(darkenFactor, event.getBlue(), targetB));
    }

    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (smoothedIntensity <= 0.05f && darknessWave <= 0.05f) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }

        float fogCloseFactor = Mth.clamp(smoothedIntensity * 0.70f + darknessWave * 0.35f, 0.0f, 0.88f);
        float scale = Mth.lerp(fogCloseFactor, 1.0f, 0.14f);
        event.scaleFarPlaneDistance(scale);
        event.scaleNearPlaneDistance(scale * 0.5f);
        event.setCanceled(true);
    }

    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (smoothedIntensity <= 0.02f && cameraTremorTicks <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.isPaused() || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }

        double partial = event.getPartialTick();
        double time = (mc.player.tickCount + partial) * 0.09D;

        // Unsettled breathing roll/pitch sway that intensifies exponentially at low sanity
        float swayRoll = (float) (Math.sin(time) * 1.8D * smoothedIntensity + Math.cos(time * 2.3D) * 0.9D * blurSpike);
        float swayPitch = (float) (Math.cos(time * 1.4D) * 0.9D * smoothedIntensity);

        // High-frequency tremor during hallucination spikes
        if (cameraTremorTicks > 0 || tremorIntensity > 0.02f) {
            float jitter = tremorIntensity * (0.5f + 0.5f * smoothedIntensity);
            swayRoll += (float) (Math.sin(time * 19.0D) * 2.2D * jitter);
            swayPitch += (float) (Math.cos(time * 23.0D) * 1.4D * jitter);
            event.setYaw(event.getYaw() + (float) (Math.sin(time * 17.0D) * 1.2D * jitter));
        }

        event.setRoll(event.getRoll() + swayRoll);
        event.setPitch(event.getPitch() + swayPitch);
    }

    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (smoothedIntensity <= 0.02f && heartbeatPulse <= 0.02f) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.isPaused() || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }

        // Subtle claustrophobic FOV narrowing + heartbeat throb
        double fovFactor = 1.0D - (smoothedIntensity * 0.06D) - (heartbeatPulse * 0.035D * smoothedIntensity) - (blurSpike * 0.03D);
        event.setFOV(event.getFOV() * fovFactor);
    }
}
