package net.nextfur.fwc.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.nextfur.fwc.FwMain;

public class FwModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, FwMain.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> FLASHLIGHT_TOGGLE = SOUND_EVENTS.register("flashlight_toggle", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "flashlight_toggle")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FLASHLIGHT_CLICK = SOUND_EVENTS.register("flashlight_click", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "flashlight_click")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTS_ON = SOUND_EVENTS.register("lights_on", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "lights_on")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTS_OUT = SOUND_EVENTS.register("lights_out", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "lights_out")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTBEAT = SOUND_EVENTS.register("heartbeat", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "heartbeat")));
    public static final DeferredHolder<SoundEvent, SoundEvent> INSANITY = SOUND_EVENTS.register("insanity", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "insanity")));
    public static final DeferredHolder<SoundEvent, SoundEvent> INSANITY1 = SOUND_EVENTS.register("insanity1", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "insanity1")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WHOOSH = SOUND_EVENTS.register("whoosh", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "whoosh")));
    public static final DeferredHolder<SoundEvent, SoundEvent> LEAVES_RUSTLE = SOUND_EVENTS.register("leaves_rustle", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "leaves_rustle")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SWISH = SOUND_EVENTS.register("swish", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "swish")));

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
