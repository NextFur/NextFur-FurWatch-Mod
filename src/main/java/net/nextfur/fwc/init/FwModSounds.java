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
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTS_OFF = SOUND_EVENTS.register("lights_off", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "lights_off")));

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}