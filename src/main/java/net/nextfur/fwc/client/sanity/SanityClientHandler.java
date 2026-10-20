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
    private static final ResourceLocation VHS_NOISE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "textures/shaders/vhs_noise.png");
    private static final ResourceLocation SUPER_NOISE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "textures/shaders/super_noise.png");

    private static float clientSanity = SanityData.MAX_SANITY;
    private static float smoothedIntensity = 0.0f;

    // Long interval between client-side activities so nothing constantly ticks during normal gameplay
    // (scales from ~4-7 minutes near 50% sanity down to ~1.5-3 minutes at <=1% sanity)
    private static int nextActivityCooldownTicks = 1200;

    // Short-lived heartbeat bout state (only ticks during an active heartbeat event)
    private static int remainingHeartbeats = 0;
    private static int heartbeatStepTimer = 0;
    private static float heartbeatPulse = 0.0f;

    // Short-lived visual static pulse state (only active for a few seconds during an event)
    private static float staticPulse = 0.0f;
    private static float staticPulseTarget = 0.0f;
    private static int staticPulseDurationTicks = 0;

    // Short-lived camera tremor state
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
            cameraTremorTicks = Math.max(cameraTremorTicks, 18);
            tremorIntensity = Math.max(tremorIntensity, 0.25f + 0.45f * targetIntensity);
        }
    }

    public static float getClientSanity() {
        return clientSanity;
    }

    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clientSanity = SanityData.MAX_SANITY;
        smoothedIntensity = 0.0f;
        nextActivityCooldownTicks = 1200;
        remainingHeartbeats = 0;
        heartbeatStepTimer = 0;
        heartbeatPulse = 0.0f;
        staticPulse = 0.0f;
        staticPulseTarget = 0.0f;
        staticPulseDurationTicks = 0;
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
            remainingHeartbeats = 0;
            heartbeatPulse *= 0.85f;
            staticPulseTarget = 0.0f;
            staticPulse *= 0.85f;
            cameraTremorTicks = 0;
            return;
        }

        float targetIntensity = SanityService.getInsanityIntensity(clientSanity);
        smoothedIntensity = Mth.lerp(0.05f, smoothedIntensity, targetIntensity);

        // Decay transient pulses so the screen & camera stay completely clean between activities
        heartbeatPulse *= 0.86f;
        if (cameraTremorTicks > 0) {
            cameraTremorTicks--;
        } else {
            tremorIntensity *= 0.82f;
        }

        if (staticPulseDurationTicks > 0) {
            staticPulseDurationTicks--;
        } else {
            staticPulseTarget = 0.0f;
        }
        staticPulse = Mth.lerp(0.08f, staticPulse, staticPulseTarget);

        if (smoothedIntensity <= 0.002f) {
            remainingHeartbeats = 0;
            return;
        }

        RandomSource random = player.getRandom();
        if (staticPulse > 0.005f) {
            noiseOffsetU = random.nextInt(256);
            noiseOffsetV = random.nextInt(256);
        }

        // Tick short heartbeat bout if one is currently active
        if (remainingHeartbeats > 0) {
            if (heartbeatStepTimer > 0) {
                heartbeatStepTimer--;
            } else {
                remainingHeartbeats--;
                heartbeatStepTimer = Mth.lerpInt(smoothedIntensity, 36, 22);

                float volume = Mth.lerp(smoothedIntensity, 0.18f, 0.55f);
                float pitch = Mth.lerp(smoothedIntensity, 0.88f, 1.08f);
                mc.getSoundManager().play(SimpleSoundInstance.forUI(FwModSounds.HEARTBEAT.get(), pitch, volume));

                heartbeatPulse = Math.min(1.0f, 0.25f + 0.50f * smoothedIntensity);
            }
        }

        // Long interval timer between discrete insanity activities
        if (nextActivityCooldownTicks > 0) {
            nextActivityCooldownTicks--;
            return;
        }

        // Once the long interval has elapsed, roll a low probability per tick to trigger a single activity bout
        float chancePerTick = 0.0002f + (0.0018f * smoothedIntensity);
        if (random.nextFloat() < chancePerTick) {
            triggerDiscreteActivity(mc, player, random, smoothedIntensity);

            // Schedule the next interval:
            // ~4 to 7 minutes (4800-8400 ticks) near 50% sanity
            // ~1.5 to 3 minutes (1800-3600 ticks) at <=1% sanity (100% activity)
            int minInterval = Mth.lerpInt(smoothedIntensity, 4800, 1800);
            int maxInterval = Mth.lerpInt(smoothedIntensity, 8400, 3600);
            nextActivityCooldownTicks = Mth.nextInt(random, minInterval, maxInterval);
        }
    }

    private static void triggerDiscreteActivity(
            Minecraft mc,
            LocalPlayer player,
            RandomSource random,
            float intensity
    ) {
        int roll = random.nextInt(100);

        if (roll < 35) {
            // Activity 1: Short heartbeat bout (3 to 5 beats, then stops completely)
            remainingHeartbeats = Mth.nextInt(random, 3, 5);
            heartbeatStepTimer = 0;
        } else if (roll < 60) {
            // Activity 2: Brief subtle static flicker (lasts ~2-3.5 seconds, then clears completely)
            staticPulseTarget = Mth.lerp(intensity, 0.15f, 0.45f);
            staticPulseDurationTicks = Mth.lerpInt(intensity, 40, 70);
        } else {
            // Activity 3: Brief auditory hallucination
            playRandomScarySound(mc, player, random, intensity);
        }
    }

    private static void playRandomScarySound(
            Minecraft mc,
            LocalPlayer player,
            RandomSource random,
            float intensity
    ) {
        int roll = random.nextInt(100);
        float volume = Mth.lerp(intensity, 0.28f, 0.72f) * (0.8f + 0.4f * random.nextFloat());
        float pitch = 0.75f + random.nextFloat() * 0.40f;

        if (roll < 22) {
            // FurWatch insanity whisper/drone
            SoundEvent sound = random.nextBoolean() ? FwModSounds.INSANITY.get() : FwModSounds.INSANITY1.get();
            mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
            cameraTremorTicks = 20;
            tremorIntensity = 0.25f + 0.40f * intensity;
        } else if (roll < 36) {
            // Sudden shadow whoosh with brief static flicker
            SoundEvent sound = (intensity > 0.35f && random.nextBoolean())
                    ? FwModSounds.LIGHTS_OUT.get()
                    : FwModSounds.WHOOSH.get();
            mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
            staticPulseTarget = Math.min(0.50f, 0.20f + 0.25f * intensity);
            staticPulseDurationTicks = 50;
            cameraTremorTicks = 15;
            tremorIntensity = 0.30f + 0.35f * intensity;
        } else if (roll < 58) {
            // Rustling or swishing right behind the player
            SoundEvent sound = random.nextBoolean() ? FwModSounds.LEAVES_RUSTLE.get() : FwModSounds.SWISH.get();
            playBehindPlayer(mc, player, random, sound, volume, pitch, 2.0D, 4.5D);
        } else if (roll < 80) {
            // Fake footsteps right behind the player
            SoundEvent stepSound = switch (random.nextInt(3)) {
                case 0 -> SoundEvents.STONE_STEP;
                case 1 -> SoundEvents.WOOD_STEP;
                default -> SoundEvents.GRASS_STEP;
            };
            playBehindPlayer(mc, player, random, stepSound, Math.min(0.85f, volume * 1.1f), 0.85f + random.nextFloat() * 0.25f, 1.5D, 3.2D);
        } else {
            // Psychological ambient hallucination behind the player
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
            playBehindPlayer(mc, player, random, eerieSound, volume * 0.80f, pitch, 2.5D, 5.5D);
            cameraTremorTicks = 18;
            tremorIntensity = 0.30f + 0.40f * intensity;
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
        // Only render overlays during active transient pulses (never constantly on screen)
        if (staticPulse <= 0.005f && heartbeatPulse <= 0.005f) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }

        GuiGraphics gui = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        gui.flush();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        // 1. Subtle Additive Static / VHS Grain Overlay (only during a brief static pulse event)
        float staticStrength = Mth.clamp(staticPulse * 0.12f, 0.0f, 0.12f);
        if (staticStrength > 0.005f) {
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );
            RenderSystem.setShaderColor(0.75f, 0.65f, 0.70f, staticStrength);
            gui.blit(VHS_NOISE_TEXTURE, 0, 0, width, height, noiseOffsetU, noiseOffsetV, width, height, 256, 256);
            if (smoothedIntensity > 0.35f) {
                RenderSystem.setShaderColor(0.6f, 0.20f, 0.20f, staticStrength * 0.45f);
                gui.blit(SUPER_NOISE_TEXTURE, 0, 0, width, height, 255 - noiseOffsetU, 255 - noiseOffsetV, width, height, 256, 256);
            }
        }

        // 2. Gentle Edge-Only Crimson Heartbeat Vignette (only during an active heartbeat pulse)
        float vignetteStrength = Mth.clamp(heartbeatPulse * 0.22f, 0.0f, 0.24f);
        if (vignetteStrength > 0.01f) {
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.ZERO,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );
            float pulseRed = heartbeatPulse * 0.10f * smoothedIntensity;
            float invR = Mth.clamp(vignetteStrength - pulseRed, 0.0f, 0.24f);
            float invG = Mth.clamp(vignetteStrength + pulseRed * 0.4f, 0.0f, 0.28f);
            float invB = Mth.clamp(vignetteStrength + pulseRed * 0.4f, 0.0f, 0.28f);
            RenderSystem.setShaderColor(invR, invG, invB, 1.0f);
            gui.blit(VIGNETTE_TEXTURE, 0, 0, width, height, 0, 0, 256, 256, 256, 256);
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        gui.flush();
    }

    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        // Only apply camera tremor during brief hallucination spikes (no constant camera sway)
        if (cameraTremorTicks <= 0 && tremorIntensity <= 0.02f) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.isPaused() || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }

        double partial = event.getPartialTick();
        double time = (mc.player.tickCount + partial) * 0.09D;

        float jitter = tremorIntensity * (0.5f + 0.5f * smoothedIntensity);
        float swayRoll = (float) (Math.sin(time * 19.0D) * 1.6D * jitter);
        float swayPitch = (float) (Math.cos(time * 23.0D) * 1.0D * jitter);
        event.setYaw(event.getYaw() + (float) (Math.sin(time * 17.0D) * 0.9D * jitter));
        event.setRoll(event.getRoll() + swayRoll);
        event.setPitch(event.getPitch() + swayPitch);
    }

    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        // Only apply a subtle FOV throb during an active heartbeat pulse
        if (heartbeatPulse <= 0.02f) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.isPaused() || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }

        double fovFactor = 1.0D - (heartbeatPulse * 0.025D * smoothedIntensity);
        event.setFOV(event.getFOV() * fovFactor);
    }
}
